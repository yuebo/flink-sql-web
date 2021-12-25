package com.eappcat.flink.sql.web.schedule;

import com.eappcat.flink.sql.web.service.ExecuteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 用于同步flink job和web console之间的状态
 */
@Component
public class JobStatusSyncJob {
    @Autowired
    private ExecuteService executeService;

    /**
     * 同步Flink job的状态
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 30000)
    public void update(){
        executeService.syncStatus();
    }
}
