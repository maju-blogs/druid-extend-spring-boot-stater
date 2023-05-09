package com.alibaba.druid.extend.web;

import com.alibaba.druid.extend.config.RedisDruidCacheConfig;
import com.alibaba.druid.extend.properties.ServerInfoProperties;
import com.alibaba.druid.extend.properties.SqlDto;
import com.alibaba.druid.extend.properties.UrlDto;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/druid-extend/")
public class DruidController {
    @Autowired
    private RedisDruidCacheConfig redisDruidCacheConfig;

    @RequestMapping({"index", "/"})
    public String index() {
        return "index.html";
    }

    @ResponseBody
    @RequestMapping("sql")
    public ResponseEntity<JSONObject> getSql(String serverName) {
        List<SqlDto> sql = redisDruidCacheConfig.getInstance().getSqlByServerName(serverName);
        JSONObject result = new JSONObject();
        result.put("ResultCode", 1);
        result.put("Content", JSON.toJSON(sql, JSONWriter.Feature.FieldBased));
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @RequestMapping("clearAll")
    public ResponseEntity<String> clearAll() {
        redisDruidCacheConfig.getInstance().clearAll();
        return ResponseEntity.ok("Clear all data successfully");
    }

    @ResponseBody
    @RequestMapping("clearOne")
    public ResponseEntity<String> clearOne(String name) {
        redisDruidCacheConfig.getInstance().clearOne(name);
        return ResponseEntity.ok("Clear one data successfully");
    }

    @ResponseBody
    @RequestMapping("clearOld")
    public ResponseEntity<String> clearOld() {
        redisDruidCacheConfig.getInstance().clearOld();
        return ResponseEntity.ok("Clear old data successfully");
    }

    @ResponseBody
    @RequestMapping("webUrl")
    public ResponseEntity<JSONObject> webUrl(String serverName) {
        List<UrlDto> web = redisDruidCacheConfig.getInstance().getWebUriByServerName(serverName);
        JSONObject result = new JSONObject();
        result.put("ResultCode", 1);
        result.put("Content", JSON.toJSON(web, JSONWriter.Feature.FieldBased));
        return ResponseEntity.ok(result);
    }

    @ResponseBody
    @RequestMapping("allServer")
    public ResponseEntity<Map<String, List<ServerInfoProperties>>> allServer() {
        Map<String, List<ServerInfoProperties>> allServeInfo = redisDruidCacheConfig.getInstance().getAllServeInfo();
        return ResponseEntity.ok(allServeInfo);
    }

    @ResponseBody
    @RequestMapping("pullData")
    public ResponseEntity<String> pullData() {
        redisDruidCacheConfig.getInstance().pullData();
        return ResponseEntity.ok().build();
    }
}
