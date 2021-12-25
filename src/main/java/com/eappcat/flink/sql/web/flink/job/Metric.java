package com.eappcat.flink.sql.web.flink.job;

import lombok.Data;

/**
 * Flink Rest Aoi对应的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class Metric {
    private String id;
    private String value;
}
