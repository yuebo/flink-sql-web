package com.eappcat.flink.sql.web.vo;

import lombok.Data;

/**
 * 前端查询的VO
 * @author Xuan Yue Bo
 */
@Data
public class NotebooksSearchVO {
    private int page;
    private int size;
    private String orderBy;
    private String direction;
    private String jobIdLike;
    private String fromDate;
    private String toDate;
}
