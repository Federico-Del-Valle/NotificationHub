package com.example.notificationhub.messages.senders;
import com.example.notificationhub.messages.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
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
            var entity  = new HttpEntity<>(Map.of("text", content), headers);

            ResponseEntity<String> resp = http.exchange(webhookUrl, HttpMethod.POST, entity, String.class);

            if (resp.getStatusCode().is3xxRedirection()) {
                URI loc = resp.getHeaders().getLocation();
                if (loc != null) {
                    resp = http.exchange(loc, HttpMethod.POST, entity, String.class);
                }
            }

            boolean ok = resp.getStatusCode().is2xxSuccessful();
            return ok
                    ? ProviderResult.ok("Slack " + resp.getStatusCode())
                    : ProviderResult.fail("Slack " + resp.getStatusCode() + " " + (resp.getBody() == null ? "" : resp.getBody()));
        } catch (Exception e) {
            return ProviderResult.fail("Slack error: " + e.getMessage());
        }
    }
}
