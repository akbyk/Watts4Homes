package com.akbyk.watts4homes.sensors.event;

import java.util.List;

public record HomeRegisteredEvent(
        Long homeId,
        String name,
        String contactEmail,
        Double budgetQuota,
        Double currentRate,
        Double penaltyRate,
        List<ApplianceEvent> appliances
) {
    public record ApplianceEvent(Long applianceId, String name, String type, Double safeLimitWatts) {}
}