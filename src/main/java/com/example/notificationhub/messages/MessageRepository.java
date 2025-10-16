package com.example.notificationhub.messages;

import com.example.notificationhub.users.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderOrderByCreatedAtDesc(User sender);
    List<Message> findByRecipientOrderByCreatedAtDesc(String recipient);

    long countBySenderAndCreatedAtBetween(User sender, Instant from, Instant to);
}
