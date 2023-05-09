# druid-extend-spring-boot-stater

#### 介绍
基于redis拓展持久化druid监控



#### 安装教程

1.  引入依赖

```
        <dependency>
            <groupId>com.alibaba.druid.extend</groupId>
            <artifactId>druid-extend-spring-boot-stater</artifactId>
            <version>1.0</version>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <version>2.3.10.RELEASE</version>
        </dependency>
```



2.  配置redisTemplate
```
  @Bean(name = "redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        //参照StringRedisTemplate内部实现指定序列化器
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(keySerializer());
        redisTemplate.setHashKeySerializer(keySerializer());
        redisTemplate.setValueSerializer(valueSerializer());
        redisTemplate.setHashValueSerializer(valueSerializer());
        return redisTemplate;
    }

    private RedisSerializer<String> keySerializer() {
        return new StringRedisSerializer();
    }

    //使用Jackson序列化器
    private RedisSerializer<Object> valueSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }
```

3.  配置druid拓展


```
    @Bean
    @Primary
    public DruidDataSource DruidDataSource(DruidDataSource druidDataSource) {
        druidDataSource.setStatLogger(new DruidExtendStatLogger());
        return druidDataSource;
    }
```
4.  配置druid

```
spring:
  datasource:
        druid:
          stat:
            slowSqlMillis: 5000
        filters: stat,logback
        timeBetweenLogStatsMillis: 600000 #持久化到日志时间
```



#### 使用说明

1.  DruidExtendStatLogger会将druid监控数据持久化到日志和redis,日志级别为debug,DRUID_SQL_STAT_LOGGER...和DRUID_URI_STAT_LOGGER...
2.  PullDataExecuter定时任务5秒拉取监控数据，并删除过期数据，默认一天，可以根据spring.datasource.druid.maxSaveTime配置
3.  访问http://localhost:18081/druid-extend/index
![sql监控](https://foruda.gitee.com/images/1683636329803663576/b1c584f6_2147200.png "屏幕截图")
![url监控](https://foruda.gitee.com/images/1683636354017283246/84ad13df_2147200.png "屏幕截图")


