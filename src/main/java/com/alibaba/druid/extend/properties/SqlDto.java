package com.alibaba.druid.extend.properties;


import lombok.Data;

@Data
public class SqlDto {
    private long updateCount;

    private long concurrentMax;

    private long executeMillisTotal;

    private long inTransactionCount;

    private String sqlMD5;

    private long fetchRowCount;

    private long runningCount;

    private long id;

    private long executeErrorCount;

    private long executeCount;

    private long executeMillisMax;

    private String sql;
}
