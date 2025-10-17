package com.example.notificationhub.messages;

import com.example.notificationhub.config.CurrentUser;
import com.example.notificationhub.messages.dto.MessageResponse;
import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messages;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final MessageFilterService filterService;


    // POST /api/messages/send  (requiere Bearer token)
    @PostMapping("/send")
    public MessageResponse send(@RequestBody @Valid SendMessageRequest req) {
        var user = currentUser.getOrThrow();
        var saved = notificationService.sendAndPersist(user, req, user.getDailyLimit());
        return toResponse(saved);
    }

    @GetMapping("/mine")
    public List<MessageResponse> mine(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        User me = currentUser.getOrThrow();
        ZoneId zone = ZoneId.systemDefault();
        Instant fromI = (from == null) ? null : from.atStartOfDay(zone).toInstant();
        Instant toI   = (to   == null) ? null : to.plusDays(1).atStartOfDay(zone).toInstant();

        return filterService.findFiltered(me, status, provider, fromI, toI)
                .stream().map(this::toResponse).toList();
    }

    // GET /api/messages/for/{recipient}  (recibidos por destinatario)
    @GetMapping("/for/{recipient}")
    public List<MessageResponse> byRecipient(@PathVariable String recipient) {
        return messages.findByRecipientOrderByCreatedAtDesc(recipient)
                .stream().map(this::toResponse).toList();
    }

    // Mapper simple entidad -> DTO
    private MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getSender().getUsername(),
                m.getRecipient(),
                m.getContent(),
                m.getStatus().name(),
                m.getProvider().name(),
                m.getProviderResponse(),
                m.getCreatedAt()
        );
    }
}
