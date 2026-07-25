package com.akbyk.watts4homes.core.config;

import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteConfig {

    @Value("${watts4homes.ignite.address}")
    private String igniteAddress;

    @Bean
    public IgniteClient igniteClient() {
        IgniteClient client = IgniteClient.builder()
                .addresses(igniteAddress)
                .build();

        client.sql().execute(null, """
                CREATE TABLE IF NOT EXISTS home_state (
                    home_id BIGINT PRIMARY KEY,
                    accumulated_usage DOUBLE,
                    accumulated_cost DOUBLE,
                    tariff_state VARCHAR,
                    budget_quota DOUBLE,
                    current_rate DOUBLE,
                    penalty_rate DOUBLE,
                    breached_eighty_percent BOOLEAN,
                    breached_hundred_percent BOOLEAN,
                    appliance_ids VARCHAR
                )
                """);

        client.sql().execute(null, """
                CREATE TABLE IF NOT EXISTS appliance_breach (
                    breach_key VARCHAR PRIMARY KEY,
                    safe_limit_watts DOUBLE,
                    consecutive_breach_count INT,
                    last_status VARCHAR
                )
                """);

        return client;
    }
}