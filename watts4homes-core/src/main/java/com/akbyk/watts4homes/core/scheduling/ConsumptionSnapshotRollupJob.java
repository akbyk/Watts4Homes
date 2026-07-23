package com.akbyk.watts4homes.core.scheduling;

import com.akbyk.watts4homes.core.homes.ConsumptionSnapshot;
import com.akbyk.watts4homes.core.homes.ConsumptionSnapshotRepository;
import com.akbyk.watts4homes.core.rules.HomeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.sql.ResultSet;
import org.apache.ignite.sql.SqlRow;
import org.apache.ignite.table.KeyValueView;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class ConsumptionSnapshotRollupJob {

    private final KeyValueView<Long, HomeState> homeStateView;
    private final IgniteClient igniteClient;
    private final ConsumptionSnapshotRepository consumptionSnapshotRepository;

    // Every 60s for local development, so you can see rollups appear without waiting a full day.
    // TODO: In production, switch to a daily cron instead, e.g.: @Scheduled(cron = "0 0 0 * * *")
    @Scheduled(fixedRate = 60000)
    public void rollup() {
        LocalDate today = LocalDate.now();

        // In Ignite 3, table scans are executed using SQL queries
        try (ResultSet<SqlRow> rs = igniteClient.sql().execute(null, "SELECT HOME_ID FROM HOME_STATE")) {
            while (rs.hasNext()) {
                SqlRow row = rs.next();
                Long homeId = row.longValue("HOME_ID");

                // Pass null as transaction for auto-commit read
                HomeState state = homeStateView.get(null, homeId);
                if (state != null) {
                    upsertSnapshot(homeId, state, today);
                }
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
        snapshot.setTotalUsage(state.getAccumulatedUsage());
        snapshot.setTotalCost(state.getAccumulatedCost());
        consumptionSnapshotRepository.save(snapshot);
    }
}