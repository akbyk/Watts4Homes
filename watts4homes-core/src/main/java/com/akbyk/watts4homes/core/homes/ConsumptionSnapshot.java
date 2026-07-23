package com.akbyk.watts4homes.core.homes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "consumption_snapshots")
@Getter
@Setter
@NoArgsConstructor
public class ConsumptionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_id", nullable = false)
    private Long homeId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_usage", nullable = false)
    private double totalUsage;

    @Column(name = "total_cost", nullable = false)
    private double totalCost;
}