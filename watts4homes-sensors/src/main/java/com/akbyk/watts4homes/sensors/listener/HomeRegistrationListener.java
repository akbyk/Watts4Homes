package com.akbyk.watts4homes.sensors.listener;

import com.akbyk.watts4homes.sensors.event.HomeRegisteredEvent;
import com.akbyk.watts4homes.sensors.registry.SimulatedAppliance;
import com.akbyk.watts4homes.sensors.registry.SimulatedHome;
import com.akbyk.watts4homes.sensors.registry.SimulationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeRegistrationListener {

    private final SimulationRegistry registry;

    @KafkaListener(topics = "home-registration", groupId = "voltwise-sensors")
    public void onHomeRegistered(HomeRegisteredEvent event) {
        List<SimulatedAppliance> appliances = event.appliances().stream()
                .map(a -> new SimulatedAppliance(a.applianceId(), a.name(), a.type(), a.safeLimitWatts()))
                .toList();

        SimulatedHome home = new SimulatedHome(event.homeId(), event.name(), appliances);
        registry.register(home);

        log.info("Registered home '{}' (id={}) into simulation registry with {} appliances",
                home.getName(), home.getHomeId(), appliances.size());
    }
}