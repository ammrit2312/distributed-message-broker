package com.broker.server;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSourceProvider {

    private final HikariDataSource dataSource;

    public DataSourceProvider(BrokerConfig config) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(config.getPostgresUrl());
        cfg.setUsername(config.getPostgresUser());
        cfg.setPassword(config.getPostgresPassword());
        cfg.setMaximumPoolSize(10);
        this.dataSource = new HikariDataSource(cfg);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

}
