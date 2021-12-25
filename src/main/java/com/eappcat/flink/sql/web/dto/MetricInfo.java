package com.eappcat.flink.sql.web.dto;

import lombok.Data;

/**
 * stats接口的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class MetricInfo {
    private String verticeId;
    private String id;
    private Long value;
}
