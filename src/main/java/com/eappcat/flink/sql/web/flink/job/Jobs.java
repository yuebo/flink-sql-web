package com.eappcat.flink.sql.web.flink.job;

import lombok.Data;

import java.util.List;

/**
 * Flink Rest Aoi对应的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class Jobs {
    private List<JobItem> jobs;
}
