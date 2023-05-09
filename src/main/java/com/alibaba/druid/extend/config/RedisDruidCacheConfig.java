package com.alibaba.druid.extend.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RedisDruidCacheConfig implements InitializingBean, ApplicationContextAware {
    private static Map<String, RedisDruidCache> queryServiceImplMap = new HashMap<>();
    private ApplicationContext applicationContext;

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, RedisDruidCache> beanMap = applicationContext.getBeansOfType(RedisDruidCache.class);
        for (RedisDruidCache serviceImpl : beanMap.values()) {
            queryServiceImplMap.put(serviceImpl.getClass().getSimpleName(), serviceImpl);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static RedisDruidCache getInstance() {
        for (Map.Entry<String, RedisDruidCache> entry : queryServiceImplMap.entrySet()) {
            if (!entry.getKey().equals("redisDruidCache")) {
                return entry.getValue();
            }
        }
        return queryServiceImplMap.get("redisDruidCache");
    }

    public static void setInstance(RedisDruidCache redisDruidCache) {
        queryServiceImplMap.put("redisDruidCacheImpl", redisDruidCache);
    }
}
