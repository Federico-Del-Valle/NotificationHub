package com.example.notificationhub.messages.dto;

import java.time.Instant;

public record MessageResponse(
        Long id,
        String sender,
        String recipient,
        String content,
        String status,
        String providerResponse,
        Instant createdAt
) {}
