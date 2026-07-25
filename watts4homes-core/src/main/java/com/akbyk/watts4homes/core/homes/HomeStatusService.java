package com.akbyk.watts4homes.core.homes;

import com.akbyk.watts4homes.core.homes.dto.HomeStatusResponse;
import com.akbyk.watts4homes.core.rules.ApplianceBreachState;
import com.akbyk.watts4homes.core.rules.ApplianceBreachStore;
import com.akbyk.watts4homes.core.rules.HomeState;
import com.akbyk.watts4homes.core.rules.HomeStateStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HomeStatusService {

    private final HomeStateStore homeStateStore;
    private final ApplianceBreachStore applianceBreachStore;

    public Optional<HomeStatusResponse> getStatus(Long homeId) {
        HomeState state = homeStateStore.get(homeId);
        if (state == null) {
            return Optional.empty();
        }
        return Optional.of(toResponse(homeId, state));
    }

    public List<HomeStatusResponse> getAllStatuses() {
        List<HomeStatusResponse> result = new ArrayList<>();
        for (HomeStateStore.Entry entry : homeStateStore.getAll()) {
            result.add(toResponse(entry.homeId(), entry.state()));
        }
        return result;
    }

    private HomeStatusResponse toResponse(Long homeId, HomeState state) {
        List<HomeStatusResponse.ApplianceStatus> appliances = new ArrayList<>();
        if (state.getApplianceIds() != null) {
            for (Long applianceId : state.getApplianceIds()) {
                ApplianceBreachState breach = applianceBreachStore.get(homeId + ":" + applianceId);
                if (breach != null) {
                    appliances.add(new HomeStatusResponse.ApplianceStatus(
                            applianceId, breach.getSafeLimitWatts(), breach.getConsecutiveBreachCount(), breach.getLastStatus()));
                } else {
                    appliances.add(new HomeStatusResponse.ApplianceStatus(applianceId, 0, 0, "UNKNOWN"));
                }
            }
        }
        return new HomeStatusResponse(homeId, state.getAccumulatedUsage(), state.getAccumulatedCost(),
                state.getTariffState(), state.getBudgetQuota(), appliances);
    }
}