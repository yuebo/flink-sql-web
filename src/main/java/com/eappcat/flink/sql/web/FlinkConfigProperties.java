package com.eappcat.flink.sql.web;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Flink 配置相关类
 * @author Xuan Yue Bo
 */
@ConfigurationProperties(prefix = "flink")
@Data
public class FlinkConfigProperties {
    /**
     * flink web ui调用地址
     */
    private String url;
    /**
     * 回调地址
     */
    private String callbackUrl;
    /**
     * flink Host地址
     */
    private String host;
    /**
     * flink rest端口
     */
    private Integer restPort;
    /**
     * flink manager端口
     */
    private Integer jobManagerPort;
    private String submitJobLocation;
}
