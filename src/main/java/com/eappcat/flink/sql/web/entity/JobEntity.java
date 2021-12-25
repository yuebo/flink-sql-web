package com.eappcat.flink.sql.web.entity;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

@Entity(name = "f_flink_job")
@Data
public class JobEntity {
    @Id
    private String id;
    private String notebookId;
    private String status;
    private Date startDate;
    private Date endDate;
}
