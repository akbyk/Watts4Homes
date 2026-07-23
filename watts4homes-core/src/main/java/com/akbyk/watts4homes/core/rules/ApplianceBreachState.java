package com.akbyk.watts4homes.core.rules;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplianceBreachState implements Serializable {
    private double safeLimitWatts;
    private int consecutiveBreachCount;
    private String lastStatus; // NORMAL | ANOMALOUS
}