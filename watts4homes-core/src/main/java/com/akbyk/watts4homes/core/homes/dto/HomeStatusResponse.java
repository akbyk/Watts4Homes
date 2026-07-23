package com.akbyk.watts4homes.core.homes.dto;

import java.util.List;

public record HomeStatusResponse(
        Long homeId,
        double accumulatedUsage,
        double accumulatedCost,
        String tariffState,
        double budgetQuota,
        List<ApplianceStatus> appliances
) {
    public record ApplianceStatus(Long applianceId, double safeLimitWatts, int consecutiveBreachCount, String status) {}
}