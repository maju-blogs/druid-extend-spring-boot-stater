package com.alibaba.druid.extend.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Scanner;

@Data
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerInfoProperties {
    @Value("${spring.application.name}")
    private String name;
    private String port;
    private String ip;
    @Value("${spring.datasource.druid.maxSaveTime:86400000}")
    private long maxSaveTime;

    public static String getCPUSerialNumber() {
        String next;
        try {
            Process process = Runtime.getRuntime().exec(new String[]{"wmic", "cpu", "get", "ProcessorId"});
            process.getOutputStream().close();
            Scanner sc = new Scanner(process.getInputStream());
            next = sc.next();
        } catch (IOException e) {
            throw new RuntimeException("获取CPU序列号失败");
        }
        return next;
    }
}
