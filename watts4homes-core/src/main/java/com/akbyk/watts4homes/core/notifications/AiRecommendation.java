package com.akbyk.watts4homes.core.notifications;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
public class AiRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_id", nullable = false)
    private Long homeId;

    @Column(name = "generated_text", nullable = false, columnDefinition = "text")
    private String generatedText;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Column(name = "email_status", nullable = false)
    private String emailStatus; // PENDING / SENT / FAILED
}