package com.alibaba.druid.extend.impl;

import com.alibaba.druid.extend.config.RedisDruidCache;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RedisDruidCacheImpl implements RedisDruidCache {
    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public void putServerInfo(String key, String value) {
        redisTemplate.opsForValue().set(RedisDruidCache.SERVER_NAME + key, value);
    }

    @Override
    public void putLogger(String key, String value) {
        redisTemplate.opsForList().rightPush(RedisDruidCache.SERVER_DATA + key, value);
    }

    @Override
    public List<ServerInfoProperties> getAllServeInfo() {
        Set<String> keys = redisTemplate.keys(RedisDruidCache.SERVER_NAME + "*");
        List<ServerInfoProperties> serverInfos = new ArrayList<>();
        if (null != keys && keys.size() > 0) {
            for (String key : keys) {
                ServerInfoProperties serverInfo = JSON.parseObject(redisTemplate.opsForValue().get(key).toString(), ServerInfoProperties.class);
                serverInfos.add(serverInfo);
            }
        }
        return serverInfos;
    }

    @Override
    public void remove(String key) {
        redisTemplate.delete(RedisDruidCache.SERVER_NAME + key);
    }

    @Override
    public void clearAll() {
        Set<String> keys = redisTemplate.keys(RedisDruidCache.SERVER_DATA + "*");
        if (null != keys && keys.size() > 0) {
            redisTemplate.delete(keys);
        }

    }

    @Override
    public List<String> getSqlByServerName(String serverName) {
        List<String> range = redisTemplate.opsForList().range(RedisDruidCache.SERVER_DATA + serverName + ":SQL", 0, -1);
        return range;
    }

    @Override
    public List<String> getWebUriByServerName(String serverName) {
        List<String> range = redisTemplate.opsForList().range(RedisDruidCache.SERVER_DATA + serverName + ":URI", 0, -1);
        return range;
    }
}
