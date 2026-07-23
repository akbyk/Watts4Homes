package com.akbyk.watts4homes.core.notifications.event;

import java.util.Map;

public record NotificationTrigger(
        Long homeId,
        TriggerType triggerType,
        Map<String, Object> context
) {
    public enum TriggerType {
        EIGHTY_PERCENT_BREACH,
        HUNDRED_PERCENT_BREACH,
        ANOMALY
    }
}