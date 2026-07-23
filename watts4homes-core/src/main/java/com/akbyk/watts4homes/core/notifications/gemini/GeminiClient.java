package com.akbyk.watts4homes.core.notifications.gemini;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class GeminiClient {

    private static final String FALLBACK_TEXT =
            "Enerji kullaniminizla ilgili onemli bir guncelleme var. Lutfen akbyk.watts4homes uygulamasindan " +
                    "guncel tuketim ve fatura durumunuzu kontrol edin; yuksek tuketimli cihazlarinizi kisa " +
                    "sureligine kapatmayi degerlendirebilirsiniz.";

    private final WebClient webClient;
    private final String apiUrl;
    private final String apiKey;

    public GeminiClient(WebClient.Builder webClientBuilder,
                        @Value("${akbyk.watts4homes.gemini.api-url}") String apiUrl,
                        @Value("${akbyk.watts4homes.gemini.api-key}") String apiKey) {
        this.webClient = webClientBuilder.build();
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String generateAdvisory(String prompt) {
        try {
            GeminiRequest request = new GeminiRequest(
                    List.of(new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt))))
            );

            GeminiResponse response = webClient.post()
                    .uri(apiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block(Duration.ofSeconds(10));

            if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
                log.warn("Gemini returned an empty/unparseable response, using fallback advisory text");
                return FALLBACK_TEXT;
            }

            return response.candidates().get(0).content().parts().get(0).text();
        } catch (Exception e) {
            log.error("Gemini call failed (timeout, rate-limit, or unreachable) - using fallback advisory text", e);
            return FALLBACK_TEXT;
        }
    }
}