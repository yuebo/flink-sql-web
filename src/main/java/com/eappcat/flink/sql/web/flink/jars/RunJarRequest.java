package com.eappcat.flink.sql.web.flink.jars;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
/**
 * Flink Rest Aoi对应的数据格式
 * @author Xuan Yue Bo
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RunJarRequest {
    private Boolean allowNonRestoredState;
//    @JsonProperty("entry-class")
//    private String entryClass;
    private String jobId;
    private Long parallelism;
    private String programArgs;
    private String savepointPath;
    @JsonProperty("program-args")
    private List<String> programArgsList;
}
