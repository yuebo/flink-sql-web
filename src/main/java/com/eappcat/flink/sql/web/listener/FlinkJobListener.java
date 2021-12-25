package com.eappcat.flink.sql.web.listener;

import com.eappcat.flink.sql.web.service.ExecuteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Flink metric消息的监听器
 * 此处用于更新响应的数量
 * @author Xuan Yue Bo
 */
@Component
@Slf4j
public class FlinkJobListener {
    public static final Pattern METRIC_PATTERN=Pattern.compile("^([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+)\\.([a-z]+)\\.([0-9a-zA-Z]+)\\.(.+)\\.([0-9a-zA-Z]+\\.[a-zA-Z]+)$");

    @Autowired
    private ExecuteService executeService;

    @KafkaListener(topics = "flink-metrics")
    public void onMessage(String message){
        log.debug("{}",message);
        executeService.onMetricEvent(message);
    }
}
