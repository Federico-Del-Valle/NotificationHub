package com.example.notificationhub.messages.senders;

import com.example.notificationhub.messages.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class TelegramSender implements CommonSender {

    private final RestTemplate http;

    @Override public Provider provider() { return Provider.TELEGRAM; }

    @Override
    public ProviderResult send(String destination, String content) {
        // destination = "BOT_TOKEN:CHAT_ID"
        try {
            var parts = destination.split(":", 2);
            String botToken = parts[0];
            String chatId = parts.length > 1 ? parts[1] : "";
            var url = UriComponentsBuilder.fromHttpUrl("https://api.telegram.org/bot" + botToken + "/sendMessage")
                    .queryParam("chat_id", chatId)
                    .queryParam("text", content)
                    .build(true).toUri();

            ResponseEntity<String> resp = http.getForEntity(url, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();
            return ok ? ProviderResult.ok("Telegram " + resp.getStatusCode())
                    : ProviderResult.fail("Telegram " + resp.getStatusCode() + " " + resp.getBody());
        } catch (Exception e) {
            return ProviderResult.fail("Telegram error: " + e.getMessage());
        }
    }
}
