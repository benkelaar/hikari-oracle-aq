package com.bartenkelaar.hikari;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.nativejdbc.NativeJdbcExtractor;
import org.springframework.jdbc.support.nativejdbc.SimpleNativeJdbcExtractor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Configuration of Hikari datasource to Oracle DB
 */
@Configuration
@EnableTransactionManagement
public class DataSourceConfig {
    @Bean
    public NativeJdbcExtractor connectionWrapper() {
        SimpleNativeJdbcExtractor jdbcExtractor = new SimpleNativeJdbcExtractor();
        jdbcExtractor.setNativeConnectionNecessaryForNativeCallableStatements(true);
        jdbcExtractor.setNativeConnectionNecessaryForNativePreparedStatements(true);
        jdbcExtractor.setNativeConnectionNecessaryForNativeStatements(true);
        return jdbcExtractor;
    }

    @Bean
    public DataSource dataSource(NativeJdbcExtractor extractor,
                                 @Value("${database.url}") String jdbcUrl,
                                 @Value("${database.user}") String user,
                                 @Value("${database.password}") String password) throws SQLException {
        HikariDataSource ds = new HikariWrappingDataSource(extractor);
        ds.setDriverClassName("oracle.jdbc.driver.OracleDriver");
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(user);
        ds.setPassword(password);

        ds.setAutoCommit(true);
        ds.setMinimumIdle(2);
        ds.setMaximumPoolSize(10);
        ds.setConnectionTimeout(1000);
        return ds;
    }

    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) throws SQLException {
        return new DataSourceTransactionManager(dataSource);
    }
}
