package com.akbyk.watts4homes.core.notifications;

import com.akbyk.watts4homes.core.homes.Home;
import com.akbyk.watts4homes.core.homes.HomeRepository;
import com.akbyk.watts4homes.core.notifications.event.NotificationTrigger;
import com.akbyk.watts4homes.core.notifications.gemini.GeminiClient;
import com.akbyk.watts4homes.core.rules.HomeState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.table.KeyValueView;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final KeyValueView<Long, HomeState> homeStateView;
    private final HomeRepository homeRepository;
    private final GeminiClient geminiClient;
    private final EmailService emailService;
    private final AiRecommendationRepository aiRecommendationRepository;

    @Async("notificationExecutor")
    public void handleTrigger(NotificationTrigger trigger) {
        try {
            Home home = homeRepository.findById(trigger.homeId())
                    .orElseThrow(() -> new IllegalStateException("Home not found for id=" + trigger.homeId()));

            // Null as the first parameter signifies an implicit transaction in Ignite 3
            HomeState state = homeStateView.get(null, trigger.homeId());

            String prompt = buildPrompt(home, state, trigger);
            String advisoryText = geminiClient.generateAdvisory(prompt);

            AiRecommendation recommendation = new AiRecommendation();
            recommendation.setHomeId(trigger.homeId());
            recommendation.setGeneratedText(advisoryText);
            recommendation.setEmailStatus("PENDING");
            recommendation = aiRecommendationRepository.save(recommendation);

            boolean sent = emailService.sendAdvisoryEmail(
                    home.getContactEmail(), "VoltWise Enerji Uyarisi", advisoryText);

            recommendation.setEmailStatus(sent ? "SENT" : "FAILED");
            recommendation.setSentAt(OffsetDateTime.now());
            aiRecommendationRepository.save(recommendation);

        } catch (Exception e) {
            log.error("Failed to process notification trigger for homeId={}", trigger.homeId(), e);
        }
    }

    private String buildPrompt(Home home, HomeState state, NotificationTrigger trigger) {
        StringBuilder sb = new StringBuilder();
        sb.append("Sen bir enerji tasarrufu danismanisin. SADECE Turkce yanit ver. ");
        sb.append("Ev sahibine kisisel, eyleme gecirilebilir ve davranissal tavsiyeler ver, 3-4 cumle ile sinirli tut. ");
        sb.append("Ev adi: ").append(home.getName()).append(". ");
        if (state != null) {
            sb.append("Guncel birikimli maliyet: ").append(state.getAccumulatedCost())
                    .append(", butce kotasi: ").append(state.getBudgetQuota())
                    .append(", tarife durumu: ").append(state.getTariffState()).append(". ");
        }
        sb.append("Tetikleyici olay turu: ").append(trigger.triggerType()).append(". ");
        sb.append("Ek baglam: ").append(trigger.context()).append(". ");
        sb.append("Bu verileri kullanarak somut sayilara atifta bulunan, kisiye ozel bir tavsiye metni yaz.");
        return sb.toString();
    }
}