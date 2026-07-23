package com.akbyk.watts4homes.sensors.event;

import java.time.Instant;

public record TelemetryReading(
        Long homeId,
        Long applianceId,
        Double watts,
        Instant timestamp
) {}