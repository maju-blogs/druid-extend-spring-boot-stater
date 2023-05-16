package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import com.alibaba.druid.extend.util.DateUtil;
import com.alibaba.druid.pool.DruidDataSourceStatLogger;
import com.alibaba.druid.pool.DruidDataSourceStatLoggerImpl;
import com.alibaba.druid.pool.DruidDataSourceStatValue;
import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.stat.JdbcSqlStatValue;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DruidExtendStatLogger extends DruidDataSourceStatLoggerImpl implements DruidDataSourceStatLogger {

    private static ServerInfoProperties serverInfoProperties;

    private DruidStatService druidStatService;
    private RedisDruidCache redisDruidCache = RedisDruidCacheConfig.getInstance();

    private static final String NOT_COUNT_SQL = "SELECT 1 FROM DUAL|/\\* ping \\*/ SELECT 1";

    @Override
    public void log(DruidDataSourceStatValue statValue) {
        try {
            if (null == druidStatService) {
                druidStatService = DruidStatService.getInstance();
            }
            if (null == redisDruidCache) {
                redisDruidCache = RedisDruidCacheConfig.getInstance();
            }
            String appName = "";
            if (null != serverInfoProperties) {
                appName = serverInfoProperties.getName();
            }
            if (null != statValue && !CollectionUtils.isEmpty(statValue.getSqlList())) {
                List<JdbcSqlStatValue> jdbcSqlStatValueStream = statValue.getSqlList().stream().filter(item -> !item.getSql().matches(NOT_COUNT_SQL)).collect(Collectors.toList());
                if (CollectionUtils.isEmpty(jdbcSqlStatValueStream)) {
                    return;
                }
                String weburi = druidStatService.service("/weburi.json");
                log.debug("DRUID_SQL_STAT_LOGGER:APP:{},{}", appName, JSON.toJSONString(jdbcSqlStatValueStream, JSONWriter.Feature.FieldBased));
                log.debug("DRUID_URI_STAT_LOGGER:APP:{},{}", appName, weburi);
                JSONArray array = new JSONArray();
                for (JdbcSqlStatValue value : jdbcSqlStatValueStream) {
                    SqlDto dto = new SqlDto();
                    dto.setId(value.getId());
                    dto.setSQL(value.getSql());
                    dto.setSqlMD5(DigestUtils.md5DigestAsHex(value.getSql().getBytes()));
                    dto.setExecuteCount(value.getExecuteCount());
                    dto.setTotalTime(value.getExecuteMillisTotal());
                    dto.setMaxTimespan(value.getExecuteMillisMax());
                    dto.setInTransactionCount(value.getInTransactionCount());
                    dto.setErrorCount(value.getExecuteErrorCount());
                    dto.setEffectedRowCount(value.getUpdateCount());
                    dto.setFetchRowCount(value.getFetchRowCount());
                    dto.setRunningCount(value.getRunningCount());
                    dto.setConcurrentMax(value.getConcurrentMax());
                    array.add(dto);
                }
                JSONObject SQL = new JSONObject();
                SQL.put("Content", array);
                SQL.put("ResultCode", 1);
                if (null != redisDruidCache) {
                    redisDruidCache.putLogger(appName + ":SQL", DateUtil.formatDateTime(new Date()), JSON.toJSONString(SQL));
                    redisDruidCache.clearSql(appName);
                }
            }
        } catch (Exception e) {
            log.warn("logDruidStatException", e.getMessage());
        }


    }

    public static void setServerInfo(ServerInfoProperties serverInfo) {
        serverInfoProperties = serverInfo;
    }

}
