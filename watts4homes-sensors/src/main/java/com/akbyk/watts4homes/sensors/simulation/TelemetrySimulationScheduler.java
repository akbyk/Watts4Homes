package com.akbyk.watts4homes.sensors.simulation;

import com.akbyk.watts4homes.sensors.event.TelemetryReading;
import com.akbyk.watts4homes.sensors.registry.SimulatedAppliance;
import com.akbyk.watts4homes.sensors.registry.SimulatedHome;
import com.akbyk.watts4homes.sensors.registry.SimulationRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelemetrySimulationScheduler {

    private static final String TELEMETRY_STREAM_TOPIC = "telemetry-stream";
    private static final double SPIKE_PROBABILITY = 0.05; // 5% chance per appliance per tick
    private static final double WALK_STEP_WATTS = 15.0;   // max +/- change per tick under normal conditions
    private static final double MIN_WATTS = 5.0;

    private final SimulationRegistry registry;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedRate = 5000)
    public void tick() {
        if (registry.size() == 0) {
            log.debug("Simulation tick: no homes registered yet");
            return;
        }

        for (SimulatedHome home : registry.allHomes()) {
            for (SimulatedAppliance appliance : home.getAppliances()) {
                double newWatts = nextWattReading(appliance);
                appliance.setCurrentWatts(newWatts);
                publishReading(home.getHomeId(), appliance.getApplianceId(), newWatts);
            }
        }
    }

    private double nextWattReading(SimulatedAppliance appliance) {
        double current = appliance.getCurrentWatts();

        if (ThreadLocalRandom.current().nextDouble() < SPIKE_PROBABILITY) {
            // Occasional spike: 1.3x-1.7x current draw, to exercise anomaly detection downstream.
            return current * (1.3 + ThreadLocalRandom.current().nextDouble() * 0.4);
        }

        double step = ThreadLocalRandom.current().nextDouble(-WALK_STEP_WATTS, WALK_STEP_WATTS);
        return Math.max(current + step, MIN_WATTS);
    }

    private void publishReading(Long homeId, Long applianceId, double watts) {
        TelemetryReading reading = new TelemetryReading(homeId, applianceId, watts, Instant.now());
        kafkaTemplate.send(TELEMETRY_STREAM_TOPIC, String.valueOf(homeId), reading)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish telemetry reading home={} appliance={}", homeId, applianceId, ex);
                    }
                });
    }
}