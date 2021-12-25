package com.eappcat.flink.sql.web.dao;


import com.eappcat.flink.sql.web.entity.JobOperatorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Operator Repository
 * @author Xuan Yue Bo
 */
public interface OperatorDao extends JpaRepository<JobOperatorEntity,String>, JpaSpecificationExecutor<JobOperatorEntity> {
}
