package com.example.notificationhub.messages;

import com.example.notificationhub.users.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageFilterService {

    private final MessageRepository messages;

    public List<Message> findFiltered(User sender,
                                      MessageStatus status,
                                      Provider provider,
                                      Instant from,
                                      Instant to) {

        List<Message> base = (sender != null)
                ? messages.findBySenderOrderByCreatedAtDesc(sender)
                : messages.findAll();

        return base.stream()
                .filter(m -> status == null || m.getStatus() == status)
                .filter(m -> provider == null || m.getProvider() == provider)
                .filter(m -> from == null || m.getCreatedAt().isAfter(from))
                .filter(m -> to == null || m.getCreatedAt().isBefore(to))
                .toList();
    }
}
