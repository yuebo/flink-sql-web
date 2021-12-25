package com.eappcat.flink.sql.web.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * notebook对应的数据表
 * [Notebook] 1 => N [Job] 1 => N [Operator]
 * @author Xuan Yue Bo
 */
@Entity(name = "f_flink_notebook")
@Data
public class NotebookEntity {
    @Id
    private String id;
    @Column(length = 4000)
    private String content;
    @LastModifiedDate
    private Date updatedDate;
    private Date executeDate;
    private Date cancelDate;
    private String userLibPath;
    @Version
    private Long version;

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getUpdatedDate() {
        return updatedDate;
    }
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    public Date getExecuteDate() {
        return executeDate;
    }
}
