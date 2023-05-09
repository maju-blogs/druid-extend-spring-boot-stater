package com.alibaba.druid.extend.properties;

import lombok.Data;

@Data
public class SqlDto {
    private long EffectedRowCount;

    private long ConcurrentMax;

    private long TotalTime;

    private long InTransactionCount;

    private String sqlMD5;

    private long FetchRowCount;

    private long RunningCount;

    private long id;

    private long ErrorCount;

    private long ExecuteCount;

    private long MaxTimespan;

    private String SQL;
}
