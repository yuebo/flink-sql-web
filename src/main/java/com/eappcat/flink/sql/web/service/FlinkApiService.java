package com.eappcat.flink.sql.web.service;

import com.eappcat.flink.sql.web.flink.jars.Jars;
import com.eappcat.flink.sql.web.flink.jars.RunJarResponse;
import com.eappcat.flink.sql.web.flink.job.Job;
import com.eappcat.flink.sql.web.flink.job.Jobs;
import com.eappcat.flink.sql.web.flink.job.Metric;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * flink web ui对应的接口调用
 * @author Xuan Yue Bo
 */
@FeignClient(name = "flink",url = "${flink.url}",decode404 = true)
public interface FlinkApiService {
    @GetMapping("/jobs")
    Jobs jobs();

    @GetMapping("/jobs/{id}")
    Job job(@PathVariable("id")String id);

    @GetMapping("jobs/{id}/vertices/{vid}/metrics")
    List<Metric> verticeMetric(@PathVariable("id")String id,@PathVariable("vid")String vid);

    @GetMapping("jobs/{id}/vertices/{vid}/metrics")
    List<Metric> verticeMetric(@PathVariable("id")String id,@PathVariable("vid")String vid,@RequestParam("get")String name);

    @PostMapping("/jars/{id}/run")
    RunJarResponse runJob(@PathVariable("id")String jarId, @RequestParam("entity-class") String entityClass, @RequestParam("programArg")String args);

    @GetMapping("/jars")
    Jars jars();

    @GetMapping("/jobs/{id}/yarn-cancel")
    void cancelJob(@PathVariable("id")String jobId);

}
