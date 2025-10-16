package com.example.notificationhub.messages.senders;

import com.example.notificationhub.messages.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class TelegramSender implements CommonSender {

    private final RestTemplate http;

    @Override public Provider provider() { return Provider.TELEGRAM; }

    @Override
    public ProviderResult send(String destination, String content) {
        try {
            // A) parseo SEGURO: BOT_TOKEN y CHAT_ID separados por el ÚLTIMO ':'
            int idx = destination.lastIndexOf(':');
            if (idx <= 0 || idx == destination.length() - 1) {
                return ProviderResult.fail("Telegram error: destination inválido. Esperado BOT_TOKEN:CHAT_ID");
            }
            String botToken = destination.substring(0, idx);   // token completo (con ':')
            String chatId   = destination.substring(idx + 1);  // chat id

            // B) POST application/x-www-form-urlencoded (evita problemas de encoding)
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("chat_id", chatId);
            form.add("text", content);

            HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(form, headers);

            ResponseEntity<String> resp = http.postForEntity(url, req, String.class);
            boolean ok = resp.getStatusCode().is2xxSuccessful();

            return ok
                    ? ProviderResult.ok("Telegram " + resp.getStatusCode() + " " + resp.getBody())
                    : ProviderResult.fail("Telegram " + resp.getStatusCode() + " " + resp.getBody());

        } catch (HttpClientErrorException e) {
            return ProviderResult.fail("Telegram " + e.getStatusCode() + " " + e.getResponseBodyAsString());
        } catch (Exception e) {
            return ProviderResult.fail("Telegram error: " + e.getMessage());
        }
    }
}
