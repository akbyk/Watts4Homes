package com.akbyk.watts4homes.core.telemetry.event;

import java.time.Instant;

public record TelemetryReading(
        Long homeId,
        Long applianceId,
        Double watts,
        Instant timestamp
) {}