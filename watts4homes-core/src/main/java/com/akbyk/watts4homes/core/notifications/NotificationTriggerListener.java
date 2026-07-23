package com.akbyk.watts4homes.core.notifications;

import com.akbyk.watts4homes.core.notifications.event.NotificationTrigger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationTriggerListener {

    private final NotificationService notificationService;

    @EventListener
    public void onTrigger(NotificationTrigger trigger) {
        notificationService.handleTrigger(trigger);
    }
}