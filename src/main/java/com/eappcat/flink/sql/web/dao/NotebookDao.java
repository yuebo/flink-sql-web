package com.eappcat.flink.sql.web.dao;


import com.eappcat.flink.sql.web.entity.NotebookEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Date;

/**
 * Notebook Repository
 * @author Xuan Yue Bo
 */
public interface NotebookDao extends JpaRepository<NotebookEntity,String>, JpaSpecificationExecutor<NotebookEntity> {
     Page<NotebookEntity> findByExecuteDateBetween(Date start, Date end, Pageable pageable);
}
