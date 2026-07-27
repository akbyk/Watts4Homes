package com.akbyk.watts4homes.core.homes.dto;

import java.util.List;

public record HomeStatusResponse(
        Long homeId,
        String homeName,
        double accumulatedUsage,
        double accumulatedCost,
        String tariffState,
        double budgetQuota,
        List<ApplianceStatus> appliances
) {
    public record ApplianceStatus(
            Long applianceId,
            String name,
            String type,
            double safeLimitWatts,
            int consecutiveBreachCount,
            String status
    ) {}
}