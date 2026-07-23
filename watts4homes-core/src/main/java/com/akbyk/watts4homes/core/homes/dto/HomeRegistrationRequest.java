package com.akbyk.watts4homes.core.homes.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record HomeRegistrationRequest(
        @NotBlank String name,
        String address,
        @NotBlank @Email String contactEmail,
        @Positive Double budgetQuota,
        @Positive Double currentRate,
        @Positive Double penaltyRate,
        @NotEmpty @Valid List<ApplianceRequest> appliances
) {}