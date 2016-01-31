package com.bartenkelaar.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link HikariDataSource} that wraps its {@link Connection}s
 * via a {@link NativeJdbcExtractor} to allow the oracle code
 * to cast created {@code Connection}s to {@link oracle.jdbc.OracleConnection} and
 * {@link oracle.jdbc.internal.OracleConnection}.
 * <p />
 * Inspired by <a href="http://docs.spring.io/spring-data/data-jdbc/docs/current/reference/html/orcl.streamsaq.html">
 *     Spring Oracle AQ documentation</a> (chapter 5.3)
 */

public class HikariWrappingDataSource extends HikariDataSource {
    private final NativeJdbcExtractor extractor;

    public HikariWrappingDataSource(NativeJdbcExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return extractor.getNativeConnection(super.getConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return extractor.getNativeConnection(super.getConnection(username, password));
    }
}
