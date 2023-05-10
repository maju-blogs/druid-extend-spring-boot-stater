package com.alibaba.druid.extend.impl;

import com.alibaba.druid.extend.config.RedisDruidCache;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import com.alibaba.druid.extend.properties.UrlDto;
import com.alibaba.druid.extend.util.DateUtil;
import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RedisDruidCacheImpl implements RedisDruidCache {
    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ServerInfoProperties serverInfoProperties;

    private DruidStatService druidStatService;

    String startTime = DateUtil.formatDateTime(new Date());


    @Override
    public void putServerInfo(String key, String value) {
        redisTemplate.opsForSet().add(RedisDruidCache.SERVER_NAME + key, value);
    }

    @Override
    public void putLogger(String key, String dateKey, String value) {
        redisTemplate.opsForHash().put(RedisDruidCache.SERVER_DATA + key, dateKey + "_" + serverInfoProperties.getIp(), value);
    }

    @Override
    public Map<String, List<ServerInfoProperties>> getAllServeInfo() {
        Set<String> keys = redisTemplate.keys(RedisDruidCache.SERVER_NAME + "*");
        List<ServerInfoProperties> serverInfos = new ArrayList<>();
        if (null != keys && keys.size() > 0) {
            for (String key : keys) {
                Set<String> members = redisTemplate.opsForSet().members(key);
                for (String s : members) {
                    ServerInfoProperties serverInfo = JSON.parseObject(s, ServerInfoProperties.class);
                    serverInfos.add(serverInfo);
                }
            }
        }
        Map<String, List<ServerInfoProperties>> collect = serverInfos.stream().collect(Collectors.groupingBy(ServerInfoProperties::getName));
        return collect;
    }

    @Override
    public void clearOne(String key) {
        redisTemplate.delete(RedisDruidCache.SERVER_NAME + key);
        Set<String> keys = redisTemplate.keys(RedisDruidCache.SERVER_DATA + key + "*");
        if (null != keys && keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void clearAll() {
        Set<String> keys = redisTemplate.keys(RedisDruidCache.DRUID_EXTEND + "*");
        if (null != keys && keys.size() > 0) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public void clearOld() {
        Set<String> keys = redisTemplate.keys(RedisDruidCache.SERVER_DATA + "*");
        long maxSaveTime = serverInfoProperties.getMaxSaveTime();
        Date date = new Date();
        for (String key : keys) {

            Map<String, String> entries = redisTemplate.opsForHash().entries(key);
            for (String tmpKey : entries.keySet()) {
                try {
                    String dateStr = tmpKey.split("_")[0];
                    if (date.getTime() - DateUtil.parseDateTime(dateStr).getTime() > maxSaveTime) {
                        redisTemplate.opsForHash().delete(key, tmpKey);
                    }
                } catch (Exception e) {
                    log.warn("clear data error,{}", e.getMessage());
                }
            }
        }
    }

    @Override
    public List<SqlDto> getSqlByServerName(String serverName) {
        Map<String, String> entries = redisTemplate.opsForHash().entries(RedisDruidCache.SERVER_DATA + serverName + ":SQL");
        List<SqlDto> sqlDtos = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            try {
                JSONObject object = JSONObject.parseObject(entry.getValue());
                List<SqlDto> tmp = JSON.parseArray(object.getString("Content"), SqlDto.class);
                sqlDtos.addAll(tmp);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        List<SqlDto> result = new ArrayList<>();
        Map<String, List<SqlDto>> collect = sqlDtos.stream().filter(item -> null != item.getSqlMD5()).collect(Collectors.groupingBy(SqlDto::getSqlMD5));
        for (Map.Entry<String, List<SqlDto>> entry : collect.entrySet()) {
            try {
                SqlDto sqlDto = new SqlDto();
                sqlDto.setSqlMD5(entry.getKey());
                sqlDto.setSQL(entry.getValue().stream().findFirst().get().getSQL());
                sqlDto.setConcurrentMax(entry.getValue().stream().mapToLong(SqlDto::getConcurrentMax).max().getAsLong());
                sqlDto.setEffectedRowCount(entry.getValue().stream().mapToLong(SqlDto::getEffectedRowCount).sum());
                sqlDto.setTotalTime(entry.getValue().stream().mapToLong(SqlDto::getTotalTime).sum());
                sqlDto.setInTransactionCount(entry.getValue().stream().mapToLong(SqlDto::getInTransactionCount).sum());
                sqlDto.setFetchRowCount(entry.getValue().stream().mapToLong(SqlDto::getFetchRowCount).sum());
                sqlDto.setRunningCount(entry.getValue().stream().findFirst().get().getRunningCount());
                sqlDto.setId(entry.getValue().stream().findFirst().get().getId());
                sqlDto.setErrorCount(entry.getValue().stream().mapToLong(SqlDto::getErrorCount).sum());
                sqlDto.setExecuteCount(entry.getValue().stream().mapToLong(SqlDto::getExecuteCount).sum());
                sqlDto.setMaxTimespan(entry.getValue().stream().mapToLong(SqlDto::getMaxTimespan).max().getAsLong());
                result.add(sqlDto);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public List<UrlDto> getWebUriByServerName(String serverName) {
        Map<String, String> entries = redisTemplate.opsForHash().entries(RedisDruidCache.SERVER_DATA + serverName + ":URI");
        List<UrlDto> urlDtos = new ArrayList<>();
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            try {
                JSONObject object = JSONObject.parseObject(entry.getValue());
                List<UrlDto> tmp = JSON.parseArray(object.getString("Content"), UrlDto.class);
                urlDtos.addAll(tmp);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        List<UrlDto> result = new ArrayList<>();
        Map<String, List<UrlDto>> collect = urlDtos.stream().filter(item -> null != item.getURI()).collect(Collectors.groupingBy(UrlDto::getURI));
        for (Map.Entry<String, List<UrlDto>> entry : collect.entrySet()) {
            try {
                UrlDto sqlDto = new UrlDto();
                sqlDto.setURI(entry.getValue().stream().findFirst().get().getURI());
                sqlDto.setRequestCount(entry.getValue().stream().mapToLong(UrlDto::getRequestCount).sum());
                sqlDto.setRequestTimeMillis(entry.getValue().stream().mapToLong(UrlDto::getRequestTimeMillis).sum());
                sqlDto.setRequestTimeMillisMax(entry.getValue().stream().mapToLong(UrlDto::getRequestTimeMillisMax).max().getAsLong());
                sqlDto.setRunningCount(entry.getValue().stream().findFirst().get().getRunningCount());
                sqlDto.setConcurrentMax(entry.getValue().stream().mapToLong(UrlDto::getConcurrentMax).max().getAsLong());
                sqlDto.setJdbcExecuteCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcExecuteCount).sum());
                sqlDto.setJdbcExecuteErrorCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcExecuteErrorCount).sum());
                sqlDto.setJdbcExecuteTimeMillis(entry.getValue().stream().mapToLong(UrlDto::getJdbcExecuteTimeMillis).sum());
                sqlDto.setJdbcCommitCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcCommitCount).sum());
                sqlDto.setJdbcRollbackCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcRollbackCount).sum());
                sqlDto.setJdbcFetchRowCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcFetchRowCount).sum());
                sqlDto.setJdbcUpdateCount(entry.getValue().stream().mapToLong(UrlDto::getJdbcUpdateCount).sum());
                result.add(sqlDto);
            } catch (Exception e) {
                log.warn(e.getMessage());
            }
        }
        return result;
    }

    @Override
    public boolean hasServer(String name) {
        return redisTemplate.hasKey(RedisDruidCache.SERVER_NAME + name);
    }

    @Override
    public void pullData() {
        if (null == druidStatService) {
            druidStatService = DruidStatService.getInstance();
        }
        String weburi = druidStatService.service("/weburi.json");
        String sql = druidStatService.service("/sql.json");
        if (weburi.length() > 50) {
            this.putLogger(serverInfoProperties.getName() + ":URI", startTime, weburi);
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
            this.putLogger(serverInfoProperties.getName() + ":SQL", startTime, object.toString());
        }
    }

    @Override
    public void clearSql(String key) {
        redisTemplate.opsForHash().delete(RedisDruidCache.SERVER_DATA + key + ":SQL", startTime);
    }
}
