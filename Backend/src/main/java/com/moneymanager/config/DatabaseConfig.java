package com.moneymanager.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;

@Configuration
public class DatabaseConfig {

    private static final Logger log = LoggerFactory.getLogger(DatabaseConfig.class);

    @Bean
    @Primary
    public DataSource dataSource() {
        // Read directly from Render environment variables
        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = System.getProperty("DATABASE_URL");
        }

        String username = System.getenv("DATABASE_USERNAME");
        String password = System.getenv("DATABASE_PASSWORD");

        log.info("==== DATABASE INITIALIZATION ====");

        if (dbUrl == null || dbUrl.isBlank()) {
            log.error("FATAL: DATABASE_URL environment variable is completely empty or not set in Render!");
            throw new IllegalStateException("DATABASE_URL environment variable is missing in Render. Please set DATABASE_URL in Render Dashboard -> Environment!");
        }

        dbUrl = dbUrl.trim();
        // Remove surrounding quotes if user pasted with quotes
        if (dbUrl.startsWith("\"") && dbUrl.endsWith("\"")) {
            dbUrl = dbUrl.substring(1, dbUrl.length() - 1).trim();
        }
        if (dbUrl.startsWith("'") && dbUrl.endsWith("'")) {
            dbUrl = dbUrl.substring(1, dbUrl.length() - 1).trim();
        }

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.postgresql.Driver");

        // Format 1: URI format: postgresql://username:password@host:port/database or postgres://...
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

                log.info("Connecting to Neon host: {} port: {} database: {}", host, port, database);

                if (uri.getUserInfo() != null && !uri.getUserInfo().isBlank()) {
                    String[] userParts = uri.getUserInfo().split(":", 2);
                    config.setUsername(userParts[0]);
                    if (userParts.length > 1) {
                        config.setPassword(userParts[1]);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not parse as URI, attempting direct jdbc: prefix: {}", e.getMessage());
                config.setJdbcUrl(dbUrl.startsWith("jdbc:") ? dbUrl : "jdbc:" + dbUrl);
            }
        } 
        // Format 2: Already JDBC format: jdbc:postgresql://host:port/database...
        else if (dbUrl.startsWith("jdbc:postgresql://")) {
            if (!dbUrl.contains("sslmode=")) {
                dbUrl += (dbUrl.contains("?") ? "&" : "?") + "sslmode=require";
            }
            config.setJdbcUrl(dbUrl);
            log.info("Using standard JDBC URL with sslmode=require");
        } else {
            config.setJdbcUrl("jdbc:postgresql://" + dbUrl);
        }

        if (username != null && !username.isBlank()) {
            config.setUsername(username);
        }
        if (password != null && !password.isBlank()) {
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
