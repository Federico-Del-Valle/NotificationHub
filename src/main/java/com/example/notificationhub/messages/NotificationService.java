package com.example.notificationhub.messages;

import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.messages.senders.CommonSender;
import com.example.notificationhub.messages.senders.ProviderResult;
import com.example.notificationhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MessageRepository messages;
    private final List<CommonSender> senders;

    public Message sendAndPersist(User sender, SendMessageRequest req, int dailyLimit) {
        var start = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        var end   = start.plusSeconds(24 * 3600);
        long sentToday = messages.countBySenderAndCreatedAtBetween(sender, start, end);
        if (sentToday >= dailyLimit) {
            throw new IllegalStateException("Daily limit exceeded: " + dailyLimit);
        }

        var msg = Message.builder()
                .sender(sender)
                .recipient(req.recipient())
                .content(req.content())
                .status(MessageStatus.PENDING)
                .build();
        messages.save(msg);

        var resultsByProvider = new EnumMap<Provider, ProviderResult>(Provider.class);
        var targets = (req.targets() == null || req.targets().isEmpty())
                ? List.<SendMessageRequest.Target>of()
                : req.targets();

        for (var t : targets) {
            senders.stream()
                    .filter(s -> s.provider() == t.provider())
                    .findFirst()
                    .ifPresent(s -> {
                        var r = s.send(t.destination(), req.content());
                        resultsByProvider.put(t.provider(), r);
                    });
        }

        boolean anyFail = resultsByProvider.values().stream().anyMatch(r -> !r.success());
        boolean anyOk   = resultsByProvider.values().stream().anyMatch(ProviderResult::success);

        if (targets.isEmpty()) {
            msg.setStatus(MessageStatus.PENDING);
            msg.setProviderResponse("No hay providers");
        } else if (anyFail && !anyOk) {
            msg.setStatus(MessageStatus.FAILED);
            msg.setProviderResponse(asText(resultsByProvider));
        } else if (anyOk && !anyFail) {
            msg.setStatus(MessageStatus.SUCCESS);
            msg.setProviderResponse(asText(resultsByProvider));
        } else {
            msg.setStatus(MessageStatus.FAILED);
            msg.setProviderResponse(asText(resultsByProvider));
        }

        return messages.save(msg);
    }

    private String asText(Map<Provider, ProviderResult> map) {
        var sb = new StringBuilder();
        map.forEach((p, r) -> sb.append(p.name()).append(": ")
                .append(r.success() ? "OK " : "FAIL ")
                .append(r.response()).append("; "));
        return sb.toString();
    }
}
