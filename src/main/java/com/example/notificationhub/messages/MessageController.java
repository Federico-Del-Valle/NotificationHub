package com.example.notificationhub.messages;

import com.example.notificationhub.config.CurrentUser;
import com.example.notificationhub.messages.dto.MessageResponse;
import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.users.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageRepository messages;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;

    // POST /api/messages/send  (requiere Bearer token)
    @PostMapping("/send")
    public MessageResponse send(@RequestBody @Valid SendMessageRequest req) {
        var user = currentUser.getOrThrow();
        var saved = notificationService.sendAndPersist(user, req, user.getDailyLimit());
        return toResponse(saved);
    }

    // GET /api/messages/mine  (mis enviados)
    @GetMapping("/mine")
    public List<MessageResponse> mine() {
        User me = currentUser.getOrThrow();
        return messages.findBySenderOrderByCreatedAtDesc(me)
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
                m.getProviderResponse(),
                m.getCreatedAt()
        );
    }
}
