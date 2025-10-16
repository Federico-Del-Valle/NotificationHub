package com.example.notificationhub.messages.senders;

import com.example.notificationhub.messages.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SlackSender implements CommonSender {

    private final RestTemplate http;

    @Override public Provider provider() { return Provider.SLACK; }

    @Override
    public ProviderResult send(String webhookUrl, String content) {
        try {
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            var body = Map.of("text", content);
            var resp = http.postForEntity(webhookUrl, new HttpEntity<>(body, headers), String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();
            return ok ? ProviderResult.ok("Slack " + resp.getStatusCode())
                    : ProviderResult.fail("Slack " + resp.getStatusCode() + " " + resp.getBody());
        } catch (Exception e) {
            return ProviderResult.fail("Slack error: " + e.getMessage());
        }
    }
}
