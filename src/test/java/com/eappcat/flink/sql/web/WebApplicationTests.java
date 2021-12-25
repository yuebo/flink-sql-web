package com.eappcat.flink.sql.web;

import com.google.common.collect.Lists;
import com.eappcat.flink.sql.web.flink.job.JobListenerImpl;
import com.eappcat.flink.sql.web.parser.SqlCommandParser;
import com.eappcat.flink.sql.web.service.NoteBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.client.ClientUtils;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.configuration.*;
import org.apache.flink.core.execution.DefaultExecutorServiceLoader;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.tools.GroovyClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.apache.flink.configuration.ConfigOptions.key;

@SpringBootTest
@Slf4j
class WebApplicationTests {
    @Autowired
    private NoteBookService noteBookService;
    @Test
    void contextLoads() throws Exception {
        List<String> sqls = new ArrayList<>();
        String sql="D:/projects/flink-sql-submit-job/sql/test.sql";
        sqls.addAll(Files.readAllLines(Paths.get(sql)));
        compileJar(sqls);
        try {
            submitJob();
//            JobClient jobClient=JobListenerImpl.jobClient();
//            JobID id = jobClient.getJobID();
//            log.info("id:{}",id.toHexString());
        }finally {
//            JobListenerImpl.release();
        }
    }

    private void submitJob() throws MalformedURLException, ProgramInvocationException {
        ConfigOption<String> notebookId=key("notebook.id")
                .stringType()
                .noDefaultValue()
                .withDescription("Attached notebookId");
        Configuration configuration=new Configuration();
        configuration.setString(JobManagerOptions.ADDRESS, "192.168.7.123");
        configuration.setInteger(JobManagerOptions.PORT, 6123);
        configuration.setInteger(RestOptions.PORT, 8081);
        configuration.setString(DeploymentOptions.TARGET, "remote");
        configuration.setBoolean(DeploymentOptions.ATTACHED, false);
        //用于job回传更新状态
        configuration.set(notebookId, "12345678");
        configuration.set(DeploymentOptions.JOB_LISTENERS, Lists.newArrayList(JobListenerImpl.class.getName()));

        String jarFilePath = "D:/projects/flink-sql-submit-job/target/flink-sql-submit-job-1.0-SNAPSHOT.jar";
        String[] args = new String[]{ "http://192.168.8.29:8080/test.sql" };
        String url="http://192.168.8.29:8080/udf.jar";
        configuration.set(PipelineOptions.CLASSPATHS,Lists.newArrayList(url));
        configuration.set(PipelineOptions.JARS,Lists.newArrayList(new File(jarFilePath).toURL().toString()));

        PackagedProgram packagedProgram=PackagedProgram.newBuilder().setEntryPointClassName("com.eappcat.flink.SubmitStreamJob").setConfiguration(configuration).setJarFile(new File(jarFilePath))
                .setUserClassPaths(Lists.newArrayList(new URL(url))).setArguments(args).build();

        ClientUtils.executeProgram(new DefaultExecutorServiceLoader(), configuration, packagedProgram, false, true);
    }

    private void compileJar(List<String> sqls) throws IOException {
        List<SqlCommandParser.SqlCommandCall> sqlCommands=SqlCommandParser.parse(sqls);

        for (SqlCommandParser.SqlCommandCall call:sqlCommands){
            switch (call.command){
                case CREATE_INLINE_CLASS:
                    String key =  call.operands[0];
                    String function = call.operands[1];
                    CompilerConfiguration compilerConfiguration=new CompilerConfiguration();
                    compilerConfiguration.setTargetDirectory("target/compile");
                    CompilationUnit cu = new CompilationUnit(compilerConfiguration);
                    cu.addSource(key,function);
                    cu.compile();
                    try(ZipOutputStream zos=new ZipOutputStream(new FileOutputStream("D:/projects/flink-sql-submit-job/sql/udf.jar"))) {
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
    }


}
