package com.akbyk.watts4homes.core.scheduling;

import com.akbyk.watts4homes.core.homes.ConsumptionSnapshot;
import com.akbyk.watts4homes.core.homes.ConsumptionSnapshotRepository;
import com.akbyk.watts4homes.core.rules.HomeState;
import com.akbyk.watts4homes.core.rules.HomeStateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumptionSnapshotRollupJob {

    private final HomeStateStore homeStateStore;
    private final ConsumptionSnapshotRepository consumptionSnapshotRepository;

    @Scheduled(fixedRate = 60000)
    public void rollup() {
        LocalDate today = LocalDate.now();
        try {
            for (HomeStateStore.Entry entry : homeStateStore.getAll()) {
                upsertSnapshot(entry.homeId(), entry.state(), today);
            }
        } catch (Exception e) {
            log.error("Consumption snapshot rollup failed", e);
        }
    }

    private void upsertSnapshot(Long homeId, HomeState state, LocalDate date) {
        ConsumptionSnapshot snapshot = consumptionSnapshotRepository.findByHomeIdAndDate(homeId, date)
                .orElseGet(ConsumptionSnapshot::new);
        snapshot.setHomeId(homeId);
        snapshot.setDate(date);
        snapshot.setTotalUsage(BigDecimal.valueOf(state.getAccumulatedUsage()));
        snapshot.setTotalCost(BigDecimal.valueOf(state.getAccumulatedCost()));
        consumptionSnapshotRepository.save(snapshot);
    }
}