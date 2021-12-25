package com.eappcat.flink.sql.web.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.eappcat.flink.sql.web.FlinkConfigProperties;
import com.eappcat.flink.sql.web.dao.JobDao;
import com.eappcat.flink.sql.web.dao.OperatorDao;
import com.eappcat.flink.sql.web.dto.JobInfo;
import com.eappcat.flink.sql.web.dto.MetricInfo;
import com.eappcat.flink.sql.web.dto.NotebookInfo;
import com.eappcat.flink.sql.web.entity.JobEntity;
import com.eappcat.flink.sql.web.entity.JobOperatorEntity;
import com.eappcat.flink.sql.web.entity.NotebookEntity;
import com.eappcat.flink.sql.web.flink.job.Job;
import com.eappcat.flink.sql.web.flink.job.JobListenerImpl;
import com.eappcat.flink.sql.web.flink.job.Metric;
import com.eappcat.flink.sql.web.flink.job.Vertice;
import com.eappcat.flink.sql.web.parser.SqlCommandParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.configuration.*;
import org.apache.flink.core.execution.DefaultExecutorServiceLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.GroovyClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.flink.configuration.ConfigOptions.key;

/**
 * Flink job 相关的api调用
 * @author Xuan Yue Bo
 */
@Service
@Slf4j
public class ExecuteService {
    @Autowired
    private FlinkApiService flinkApiService;
    @Autowired
    private NoteBookService noteBookService;
    @Autowired
    private JobDao jobDao;
    @Autowired
    private OperatorDao operatorDao;
    @Autowired
    private FlinkConfigProperties configProperties;

    /**
     * 启动本地detach模式提交任务，会编译notebook的sql语言等
     * @param inputStream
     * @return
     * @throws Exception
     */
    public NotebookEntity run(InputStream inputStream) throws Exception{
        NotebookEntity notebook=new NotebookEntity();
        notebook.setId(UUID.randomUUID().toString());
        try(InputStream stream=inputStream) {
            notebook.setContent(IOUtils.toString(stream, StandardCharsets.UTF_8.name()));
        }
        notebook.setExecuteDate(new Date());
        notebook.setCancelDate(null);
        List<String> sqls = Arrays.asList(notebook.getContent().split("\n"));
        File file=compileJar(sqls,notebook.getId());
        notebook.setUserLibPath(file.getAbsolutePath());
        boolean exists=file.exists();
        NotebookEntity entity = noteBookService.save(notebook);
        //执行flink job，获取当前现场获取的jobId
        try {
            JobListenerImpl.init();
            submitJob(entity.getId(),exists);
            if (JobListenerImpl.jobs()!=null && JobListenerImpl.jobs().size()>0){
                List<JobEntity> entities=new ArrayList<>();
                for (String id:JobListenerImpl.jobs()) {
                    JobEntity jobEntity=new JobEntity();
                    jobEntity.setNotebookId(entity.getId());
                    jobEntity.setStartDate(new Date());
                    jobEntity.setStatus("CREATED");
                    jobEntity.setId(id);
                    entities.add(jobEntity);
                }
                jobDao.saveAll(entities);
            }
        }finally {
            JobListenerImpl.release();
        }

        return noteBookService.findById(entity.getId());
    }

    public String cancel(String noteId) {
        JobEntity jobEntity=new JobEntity();
        jobEntity.setNotebookId(noteId);
        List<JobEntity> jobEntityList=jobDao.findAll(Example.of(jobEntity));
        if (jobEntityList.size()>0){
            for (JobEntity job:jobEntityList) {
                flinkApiService.cancelJob(job.getId());
            }
        }
        NotebookEntity notebook=noteBookService.findById(noteId);
        notebook.setCancelDate(new Date());
        return notebook.getId();
    }

    /**
     * 编译notebook包含的Java Class
     * @param sqls
     * @param notebookId
     * @return
     * @throws IOException
     */
    public static File compileJar(List<String> sqls,String notebookId) throws IOException {
        List<SqlCommandParser.SqlCommandCall> sqlCommands=SqlCommandParser.parse(sqls);
        File root=FileUtils.getTempDirectory();

        File jarFile = new File(root,"jobSubmit/"+notebookId+"/udf.jar");
        if(!jarFile.getParentFile().exists()){
            jarFile.getParentFile().mkdirs();
        }

        for (SqlCommandParser.SqlCommandCall call:sqlCommands){
            switch (call.command){
                case CREATE_INLINE_CLASS:
                    String key =  call.operands[0];
                    String function = call.operands[1];
                    CompilerConfiguration compilerConfiguration=new CompilerConfiguration();
                    compilerConfiguration.setTargetDirectory(new File(root,"jobSubmit/"+notebookId+"/compile"));
                    CompilationUnit cu = new CompilationUnit(compilerConfiguration);
                    cu.addSource(key,function);
                    cu.compile();
                    try(ZipOutputStream zos=new ZipOutputStream(new FileOutputStream(jarFile))) {
                        List<GroovyClass> groovyClasses=cu.getClasses();
                        for (GroovyClass groovyClass:groovyClasses) {
                            String value=StringUtils.replace(groovyClass.getName(),".","/").concat(".class");
                            ZipEntry ze = new ZipEntry(value);
                            zos.putNextEntry(ze);
                            zos.write(groovyClass.getBytes(), 0, groovyClass.getBytes().length);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return jarFile;
    }

    /**
     * 提交任务
     * @param notebookId notebook的标识
     * @param userLib 是否含有脚本
     * @throws MalformedURLException
     * @throws ProgramInvocationException
     */
    public void submitJob(String notebookId,boolean userLib) throws MalformedURLException, ProgramInvocationException {
        ConfigOption<String> notebookIdOption=key("notebook.id")
                .stringType()
                .noDefaultValue()
                .withDescription("Attached notebookId");
        Configuration configuration=new Configuration();
        configuration.setString("pipeline.name","test");
        configuration.setString(JobManagerOptions.ADDRESS, configProperties.getHost());
        configuration.setInteger(JobManagerOptions.PORT, configProperties.getJobManagerPort());
        configuration.setInteger(RestOptions.PORT, configProperties.getRestPort());
        configuration.setString(DeploymentOptions.TARGET, "remote");
        configuration.setBoolean(DeploymentOptions.ATTACHED, false);
        //用于job回传更新状态
        configuration.set(notebookIdOption, notebookId);
        configuration.set(DeploymentOptions.JOB_LISTENERS, Arrays.asList(JobListenerImpl.class.getName()));

        String[] args = new String[]{ configProperties.getCallbackUrl().concat("/notebook/").concat(notebookId) };
        String url= configProperties.getCallbackUrl().concat("/jar/").concat(notebookId).concat("/udf.jar");
        if (userLib){
            configuration.set(PipelineOptions.CLASSPATHS,Arrays.asList(url));
        }
        configuration.set(PipelineOptions.JARS,Arrays.asList(new File(configProperties.getSubmitJobLocation()).toURL().toString()));

        PackagedProgram.Builder builder=PackagedProgram.newBuilder().setEntryPointClassName("com.eappcat.flink.SubmitStreamJob")
                .setConfiguration(configuration)
                .setJarFile(new File(configProperties.getSubmitJobLocation()));
        if (userLib){
            builder.setUserClassPaths(Arrays.asList(new URL(url)));
        }
        PackagedProgram packagedProgram=builder.setArguments(args).build();


        ClientUtils.executeProgram(new DefaultExecutorServiceLoader(), configuration, packagedProgram, false, true);
    }

    /**
     * 通过Rest API获取状态
     * @param noteId
     * @return
     */
    public NotebookInfo stats(String noteId) {
        NotebookInfo info=new NotebookInfo();
        NotebookEntity notebook=noteBookService.findById(noteId);
        JobEntity jobEntity=new JobEntity();
        jobEntity.setNotebookId(noteId);
        List<JobEntity> jobEntityList=jobDao.findAll(Example.of(jobEntity));
        info.setId(notebook.getId());
        if (jobEntityList.size()>0){
            for (JobEntity item:jobEntityList) {
                String jobId = item.getId();
                JobInfo jobInfo=new JobInfo();
                jobInfo.setId(jobId);
                Job job=flinkApiService.job(jobId);
                jobInfo.setStartDate(new Date(job.getStartTime()));
                jobInfo.setStatus(job.getState());
                if (job.getEndTime()!=null&&job.getEndTime()>0){
                    jobInfo.setEndDate(new Date(job.getEndTime()));
                }
                List<String> ids=job.getVertices().stream().map(Vertice::getId).collect(Collectors.toList());
                for (String vid:ids){
                   List<String> metricIds = flinkApiService.verticeMetric(jobId,vid).stream().map(Metric::getId).filter(val-> val.endsWith("numRecordsOut")||val.endsWith("numRecordsIn")).collect(Collectors.toList());
                   List<Metric> metrics=flinkApiService.verticeMetric(jobId,vid,String.join(",",metricIds));
                   for (Metric metric:metrics){
                       MetricInfo metricInfo=new MetricInfo();
                       metricInfo.setId(metric.getId());
                       metricInfo.setVerticeId(vid);
                       metricInfo.setValue(Long.parseLong(metric.getValue()));
                       jobInfo.getMetics().add(metricInfo);
                   }
                }
                info.getJobs().add(jobInfo);
            }
        }

        return info;
    }

    /**
     * 处理flink metric事件，对算子状态进行同步
     * @param message
     */
    @Transactional(rollbackFor = Exception.class)
    public void onMetricEvent(String message) {
        JSONArray jsonArray=JSONObject.parseArray(message);
        List<JobOperatorEntity> entities=new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject metric = jsonArray.getJSONObject(i);
            String jobId = metric.getJSONObject("metric").getString("job_id");
            String taskId = metric.getJSONObject("metric").getString("task_id");
            String index = metric.getJSONObject("metric").getString("subtask_index");
            String host = metric.getJSONObject("metric").getString("host");
            String operatorName = metric.getJSONObject("metric").getString("operator_name");
            String jobName = metric.getJSONObject("metric").getString("job_name");
            String taskName = metric.getJSONObject("metric").getString("task_name");
            String operatorId = metric.getJSONObject("metric").getString("operator_id");
            String name = metric.getJSONObject("metric").getString("name");
            String tmId = metric.getJSONObject("metric").getString("tm_id");

            if (StringUtils.isNotEmpty(operatorId)){
                if (("numRecordsIn".equals(metric.getString("name")) || "numRecordsOut".equals(metric.getString("name")))) {
                    JobOperatorEntity entity=new JobOperatorEntity();
                    String id=jobId.concat(".").concat(taskId).concat(".").concat(operatorId).concat(".").concat(index).concat(".").concat(metric.getString("name"));
                    entity.setId(DigestUtils.sha256Hex(id));
                    entity.setOperatorId(operatorId);
                    entity.setOperatorName(operatorName);
                    entity.setCount(metric.getJSONObject("value").getLong("count"));
                    entity.setMetricName(name);
                    entity.setHost(host);
                    entity.setTaskId(taskId);
                    entity.setTaskName(taskName);
                    entity.setTaskIndex(index);
                    entity.setJobId(jobId);
                    entity.setJobName(jobName);
                    entity.setTaskManagerId(tmId);
                    entities.add(entity);
                }
            }

        }

        if (entities.size()>0){
            operatorDao.deleteInBatch(entities.stream().map(e->{
                JobOperatorEntity entity=new JobOperatorEntity();
                entity.setId(e.getId());
                return entity;
            }).collect(Collectors.toList()));
            operatorDao.saveAll(entities);
        }

    }


    /**
     * 根据nodebook id获取job对应的运行指标，包括状态和算子的处理数量
     * @param noteId
     * @return
     */
    public JSONObject statistic(String noteId) {
        JSONObject info=new JSONObject();
        NotebookEntity notebook=noteBookService.findById(noteId);
        JobEntity jobEntity=new JobEntity();
        jobEntity.setNotebookId(noteId);
        List<JobEntity> jobEntityList=jobDao.findAll(Example.of(jobEntity));
        info.put("id",notebook.getId());
        List<JSONObject> items=new ArrayList<>();
        if (jobEntityList.size()>0){
            for (JobEntity item:jobEntityList) {

                JobOperatorEntity operatorEntity=new JobOperatorEntity();
                operatorEntity.setJobId(item.getId());
                JSONObject jobInfo=new JSONObject();
                jobInfo.put("jobId",item.getId());
                jobInfo.put("status",item.getStatus());
                jobInfo.put("start",item.getStartDate());
                jobInfo.put("end",item.getEndDate());
                jobInfo.put("list",operatorDao.findAll(Example.of(operatorEntity)));
                items.add(jobInfo);
            }
        }
        info.put("list",items);
        return info;
    }

    /**
     * 定时同步状态方法，使用flink rest api获取状态，如果不是最终状态，则进行同步。
     * 终止状态有：CANCELED, FINISHED, FAILED
     */
    @Transactional(rollbackFor = Exception.class)
    public void syncStatus(){
        List<JobEntity> jobEntityList=jobDao.findByStatusIn(Arrays.asList("CREATED","SCHEDULED","DEPLOYING","CANCELING","RUNNING"));
        for (JobEntity job:jobEntityList){
            try {
                Job job1 = flinkApiService.job(job.getId());
                job.setStatus(job1.getState());
                if (job1.getEndTime()!=null){
                    job.setEndDate(new Date(job1.getEndTime()));
                }
            }catch (Exception e){
                log.error("{}",e);
            }

        }
        if (jobEntityList.size()>0){
            jobDao.saveAll(jobEntityList);
        }
    }
}
