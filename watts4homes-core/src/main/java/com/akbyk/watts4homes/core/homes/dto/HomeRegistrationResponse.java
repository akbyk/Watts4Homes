package com.akbyk.watts4homes.core.homes.dto;

import java.util.List;

public record HomeRegistrationResponse(
        Long homeId,
        String name,
        String contactEmail,
        List<ApplianceResponse> appliances
) {
    public record ApplianceResponse(Long applianceId, String name, String type, Double safeLimitWatts) {}
}