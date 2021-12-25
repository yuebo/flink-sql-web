package com.eappcat.flink.sql.web.dao;


import com.eappcat.flink.sql.web.entity.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Job Repository
 * @author Xuan Yue Bo
 */
public interface JobDao extends JpaRepository<JobEntity,String>, JpaSpecificationExecutor<JobEntity> {
    List<JobEntity> findByStatusIn(List<String> status);
}
