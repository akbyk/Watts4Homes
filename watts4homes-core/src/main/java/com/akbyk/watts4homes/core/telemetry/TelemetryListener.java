package com.akbyk.watts4homes.core.telemetry;


import com.akbyk.watts4homes.core.telemetry.event.TelemetryReading;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TelemetryListener {

    private final TelemetryProcessingService telemetryProcessingService;

    @KafkaListener(topics = "telemetry-stream", groupId = "watts4homes-core")
    public void onTelemetryReading(TelemetryReading reading) {
        telemetryProcessingService.process(reading);
    }
}