package com.example.notificationhub.messages;
import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.messages.senders.CommonSender;
import com.example.notificationhub.messages.senders.ProviderResult;
import com.example.notificationhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MessageRepository messages;
    private final List<CommonSender> senders;

    public Message sendAndPersist(User sender, SendMessageRequest req, int dailyLimit) {
        var targets = (req.targets() == null) ? List.<SendMessageRequest.Target>of() : req.targets();
        if (targets.isEmpty()) {
            throw new IllegalArgumentException("No hay targets para enviar");
        }

        ZoneId zone = ZoneId.systemDefault();
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end   = start.plusSeconds(24 * 3600);

        long sentToday = messages.countBySenderAndCreatedAtBetween(sender, start, end);
        if (sentToday >= dailyLimit) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily message limit exceeded");
        }

        Message lastSaved = null;

        for (var t : targets) {
            if (sentToday >= dailyLimit) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily message limit exceeded");
            }

            var result = senders.stream()
                    .filter(s -> s.provider() == t.provider())
                    .findFirst()
                    .map(s -> s.send(t.destination(), req.content()))
                    .orElseGet(() -> ProviderResult.fail("Provider no soportado: " + t.provider()));

            var saved = messages.save(Message.builder()
                    .sender(sender)
                    .recipient(req.recipient())
                    .content(req.content())
                    .provider(t.provider()) // ← CLAVE: seteamos provider
                    .status(result.success() ? MessageStatus.SUCCESS : MessageStatus.FAILED)
                    .providerResponse(result.response())
                    .createdAt(Instant.now())
                    .build());

            lastSaved = saved;
            sentToday++;
        }

        return lastSaved; // el controller mapea a DTO
    }


    private String asText(Map<Provider, ProviderResult> map) {
        var sb = new StringBuilder();
        map.forEach((p, r) -> sb.append(p.name()).append(": ")
                .append(r.success() ? "OK " : "FAIL ")
                .append(r.response()).append("; "));
        return sb.toString();
    }
}
