package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.config.RedisDruidCache;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.util.DateUtil;
import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.DigestUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

@Configuration
@EnableScheduling
@Slf4j
@DependsOn(value = "redisDruidCacheImpl")
public class PullDataExecuter {
    private DruidStatService druidStatService;

    private RedisDruidCache redisDruidCache;

    @Resource
    private ServerInfoProperties serverInfoProperties;


    ServerInfoProperties infoProperties;

    String startTime = DateUtil.formatDateTime(new Date());

    @PostConstruct
    public void countData() {

    }


    @Scheduled(cron = "0/10 * * * * ?")
    public void pullData() {
        if (null == druidStatService) {
            druidStatService = DruidStatService.getInstance();
        }
        if (null == redisDruidCache) {
            redisDruidCache = RedisDruidCacheConfig.getInstance();
        }
        if (null == redisDruidCache) {
            log.warn("config name is null");
            return;
        }
        String weburi = druidStatService.service("/weburi.json");
        String sql = druidStatService.service("/sql.json");
        if (weburi.length() > 50) {
            redisDruidCache.putLogger(serverInfoProperties.getName() + ":URI", startTime, weburi);
        }
        if (sql.length() > 50) {
            JSONObject object = JSONObject.parseObject(sql);
            JSONArray array = object.getJSONArray("Content");
            if (null != array && array.size() > 0) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    if (null != obj.get("SQL")) {
                        obj.put("sqlMD5", DigestUtils.md5DigestAsHex(obj.getString("SQL").getBytes()));
                    }
                }
            }
            redisDruidCache.putLogger(serverInfoProperties.getName() + ":SQL", startTime, object.toString());
        }
        if (null == infoProperties || !redisDruidCache.hasServer(serverInfoProperties.getName())) {
            infoProperties = new ServerInfoProperties();
            infoProperties.setIp(getIP());
            infoProperties.setName(serverInfoProperties.getName());
            infoProperties.setPort(serverInfoProperties.getPort());
            infoProperties.setNumber(null);
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

    @Scheduled(cron = "0 * * * * ?")
    public void clearOld() {
        redisDruidCache.clearOld();
    }
}
