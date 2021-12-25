package com.eappcat.flink.sql.web.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * stats接口的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class JobInfo {
    private String id;
    private String status;
    private List<MetricInfo> metics=new ArrayList<>();
    private Date startDate;
    private Date endDate;
}
