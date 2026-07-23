package com.akbyk.watts4homes.core.homes;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ConsumptionSnapshotRepository extends JpaRepository<ConsumptionSnapshot, Long> {
    List<ConsumptionSnapshot> findByHomeIdOrderByDateAsc(Long homeId);
    Optional<ConsumptionSnapshot> findByHomeIdAndDate(Long homeId, LocalDate date);
}