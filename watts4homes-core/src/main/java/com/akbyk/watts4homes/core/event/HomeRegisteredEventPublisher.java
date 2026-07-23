package com.akbyk.watts4homes.core.event;

import com.akbyk.watts4homes.core.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HomeRegisteredEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHomeRegistered(HomeRegisteredInternalEvent event) {
        HomeRegisteredEvent payload = event.payload();
        String key = String.valueOf(payload.homeId());
        kafkaTemplate.send(KafkaTopicConfig.HOME_REGISTRATION_TOPIC, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish home-registration event for homeId={}", payload.homeId(), ex);
                    } else {
                        log.info("Published home-registration event for homeId={}", payload.homeId());
                    }
                });
    }
}