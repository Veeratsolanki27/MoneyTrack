package com.moneymanager.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url:}")
    private String rawUrl;

    @Value("${spring.datasource.username:}")
    private String defaultUsername;

    @Value("${spring.datasource.password:}")
    private String defaultPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();

        // Handle standard Render / Neon "postgresql://user:password@host/dbname" URLs
        if (rawUrl != null && (rawUrl.startsWith("postgres://") || rawUrl.startsWith("postgresql://"))) {
            try {
                String cleanUrl = rawUrl.replace("postgresql://", "http://").replace("postgres://", "http://");
                URI uri = new URI(cleanUrl);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String database = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
                ds.setJdbcUrl(jdbcUrl);

                if (uri.getUserInfo() != null) {
                    String[] userParts = uri.getUserInfo().split(":");
                    ds.setUsername(userParts[0]);
                    if (userParts.length > 1) {
                        ds.setPassword(userParts[1]);
                    }
                }
            } catch (Exception e) {
                ds.setJdbcUrl(rawUrl);
                ds.setUsername(defaultUsername);
                ds.setPassword(defaultPassword);
            }
        } else {
            // Already standard JDBC format (jdbc:postgresql://...)
            ds.setJdbcUrl(rawUrl);
            ds.setUsername(defaultUsername);
            ds.setPassword(defaultPassword);
        }

        ds.setDriverClassName("org.postgresql.Driver");
        ds.setMaximumPoolSize(5);
        ds.setMinimumIdle(1);
        ds.setConnectionTimeout(30000);
        ds.setIdleTimeout(30000);
        ds.setMaxLifetime(60000);

        return ds;
    }
}

