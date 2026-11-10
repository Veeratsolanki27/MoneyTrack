package com.moneymanager.config;

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
        HikariDataSource ds = new HikariDataSource();

        // Check system environment variables first, then Java system properties
        String envDbUrl = System.getenv("DATABASE_URL");
        if (envDbUrl == null || envDbUrl.isBlank()) {
            envDbUrl = System.getProperty("DATABASE_URL");
        }

        String envUsername = System.getenv("DATABASE_USERNAME");
        String envPassword = System.getenv("DATABASE_PASSWORD");

        log.info("Detected DATABASE_URL environment: {}", (envDbUrl != null ? "CONFIGURED (length=" + envDbUrl.length() + ")" : "NOT SET"));

        if (envDbUrl != null && !envDbUrl.isBlank()) {
            envDbUrl = envDbUrl.trim();

            // Handle URL with or without quotes
            if (envDbUrl.startsWith("\"") && envDbUrl.endsWith("\"")) {
                envDbUrl = envDbUrl.substring(1, envDbUrl.length() - 1);
            }

            // Case A: PostgreSQL URI like postgresql://user:password@host:port/database or postgres://...
            if (envDbUrl.startsWith("postgres://") || envDbUrl.startsWith("postgresql://")) {
                try {
                    String cleanUrl = envDbUrl.replaceFirst("^postgres(ql)?://", "http://");
                    URI uri = new URI(cleanUrl);

                    String host = uri.getHost();
                    int port = uri.getPort() == -1 ? 5432 : uri.getPort();
                    String database = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();

                    String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=require";
                    ds.setJdbcUrl(jdbcUrl);
                    log.info("Configured JDBC URL: jdbc:postgresql://{}:{}/{}?sslmode=require", host, port, database);

                    if (uri.getUserInfo() != null) {
                        String[] userParts = uri.getUserInfo().split(":", 2);
                        ds.setUsername(userParts[0]);
                        if (userParts.length > 1) {
                            ds.setPassword(userParts[1]);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse postgresql URI, falling back to raw: {}", e.getMessage());
                    ds.setJdbcUrl(envDbUrl.startsWith("jdbc:") ? envDbUrl : "jdbc:" + envDbUrl);
                }
            } else if (envDbUrl.startsWith("jdbc:")) {
                // Case B: Standard JDBC URL already
                if (!envDbUrl.contains("sslmode=")) {
                    envDbUrl += (envDbUrl.contains("?") ? "&" : "?") + "sslmode=require";
                }
                ds.setJdbcUrl(envDbUrl);
            } else {
                ds.setJdbcUrl("jdbc:postgresql://" + envDbUrl);
            }

            if (envUsername != null && !envUsername.isBlank()) {
                ds.setUsername(envUsername);
            }
            if (envPassword != null && !envPassword.isBlank()) {
                ds.setPassword(envPassword);
            }
        } else {
            log.error("CRITICAL: DATABASE_URL is not set! Please configure DATABASE_URL in Render environment variables.");
            ds.setJdbcUrl("jdbc:postgresql://localhost:5432/neondb");
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
