package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@Slf4j
@ComponentScan(value = "com.alibaba.druid.extend")
@EnableConfigurationProperties({ServerInfoProperties.class})
public class DruidExtendAutoConfig {


    DruidExtendAutoConfig(ServerInfoProperties serverInfoProperties) {
        try {
            DruidExtendStatLogger.setServerInfo(serverInfoProperties);
            log.info("Druid Extend Init...");
        } catch (IllegalArgumentException e) {
            log.error("redisTemplate can not null");
        }

    }

}
