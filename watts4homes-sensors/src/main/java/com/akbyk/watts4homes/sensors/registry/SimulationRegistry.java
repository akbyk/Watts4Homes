package com.akbyk.watts4homes.sensors.registry;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of homes currently being simulated.
 */
@Component
public class SimulationRegistry {

    private final ConcurrentHashMap<Long, SimulatedHome> homesById = new ConcurrentHashMap<>();

    public void register(SimulatedHome home) {
        homesById.put(home.getHomeId(), home);
    }

    public Collection<SimulatedHome> allHomes() {
        return homesById.values();
    }

    public int size() {
        return homesById.size();
    }
}