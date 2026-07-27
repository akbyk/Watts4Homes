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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeStatusService {

    private final HomeStateStore homeStateStore;
    private final ApplianceBreachStore applianceBreachStore;
    private final HomeRepository homeRepository;
    private final ApplianceRepository applianceRepository;

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
        // labels live in postgres, not ignite -> read them here so any device stays label-complete
        String homeName = homeRepository.findById(homeId)
                .map(Home::getName)
                .orElse("Ev #" + homeId);

        // one query per home for its appliances -> map by id for name/type lookup
        Map<Long, Appliance> applianceById = applianceRepository.findByHomeId(homeId).stream()
                .collect(Collectors.toMap(Appliance::getId, a -> a));

        List<HomeStatusResponse.ApplianceStatus> appliances = new ArrayList<>();
        if (state.getApplianceIds() != null) {
            for (Long applianceId : state.getApplianceIds()) {
                Appliance appliance = applianceById.get(applianceId);
                String name = appliance != null ? appliance.getName() : "Cihaz";
                String type = appliance != null ? appliance.getType() : "DEFAULT";

                ApplianceBreachState breach = applianceBreachStore.get(homeId + ":" + applianceId);
                if (breach != null) {
                    appliances.add(new HomeStatusResponse.ApplianceStatus(
                            applianceId, name, type,
                            breach.getSafeLimitWatts(), breach.getConsecutiveBreachCount(), breach.getLastStatus()));
                } else {
                    appliances.add(new HomeStatusResponse.ApplianceStatus(
                            applianceId, name, type, 0, 0, "UNKNOWN"));
                }
            }
        }

        return new HomeStatusResponse(homeId, homeName, state.getAccumulatedUsage(),
                state.getAccumulatedCost(), state.getTariffState(), state.getBudgetQuota(), appliances);
    }
}