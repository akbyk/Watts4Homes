package com.akbyk.watts4homes.core.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeState implements Serializable {
    private double accumulatedUsage;
    private double accumulatedCost;
    private String tariffState;
    private double budgetQuota;
    private double currentRate;
    private double penaltyRate;
    private boolean breachedEightyPercent;
    private boolean breachedHundredPercent;
    private List<Long> applianceIds; // cached once at init, so status reads never hit Postgres
    private String applianceIdsCsv;
}