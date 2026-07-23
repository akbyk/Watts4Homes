package com.akbyk.watts4homes.core.config;

import com.akbyk.watts4homes.core.rules.ApplianceBreachState;
import com.akbyk.watts4homes.core.rules.HomeState;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.table.KeyValueView;
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

    @Bean
    public KeyValueView<Long, HomeState> homeStateView(IgniteClient client) {
        return client.tables()
                .table(HOME_STATE_TABLE)
                .keyValueView(Long.class, HomeState.class);
    }

    @Bean
    public KeyValueView<String, ApplianceBreachState> applianceBreachView(IgniteClient client) {
        return client.tables()
                .table(APPLIANCE_BREACH_TABLE)
                .keyValueView(String.class, ApplianceBreachState.class);
    }
}