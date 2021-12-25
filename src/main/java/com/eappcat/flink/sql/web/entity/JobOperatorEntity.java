package com.eappcat.flink.sql.web.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * flink operation对应的数据表
 * 数据由kafka消息同步过来
 * @author Xuan Yue Bo
 */
@Entity(name = "f_flink_job_operator")
@Data
public class JobOperatorEntity {
    @Id
    private String id;
    private String operatorId;
    private String jobId;
    private String taskId;
    private String jobName;
    private String taskName;
    private String operatorName;
    private String taskManagerId;
    private String taskIndex;
    private String host;
    private String metricName;
    private Long count;
}
