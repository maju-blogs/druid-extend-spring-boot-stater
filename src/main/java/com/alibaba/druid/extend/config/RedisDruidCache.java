package com.alibaba.druid.extend.config;

import com.alibaba.druid.extend.properties.ServerInfoProperties;

import java.util.List;

public interface RedisDruidCache {
    public final String SERVER_NAME = "DRUID_EXTEND:SERVER_NAME:";
    public final String SERVER_DATA = "DRUID_EXTEND:DATA:";

    void putServerInfo(String key, String value);

    void putLogger(String key, String value);

    List<ServerInfoProperties> getAllServeInfo();

    void remove(String key);

    void clearAll();

    List<String>  getSqlByServerName(String serverName);

    List<String>  getWebUriByServerName(String serverName);

}
