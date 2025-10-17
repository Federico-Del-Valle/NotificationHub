package com.example.notificationhub.messages;

import com.example.notificationhub.messages.dto.MessageResponse;
import com.example.notificationhub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.List;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/messages/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMessageController {

    private final MessageFilterService filterService;
    private final MessageRepository messages;
    private final UserRepository users;

    // --- Endpoint 1: listar todos los mensajes con filtros ---
    @GetMapping("/all")
    public List<MessageResponse> all(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant fromI = (from == null) ? null : from.atStartOfDay(zone).toInstant();
        Instant toI   = (to   == null) ? null : to.plusDays(1).atStartOfDay(zone).toInstant();

        return filterService.findFiltered(null, status, provider, fromI, toI)
                .stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getSender().getUsername(),
                        m.getRecipient(),
                        m.getContent(),
                        m.getStatus().name(),
                        m.getProvider().name(),
                        m.getProviderResponse(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    // --- Record para métricas ---
    record UserMetricsResponse(String username, long totalSent, long remainingToday) {}

    // --- Endpoint 2: métricas globales ---
    @GetMapping("/metrics")
    public List<UserMetricsResponse> metrics() {
        ZoneId zone = ZoneId.systemDefault();
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end   = start.plusSeconds(24 * 3600);

        return users.findAll().stream().map(u -> {
            long total     = messages.countBySender(u);
            long sentToday = messages.countBySenderAndCreatedAtBetween(u, start, end);
            long remaining = Math.max(0, u.getDailyLimit() - sentToday);
            return new UserMetricsResponse(u.getUsername(), total, remaining);
        }).toList();
    }
}

