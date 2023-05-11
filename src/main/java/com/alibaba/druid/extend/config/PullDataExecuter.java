package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Configuration
@EnableScheduling
@Slf4j
@DependsOn(value = "redisDruidCacheImpl")
public class PullDataExecuter {

    private RedisDruidCache redisDruidCache;

    @Resource
    private ServerInfoProperties serverInfoProperties;


    ServerInfoProperties infoProperties;


    @PostConstruct
    public void countData() {

    }


    @Scheduled(cron = "0/5 * * * * ?")
    public void pullData() {
        if (null == redisDruidCache) {
            redisDruidCache = RedisDruidCacheConfig.getInstance();
        }
        if (null == redisDruidCache) {
            log.warn("config name is null");
            return;
        }
        redisDruidCache.pullData();
        if (null == infoProperties || !redisDruidCache.hasServer(serverInfoProperties.getName())) {
            infoProperties = new ServerInfoProperties();
            infoProperties.setIp(getIP());
            infoProperties.setName(serverInfoProperties.getName());
            infoProperties.setPort(serverInfoProperties.getPort());
            redisDruidCache.putServerInfo(serverInfoProperties.getName(), JSON.toJSONString(infoProperties));
        }
    }

    public String getIP() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("get ip error");
        }
        return "";
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void clearOld() {
        redisDruidCache.clearOld(null);
    }
}
