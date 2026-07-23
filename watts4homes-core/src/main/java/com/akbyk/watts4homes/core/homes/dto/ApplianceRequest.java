package com.akbyk.watts4homes.core.homes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ApplianceRequest(
        @NotBlank String name,
        @NotBlank String type,
        @Positive Double safeLimitWatts
) {}