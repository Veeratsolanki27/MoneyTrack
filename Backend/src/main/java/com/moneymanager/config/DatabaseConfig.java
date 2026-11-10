package com.moneymanager.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Value("${spring.datasource.url}")
    private String springUrl;

    @Value("${spring.datasource.username:}")
    private String springUsername;

    @Value("${spring.datasource.password:}")
    private String springPassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        log.info("==== DATABASE INITIALIZATION ====");

        String dbUrl = springUrl;
        String username = springUsername;
        String password = springPassword;

        // Check if environment has a direct override
        String envDb = System.getenv("DATABASE_URL");
        if (envDb != null && !envDb.isBlank()) {
            dbUrl = envDb;
        }

        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = "jdbc:postgresql://ep-proud-boat-be61gqr6-pooler.c-9.us-east-2.aws.neon.tech/neondb?sslmode=require";
        }

        dbUrl = dbUrl.trim();
        if ((dbUrl.startsWith("\"") && dbUrl.endsWith("\"")) || (dbUrl.startsWith("'") && dbUrl.endsWith("'"))) {
            dbUrl = dbUrl.substring(1, dbUrl.length() - 1).trim();
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        // Format 1: PostgreSQL URI format: postgresql://username:password@host/database
        if (dbUrl.startsWith("postgres://") || dbUrl.startsWith("postgresql://")) {
            try {
                String cleanUri = dbUrl.replaceFirst("^postgres(ql)?://", "http://");
                URI uri = new URI(cleanUri);

                String host = uri.getHost();
                int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                String path = uri.getPath();
                String database = (path != null && path.length() > 1) ? path.substring(1) : "neondb";

                String finalJdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
                config.setJdbcUrl(finalJdbcUrl);

                if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    config.setUsername(userParts[0]);
                    if (userParts.length > 1) {
                        config.setPassword(userParts[1]);
                    }
                }
            } catch (Exception e) {
                config.setJdbcUrl(dbUrl.startsWith("jdbc:") ? dbUrl : "jdbc:" + dbUrl);
            }
        } 
        // Format 2: Already JDBC format: jdbc:postgresql://...
        else if (dbUrl.startsWith("jdbc:postgresql://")) {
            if (!dbUrl.contains("sslmode=")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }
            config.setJdbcUrl(dbUrl);
        } else {
            config.setJdbcUrl("jdbc:postgresql://" + dbUrl);
        }

        if (username != null && !username.isBlank() && config.getUsername() == null) {
            config.setUsername(username);
        }
        if (password != null && !password.isBlank() && config.getPassword() == null) {
            config.setPassword(password);
        }

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(30000);
        config.setMaxLifetime(60000);

        log.info("Hikari connecting with JDBC URL: {}", config.getJdbcUrl());
        return new HikariDataSource(config);
    }
}
