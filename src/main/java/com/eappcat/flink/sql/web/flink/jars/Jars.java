package com.eappcat.flink.sql.web.flink.jars;

import lombok.Data;

import java.util.List;
/**
 * Flink Rest Aoi对应的数据格式
 * @author Xuan Yue Bo
 */
@Data
public class Jars {
    private String address;
    private List<JarItem> files;
}
