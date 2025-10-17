package com.example.notificationhub.messages;

import com.example.notificationhub.users.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;


@Entity
@Table(name = "app_message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name = "sender_id")
    private User sender;

    @Column(nullable = false, length = 120)
    private String recipient;

    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageStatus status = MessageStatus.PENDING;

    // Podés dejarlo como TEXT; si después usamos JSON, lo mapeamos igual como String
    @Column(name = "provider_response", columnDefinition = "text")
    private String providerResponse;

    @Builder.Default
    @Column(nullable = false, name = "created_at")
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Provider provider;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = MessageStatus.PENDING;
    }
}

