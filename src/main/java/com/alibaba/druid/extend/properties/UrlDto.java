package com.alibaba.druid.extend.properties;

import lombok.Data;

@Data
public class UrlDto {
    private String URI;
    private long RequestCount;
    private long RequestTimeMillis;
    private long RunningCount;
    private long ConcurrentMax;
    private long JdbcExecuteCount;
    private long JdbcExecuteErrorCount;
    private long JdbcExecuteTimeMillis;
    private long JdbcCommitCount;
    private long JdbcRollbackCount;
    private long JdbcFetchRowCount;
    private long JdbcUpdateCount;
}
