package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import com.alibaba.druid.extend.properties.UrlDto;

import java.util.List;
import java.util.Map;

public interface RedisDruidCache {
    public final String SERVER_NAME = "DRUID_EXTEND:SERVER_NAME:";
    public final String DRUID_EXTEND = "DRUID_EXTEND:";
    public final String SERVER_DATA = "DRUID_EXTEND:DATA:";

    void putServerInfo(String key, String value);

    void putLogger(String key, String date, String value);

    Map<String, List<ServerInfoProperties>> getAllServeInfo();

    void clearOne(String key);

    void clearAll();

    void clearOld();

    List<SqlDto> getSqlByServerName(String serverName);

    List<UrlDto>  getWebUriByServerName(String serverName);

    boolean hasServer(String name);
}
