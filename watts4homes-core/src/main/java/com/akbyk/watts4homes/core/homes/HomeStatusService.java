package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.HomeStatusResponse;
import com.akbyk.watts4homes.core.rules.ApplianceBreachState;
import com.akbyk.watts4homes.core.rules.HomeState;
import lombok.RequiredArgsConstructor;
import org.apache.ignite.client.IgniteClient;
import org.apache.ignite.sql.ResultSet;
import org.apache.ignite.sql.SqlRow;
import org.apache.ignite.table.KeyValueView;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomeStatusService {

    private final KeyValueView<Long, HomeState> homeStateView;
    private final KeyValueView<String, ApplianceBreachState> applianceBreachView;
    private final IgniteClient igniteClient;

    public Optional<HomeStatusResponse> getStatus(Long homeId) {
        // Null as first param signifies implicit auto-commit transaction
        HomeState state = homeStateView.get(null, homeId);
        if (state == null) {
            return Optional.empty();
        }
        return Optional.of(toResponse(homeId, state));
    }

    public List<HomeStatusResponse> getAllStatuses() {
        List<HomeStatusResponse> result = new ArrayList<>();

        try (ResultSet<SqlRow> resultSet = igniteClient.sql().execute(null, "SELECT HOME_ID FROM HOME_STATE")) {
            while (resultSet.hasNext()) {
                SqlRow row = resultSet.next();
                Long homeId = row.longValue("HOME_ID");
                HomeState state = homeStateView.get(null, homeId);
                if (state != null) {
                    result.add(toResponse(homeId, state));
                }
            }
        }

        return result;
    }

    private HomeStatusResponse toResponse(Long homeId, HomeState state) {
        List<HomeStatusResponse.ApplianceStatus> appliances = new ArrayList<>();

        // Uses the getApplianceIds() helper on HomeState that parses applianceIdsCsv
        if (state.getApplianceIds() != null && !state.getApplianceIds().isEmpty()) {
            for (Long applianceId : state.getApplianceIds()) {
                ApplianceBreachState breach = applianceBreachView.get(null, homeId + ":" + applianceId);

                if (breach != null) {
                    appliances.add(new HomeStatusResponse.ApplianceStatus(
                            applianceId,
                            breach.getSafeLimitWatts(),
                            breach.getConsecutiveBreachCount(),
                            breach.getLastStatus()
                    ));
                } else {
                    // Registered home, but no telemetry has arrived for this appliance yet
                    appliances.add(new HomeStatusResponse.ApplianceStatus(applianceId, 0.0, 0, "UNKNOWN"));
                }
            }
        }

        return new HomeStatusResponse(
                homeId,
                state.getAccumulatedUsage(),
                state.getAccumulatedCost(),
                state.getTariffState(),
                state.getBudgetQuota(),
                appliances
        );
    }
}