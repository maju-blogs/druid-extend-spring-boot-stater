package com.alibaba.druid.extend.impl;

import com.alibaba.druid.extend.config.RedisDruidCache;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    public List<SqlDto> getSqlByServerName(String serverName) {
        List<String> range = redisTemplate.opsForList().range(RedisDruidCache.SERVER_DATA + serverName + ":SQL", 0, -1);
        List<SqlDto> sqlDtos = new ArrayList<>();
        for (String s : range) {
            try {
                List<SqlDto> tmp = JSON.parseArray(s, SqlDto.class);
                sqlDtos.addAll(tmp);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        List<SqlDto> result = new ArrayList<>();
        Map<String, List<SqlDto>> collect = sqlDtos.stream().collect(Collectors.groupingBy(SqlDto::getSqlMD5));
        for (Map.Entry<String, List<SqlDto>> entry : collect.entrySet()) {
            SqlDto sqlDto = new SqlDto();
            sqlDto.setSqlMD5(entry.getKey());
            sqlDto.setSql(entry.getValue().stream().findFirst().get().getSql());
            sqlDto.setConcurrentMax(entry.getValue().stream().mapToLong(SqlDto::getConcurrentMax).max().getAsLong());
            sqlDto.setUpdateCount(entry.getValue().stream().mapToLong(SqlDto::getUpdateCount).sum());
            sqlDto.setExecuteMillisTotal(entry.getValue().stream().mapToLong(SqlDto::getExecuteMillisTotal).sum());
            sqlDto.setInTransactionCount(entry.getValue().stream().mapToLong(SqlDto::getExecuteMillisTotal).sum());
            sqlDto.setFetchRowCount(entry.getValue().stream().mapToLong(SqlDto::getFetchRowCount).sum());
            sqlDto.setRunningCount(entry.getValue().stream().findFirst().get().getRunningCount());
            sqlDto.setId(entry.getValue().stream().findFirst().get().getId());
            sqlDto.setExecuteErrorCount(entry.getValue().stream().mapToLong(SqlDto::getExecuteErrorCount).sum());
            sqlDto.setExecuteCount(entry.getValue().stream().mapToLong(SqlDto::getExecuteCount).sum());
            sqlDto.setExecuteMillisMax(entry.getValue().stream().mapToLong(SqlDto::getExecuteMillisMax).max().getAsLong());
            result.add(sqlDto);

        }
        return result;
    }

    @Override
    public String getWebUriByServerName(String serverName) {
        Object range = redisTemplate.opsForList().index(RedisDruidCache.SERVER_DATA + serverName + ":URI", -1);
        if (null != range) {
            return range.toString();
        }
        return null;
    }

    @Override
    public String getRecentSqlByServerName(String serverName) {
        Object range = redisTemplate.opsForList().index(RedisDruidCache.SERVER_DATA + serverName + ":SQL", -1);
        if (null != range) {
            return range.toString();
        }
        return null;
    }
}
