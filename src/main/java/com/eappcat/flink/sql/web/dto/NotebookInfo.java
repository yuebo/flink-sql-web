package com.eappcat.flink.sql.web.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * stats接口的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class NotebookInfo {
    private String id;
    private List<JobInfo> jobs=new ArrayList<>();

}
