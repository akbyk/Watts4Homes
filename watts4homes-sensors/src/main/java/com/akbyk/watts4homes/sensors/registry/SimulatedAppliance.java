package com.akbyk.watts4homes.sensors.registry;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SimulatedAppliance {
    private final Long applianceId;
    private final String name;
    private final String type;
    private final Double safeLimitWatts;

    public SimulatedAppliance(Long applianceId, String name, String type, Double safeLimitWatts) {
        this.applianceId = applianceId;
        this.name = name;
        this.type = type;
        this.safeLimitWatts = safeLimitWatts;
    }
}