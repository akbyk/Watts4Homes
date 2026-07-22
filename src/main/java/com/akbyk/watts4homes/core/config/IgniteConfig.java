package com.akbyk.watts4homes.core.config;

import org.apache.ignite.Ignition;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.configuration.ClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IgniteConfig {

    public static final String HOME_STATE_CACHE = "home-state";
    public static final String APPLIANCE_BREACH_CACHE = "appliance-breach";

    @Value("${watts4homes.ignite.address}")
    private String igniteAddress;

    @Bean
    public IgniteClient igniteClient() {
        ClientConfiguration cfg = new ClientConfiguration()
                .setAddresses(igniteAddress);
        return Ignition.startClient(cfg);
    }
}