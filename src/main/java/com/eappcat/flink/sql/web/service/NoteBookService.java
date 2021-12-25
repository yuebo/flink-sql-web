package com.eappcat.flink.sql.web.service;

import com.eappcat.flink.sql.web.dao.JobDao;
import com.eappcat.flink.sql.web.dao.NotebookDao;
import com.eappcat.flink.sql.web.entity.JobEntity;
import com.eappcat.flink.sql.web.entity.NotebookEntity;
import com.eappcat.flink.sql.web.vo.NotebookVO;
import com.eappcat.flink.sql.web.vo.NotebooksSearchVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Nodebook 查询修改相关接口
 * @author Xuan Yue Bo
 */
@Service
public class NoteBookService {
    @Autowired
    private NotebookDao notebookDao;
    @Autowired
    private JobDao jobDao;


    public Page<NotebookEntity> findPage(Pageable pageable){
        return notebookDao.findAll(pageable);
    }

    public Page<NotebookVO> findPage(NotebooksSearchVO notebooksSearchVO) {
        SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd yyyy HH:mm:ss 'GMT'Z", Locale.ENGLISH);
        Date start = new Date(0);
        Date end = new Date();
        if (!StringUtils.isEmpty(notebooksSearchVO.getFromDate())){
            try {
                start = format.parse(notebooksSearchVO.getFromDate());
            } catch (ParseException e) {
            }
        }
        if (!StringUtils.isEmpty(notebooksSearchVO.getToDate())) {
            try {
                end = format.parse(notebooksSearchVO.getToDate());
            } catch (ParseException e) {
            }
        }

        if (StringUtils.isEmpty(notebooksSearchVO.getDirection())){
            notebooksSearchVO.setDirection("asc");
        }
        if (StringUtils.isEmpty(notebooksSearchVO.getOrderBy())){
            notebooksSearchVO.setOrderBy("executeDate");
        }
        Page<NotebookEntity> result=notebookDao.findByExecuteDateBetween(start,end,PageRequest.of(notebooksSearchVO.getPage(),notebooksSearchVO.getSize(),Sort.by(Sort.Direction.fromString(notebooksSearchVO.getDirection()),notebooksSearchVO.getOrderBy())));

        //兼容老前端，如果重新设计可以去掉jobId相关内容
        List<NotebookVO> data = result.getContent().stream().map(notebookEntity -> {
            JobEntity jobEntity=new JobEntity();
            jobEntity.setNotebookId(notebookEntity.getId());
            List<JobEntity> entities=jobDao.findAll(Example.of(jobEntity));
            NotebookVO notebookVO1=new NotebookVO();
            BeanUtils.copyProperties(notebookEntity,notebookVO1);
            notebookVO1.setJobId(String.join(",",entities.stream().map(JobEntity::getId).collect(Collectors.toList())));
            return notebookVO1;
        }).collect(Collectors.toList());
        return new PageImpl<>(data,result.getPageable(),result.getTotalElements());
    }

    @Transactional(rollbackFor = Exception.class)
    public NotebookEntity save(NotebookEntity notebook){
        if (StringUtils.isEmpty(notebook.getId())){
            notebook.setId(UUID.randomUUID().toString());
            notebook.setVersion(1L);
        }else {
           Optional<NotebookEntity> notebook1=notebookDao.findById(notebook.getId());
           if (notebook1.isPresent()){
               BeanUtils.copyProperties(notebook,notebook1,"id","version","updatedDate");
           }else {
               notebook.setId(UUID.randomUUID().toString());
               notebook.setVersion(1L);
           }
        }
        notebook.setUpdatedDate(new Date());

        return this.notebookDao.save(notebook);
    }


    public NotebookEntity findById(String noteId) {
        return notebookDao.findById(noteId).get();
    }

}
