package com.akbyk.watts4homes.core.rules;

import com.akbyk.watts4homes.core.notifications.event.NotificationTrigger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RulesService {

    private final EventLogRepository eventLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void evaluateQuota(Long homeId, HomeState state) {
        double eightyPercentThreshold = state.getBudgetQuota() * 0.8;

        if (!state.isBreachedEightyPercent() && state.getAccumulatedCost() >= eightyPercentThreshold) {
            state.setBreachedEightyPercent(true);
            logEventSafely(homeId, "80%_BREACH", Map.of(
                    "accumulatedCost", state.getAccumulatedCost(),
                    "budgetQuota", state.getBudgetQuota()
            ));
            log.info("[AI NOTIFICATION STUB] Would trigger 80% breach alert for homeId={}", homeId);
        }

        if (!state.isBreachedHundredPercent() && state.getAccumulatedCost() >= state.getBudgetQuota()) {
            state.setBreachedHundredPercent(true);
            state.setTariffState("PENALTY");
            Map<String, Object> context = Map.of(
                    "accumulatedCost", state.getAccumulatedCost(),
                    "budgetQuota", state.getBudgetQuota(),
                    "penaltyRate", state.getPenaltyRate()
            );
            logEventSafely(homeId, "100%_BREACH", context);
            logEventSafely(homeId, "PENALTY_ACTIVATED", Map.of("penaltyRate", state.getPenaltyRate()));
            eventPublisher.publishEvent(new NotificationTrigger(homeId, NotificationTrigger.TriggerType.HUNDRED_PERCENT_BREACH, context));
        }
    }

    public void evaluateApplianceBreach(Long homeId, Long applianceId, double watts, ApplianceBreachState state) {
        if (watts > state.getSafeLimitWatts()) {
            state.setConsecutiveBreachCount(state.getConsecutiveBreachCount() + 1);

            if (state.getConsecutiveBreachCount() >= 3 && !"ANOMALOUS".equals(state.getLastStatus())) {
                state.setLastStatus("ANOMALOUS");
                Map<String, Object> context = Map.of(
                        "applianceId", applianceId,
                        "watts", watts,
                        "safeLimitWatts", state.getSafeLimitWatts()
                );
                logEventSafely(homeId, "ANOMALY", context);
                eventPublisher.publishEvent(new NotificationTrigger(homeId, NotificationTrigger.TriggerType.ANOMALY, context));
            }
        } else {
            state.setConsecutiveBreachCount(0);
            state.setLastStatus("NORMAL");
        }
    }

    private void logEventSafely(Long homeId, String eventType, Map<String, Object> metadata) {
        try {
            EventLog eventLog = new EventLog();
            eventLog.setHomeId(homeId);
            eventLog.setEventType(eventType);
            eventLog.setTimestamp(OffsetDateTime.now());
            eventLog.setMetadata(metadata);
            eventLogRepository.save(eventLog);
        } catch (Exception e) {
            log.error("Failed to write event_log entry (homeId={}, eventType={}) - Ignite state is unaffected",
                    homeId, eventType, e);
        }
    }
}