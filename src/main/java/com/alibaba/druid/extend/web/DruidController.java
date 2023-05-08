package com.alibaba.druid.extend.web;

import com.alibaba.druid.extend.config.RedisDruidCacheConfig;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/druid-extend/")
public class DruidController {
    @Autowired
    private RedisDruidCacheConfig redisDruidCacheConfig;

    @RequestMapping("index")
    public String index() {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("sql")
    public ResponseEntity<List<SqlDto>> getSql(String serverName) {
        List<SqlDto> sql = redisDruidCacheConfig.getRedisDruidCache().getSqlByServerName(serverName);
        return ResponseEntity.ok(sql);
    }

    @ResponseBody
    @RequestMapping("recentSql")
    public ResponseEntity<String> recentSql(String serverName) {
        String sql = redisDruidCacheConfig.getRedisDruidCache().getRecentSqlByServerName(serverName);
        return ResponseEntity.ok(sql);
    }

    @ResponseBody
    @RequestMapping("clearAll")
    public ResponseEntity<String> clearAll() {
        redisDruidCacheConfig.getRedisDruidCache().clearAll();
        return ResponseEntity.ok("Clear all data successfully");
    }

    @ResponseBody
    @RequestMapping("webUrl")
    public ResponseEntity<String> webUrl(String serverName) {
        String web = redisDruidCacheConfig.getRedisDruidCache().getWebUriByServerName(serverName);
        return ResponseEntity.ok(web);
    }

    @ResponseBody
    @RequestMapping("allServer")
    public ResponseEntity<List<ServerInfoProperties>> allServer() {
        List<ServerInfoProperties> allServeInfo = redisDruidCacheConfig.getRedisDruidCache().getAllServeInfo();
        return ResponseEntity.ok(allServeInfo);
    }
}
