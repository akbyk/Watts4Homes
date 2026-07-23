package com.akbyk.watts4homes.sensors.simulation;

import com.akbyk.watts4homes.sensors.registry.SimulatedAppliance;
import com.akbyk.watts4homes.sensors.registry.SimulatedHome;
import com.akbyk.watts4homes.sensors.registry.SimulationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetrySimulationScheduler {

    private final SimulationRegistry registry;

    // Placeholder loop only  real wattage generation + Kafka publish will be implemented
    @Scheduled(fixedRate = 5000)
    public void tick() {
        if (registry.size() == 0) {
            log.info("Simulation tick: no homes registered yet");
            return;
        }
        for (SimulatedHome home : registry.allHomes()) {
            for (SimulatedAppliance appliance : home.getAppliances()) {
                log.info("[STUB READING] home={} appliance={} ({}) -> would generate a wattage reading here",
                        home.getHomeId(), appliance.getApplianceId(), appliance.getName());
            }
        }
    }
}