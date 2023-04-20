package com.alibaba.druid.extend.properties;

import com.alibaba.druid.extend.config.RedisDruidCache;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "server")
public class ServerInfoProperties {
    private String name;
    private String port;
    private String ip;
    private RedisDruidCache redisDruidCache;
}
