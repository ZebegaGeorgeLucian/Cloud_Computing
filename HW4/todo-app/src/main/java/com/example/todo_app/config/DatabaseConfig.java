package com.example.todo_app.config;

import com.example.todo_app.service.KeyVaultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Autowired
    private KeyVaultService keyVaultService;

    @Bean
    public DataSource dataSource() {
        // Fetch the JDBC connection string from Key Vault
        String dbUrl = keyVaultService.getSecret("sql-database-connection-string");
        return DataSourceBuilder.create().url(dbUrl).build();
    }
}