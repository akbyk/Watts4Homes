package com.akbyk.watts4homes.core.homes;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "homes")
@Getter
@Setter
@NoArgsConstructor
public class Home {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(name = "budget_quota", nullable = false)
    private BigDecimal budgetQuota;

    @Column(name = "current_rate", nullable = false)
    private BigDecimal currentRate;

    @Column(name = "penalty_rate", nullable = false)
    private BigDecimal penaltyRate;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @OneToMany(mappedBy = "home", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Appliance> appliances = new ArrayList<>();

    public void addAppliance(Appliance appliance) {
        appliance.setHome(this);
        this.appliances.add(appliance);
    }
}