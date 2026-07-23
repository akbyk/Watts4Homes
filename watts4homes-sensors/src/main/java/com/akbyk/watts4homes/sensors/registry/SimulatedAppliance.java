package com.akbyk.watts4homes.sensors.registry;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class SimulatedAppliance {
    private final Long applianceId;
    private final String name;
    private final String type;
    private final Double safeLimitWatts;
    private double currentWatts;

    public SimulatedAppliance(Long applianceId, String name, String type, Double safeLimitWatts) {
        this.applianceId = applianceId;
        this.name = name;
        this.type = type;
        this.safeLimitWatts = safeLimitWatts;
        this.currentWatts = 50 + ThreadLocalRandom.current().nextDouble() * 250; // 50W - 300W
    }
}