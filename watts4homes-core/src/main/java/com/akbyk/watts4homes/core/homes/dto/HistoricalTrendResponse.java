package com.akbyk.watts4homes.core.homes.dto;

import java.time.LocalDate;
import java.util.List;

public record HistoricalTrendResponse(
        Long homeId,
        List<DailyPoint> points
) {
    public record DailyPoint(LocalDate date, java.math.BigDecimal totalUsage, java.math.BigDecimal totalCost) {}
}