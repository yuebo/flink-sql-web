package com.eappcat.flink.sql.web.flink.job;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Flink Rest Aoi对应的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class Job {
    private String jid;
    private String name;
    @JsonProperty("isStoppable")
    private Boolean stoppable;
    private String state;
    @JsonProperty("start-time")
    private Long startTime;
    @JsonProperty("end-time")
    private Long endTime;
    private Long duration;
    private Long now;
    private List<Vertice> vertices;
}
