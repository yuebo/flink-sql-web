package com.eappcat.flink.sql.web.flink.job;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.core.execution.JobListener;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Flink Job回调类，用于获取JobId
 */
@Slf4j
public class JobListenerImpl implements JobListener {
    private static ThreadLocal<List<String>> cache =new ThreadLocal<>();
    @Override
    public void onJobSubmitted(@Nullable JobClient jobClient, @Nullable Throwable throwable) {
        cache.get().add(jobClient.getJobID().toHexString());
        log.info("onJobSubmitted:{}",jobClient.getJobID().toHexString());
    }

    @Override
    public void onJobExecuted(@Nullable JobExecutionResult jobExecutionResult, @Nullable Throwable throwable) {
        log.info("onJobExecuted:");
    }
    public static void init(){
        cache.set(Lists.newArrayList());
    }
    public static void release(){
        cache.remove();
    }
    public static List<String> jobs(){
        return cache.get();
    }

}
