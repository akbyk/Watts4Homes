package com.akbyk.watts4homes.core.config;

import org.apache.ignite.client.IgniteClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteConfig {

    // In Ignite 3, standard Key-Value caches are replaced by Tables.
    public static final String HOME_STATE_TABLE = "home-state";
    public static final String APPLIANCE_BREACH_TABLE = "appliance-breach";

    @Value("${watts4homes.ignite.address}")
    private String igniteAddress;

    @Bean(destroyMethod = "close")
    public IgniteClient igniteClient() {
        return IgniteClient.builder()
                .addresses(igniteAddress)
                .build();
    }
}