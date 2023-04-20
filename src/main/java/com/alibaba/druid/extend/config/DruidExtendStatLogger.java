package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.pool.DruidDataSourceStatLogger;
import com.alibaba.druid.pool.DruidDataSourceStatLoggerImpl;
import com.alibaba.druid.pool.DruidDataSourceStatValue;
import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.druid.stat.JdbcSqlStatValue;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DruidExtendStatLogger extends DruidDataSourceStatLoggerImpl implements DruidDataSourceStatLogger {

    private static ServerInfoProperties serverInfoProperties;

    private DruidStatService druidStatService = DruidStatService.getInstance();

    private static final String NOT_COUNT_SQL = "SELECT 1 FROM DUAL|/\\* ping \\*/ SELECT 1";
    ServerInfoProperties infoProperties;

    private Date lastClearDate = new Date();

    private long clear = 24 * 60* 60 *1000;


    @Override
    public void log(DruidDataSourceStatValue statValue) {
        try {
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
                log.debug("DRUID_SQL_STAT_LOGGER:APP:{},{}", appName, jdbcSqlStatValueStream);
                log.debug("DRUID_URI_STAT_LOGGER:APP:{},{}", appName, weburi);
                if (null != serverInfoProperties.getRedisDruidCache()) {
                    serverInfoProperties.getRedisDruidCache().putLogger(appName + ":SQL", JSON.toJSONString(jdbcSqlStatValueStream,SerializerFeature.DisableCircularReferenceDetect));
                    serverInfoProperties.getRedisDruidCache().putLogger(appName + ":URI", weburi);
                    if (null == infoProperties) {
                        infoProperties = new ServerInfoProperties();
                        infoProperties.setIp(serverInfoProperties.getIp());
                        infoProperties.setName(serverInfoProperties.getName());
                        infoProperties.setPort(serverInfoProperties.getPort());
                        infoProperties.setRedisDruidCache(null);
                        serverInfoProperties.getRedisDruidCache().putServerInfo(appName, JSON.toJSONString(infoProperties));
                    }

                }
            }
            Date now = new Date();
            if (now.getTime() - lastClearDate.getTime() > clear) {
                serverInfoProperties.getRedisDruidCache().clearAll();
            }
        } catch (Exception e) {
            log.warn("logDruidStatException", e.getMessage());
        }


    }

    public static void setServerInfo(ServerInfoProperties serverInfo) {
        serverInfoProperties = serverInfo;
    }

}
