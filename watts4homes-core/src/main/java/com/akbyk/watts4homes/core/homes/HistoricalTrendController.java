package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.HistoricalTrendResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
@Tag(name = "Historical Trend", description = "Daily aggregated usage/cost - reads exclusively from Postgres")
public class HistoricalTrendController {

    private final ConsumptionSnapshotRepository consumptionSnapshotRepository;

    @GetMapping("/{homeId}/trend")
    @Operation(summary = "Get daily aggregated consumption history for one home")
    public ResponseEntity<HistoricalTrendResponse> getTrend(@PathVariable Long homeId) {
        List<ConsumptionSnapshot> snapshots = consumptionSnapshotRepository.findByHomeIdOrderByDateAsc(homeId);
        List<HistoricalTrendResponse.DailyPoint> points = snapshots.stream()
                .map(s -> new HistoricalTrendResponse.DailyPoint(s.getDate(), s.getTotalUsage(), s.getTotalCost()))
                .toList();
        return ResponseEntity.ok(new HistoricalTrendResponse(homeId, points));
    }
}