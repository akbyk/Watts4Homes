package com.akbyk.watts4homes.core.rules;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeState implements Serializable {
    private double accumulatedUsage;   // kWh
    private double accumulatedCost;
    private String tariffState;        // NORMAL | PENALTY
    private double budgetQuota;
    private double currentRate;
    private double penaltyRate;
    private boolean breachedEightyPercent;
    private boolean breachedHundredPercent;
}