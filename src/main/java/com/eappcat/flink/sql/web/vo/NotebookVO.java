package com.eappcat.flink.sql.web.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.util.Date;

/**
 * Notebook的对应的VO
 * @author Xuan Yue Bo
 */
@Data
public class NotebookVO {
    private String id;
    private String content;
    private Date updatedDate;
    private Date executeDate;
    private Date cancelDate;
    private String userLibPath;
    private Long version;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getUpdatedDate() {
        return updatedDate;
    }
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getExecuteDate() {
        return executeDate;
    }
    private String jobId;
}
