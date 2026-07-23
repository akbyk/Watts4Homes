package com.akbyk.watts4homes.core.telemetry;

import com.akbyk.watts4homes.core.homes.Appliance;
import com.akbyk.watts4homes.core.homes.ApplianceRepository;
import com.akbyk.watts4homes.core.homes.Home;
import com.akbyk.watts4homes.core.homes.HomeRepository;
import com.akbyk.watts4homes.core.rules.ApplianceBreachState;
import com.akbyk.watts4homes.core.rules.HomeState;
import com.akbyk.watts4homes.core.rules.RulesService;
import com.akbyk.watts4homes.core.telemetry.event.TelemetryReading;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.table.KeyValueView;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelemetryProcessingService {

    // Matches Sensors' @Scheduled(fixedRate = 5000) tick interval. If that interval ever changes,
    // update this constant too — it's what converts a wattage reading into an energy/cost delta.
    private static final int TELEMETRY_INTERVAL_SECONDS = 5;

    // Injected KeyValueViews configured in IgniteConfig
    private final KeyValueView<Long, HomeState> homeStateView;
    private final KeyValueView<String, ApplianceBreachState> applianceBreachView;
    private final HomeRepository homeRepository;
    private final ApplianceRepository applianceRepository;
    private final RulesService rulesService;

    public void process(TelemetryReading reading) {
        HomeState homeState = updateHomeState(reading);
        if (homeState == null) {
            return; // Ignite write failed and was already logged; skip the rest of this message.
        }
        updateApplianceBreachState(reading);
    }

    private HomeState updateHomeState(TelemetryReading reading) {
        try {
            // Null as the first parameter signifies an implicit auto-commit transaction
            HomeState homeState = homeStateView.get(null, reading.homeId());
            if (homeState == null) {
                homeState = initializeHomeState(reading.homeId());
            }

            double deltaKwh = (reading.watts() / 1000.0) * (TELEMETRY_INTERVAL_SECONDS / 3600.0);
            double rate = "PENALTY".equals(homeState.getTariffState())
                    ? homeState.getPenaltyRate()
                    : homeState.getCurrentRate();
            double deltaCost = deltaKwh * rate;

            homeState.setAccumulatedUsage(homeState.getAccumulatedUsage() + deltaKwh);
            homeState.setAccumulatedCost(homeState.getAccumulatedCost() + deltaCost);

            // Rules Module mutates tariffState / breach flags on this same object in place.
            rulesService.evaluateQuota(reading.homeId(), homeState);

            // Atomic put operation in Ignite 3
            homeStateView.put(null, reading.homeId(), homeState);
            return homeState;
        } catch (Exception e) {
            log.error("Failed to update Ignite home-state for homeId={}, skipping message", reading.homeId(), e);
            return null;
        }
    }

    private void updateApplianceBreachState(TelemetryReading reading) {
        String breachKey = reading.homeId() + ":" + reading.applianceId();
        try {
            ApplianceBreachState breachState = applianceBreachView.get(null, breachKey);
            if (breachState == null) {
                breachState = initializeApplianceBreachState(reading.applianceId());
            }

            rulesService.evaluateApplianceBreach(reading.homeId(), reading.applianceId(), reading.watts(), breachState);

            applianceBreachView.put(null, breachKey, breachState);
        } catch (Exception e) {
            log.error("Failed to update Ignite appliance-breach for key={}, skipping", breachKey, e);
        }
    }

    private HomeState initializeHomeState(Long homeId) {
        Home home = homeRepository.findById(homeId)
                .orElseThrow(() -> new IllegalStateException("Home not found for id=" + homeId));

        HomeState state = new HomeState();
        state.setBudgetQuota(home.getBudgetQuota().doubleValue());
        state.setCurrentRate(home.getCurrentRate().doubleValue());
        state.setPenaltyRate(home.getPenaltyRate().doubleValue());
        state.setAccumulatedUsage(0.0);
        state.setAccumulatedCost(0.0);
        state.setTariffState("NORMAL");
        state.setBreachedEightyPercent(false);
        state.setBreachedHundredPercent(false);
        return state;
    }

    private ApplianceBreachState initializeApplianceBreachState(Long applianceId) {
        Appliance appliance = applianceRepository.findById(applianceId)
                .orElseThrow(() -> new IllegalStateException("Appliance not found for id=" + applianceId));

        ApplianceBreachState state = new ApplianceBreachState();
        state.setSafeLimitWatts(appliance.getSafeLimitWatts().doubleValue());
        state.setConsecutiveBreachCount(0);
        state.setLastStatus("NORMAL");
        return state;
    }
}