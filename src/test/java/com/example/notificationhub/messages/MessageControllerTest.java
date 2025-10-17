package com.example.notificationhub.messages;

import com.example.notificationhub.auth.JwtAuth;
import com.example.notificationhub.config.CurrentUser;
import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.users.Rol;
import com.example.notificationhub.users.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class MessageControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean MessageRepository messages;
    @MockBean JwtAuth jwtAuth;
    @MockBean CurrentUser currentUser;
    @MockBean NotificationService notificationService;
    @MockBean MessageFilterService filterService;

    private User user() {
        return User.builder()
                .username("fede")
                .rol(Rol.USER)
                .dailyLimit(100)
                .build();
    }

    @Test
    @WithMockUser(username = "fede", roles = {"USER"})
    void send_ok() throws Exception {
        var logged = user();
        when(currentUser.getOrThrow()).thenReturn(logged);

        var req = new SendMessageRequest(
                "NotificacionHub",
                "Mensaje a Slack",
                List.of(new SendMessageRequest.Target(Provider.SLACK, "{{slack}}"))
        );

        var message = new Message();
        message.setId(1L);
        message.setSender(logged);
        message.setRecipient("NotificacionHub");
        message.setContent("Mensaje a Slack");
        message.setStatus(MessageStatus.SUCCESS);
        message.setProvider(Provider.SLACK);
        message.setProviderResponse("OK");
        message.setCreatedAt(Instant.parse("2025-10-10T10:00:00Z"));
        when(notificationService.sendAndPersist(eq(logged), eq(req), eq(logged.getDailyLimit()))).thenReturn(message);

        mvc.perform(post("/api/messages/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sender").value("fede"))
                .andExpect(jsonPath("$.status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "fede", roles = {"USER"})
    void mine_with_filters_ok() throws Exception {
        var logged = user();
        when(currentUser.getOrThrow()).thenReturn(logged);

        var message = new Message();
        message.setId(2L);
        message.setSender(logged);
        message.setRecipient("r");
        message.setContent("c");
        message.setStatus(MessageStatus.SUCCESS);
        message.setProvider(Provider.SLACK);
        message.setProviderResponse("OK");
        message.setCreatedAt(Instant.parse("2025-10-10T10:00:00Z"));

        when(filterService.findFiltered(eq(logged), eq(MessageStatus.SUCCESS), eq(Provider.SLACK), any(), any()))
                .thenReturn(List.of(message));

        mvc.perform(get("/api/messages/mine")
                        .param("status", "SUCCESS")
                        .param("provider", "SLACK")
                        .param("from", "2025-10-01")
                        .param("to", "2025-10-17"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].sender").value("fede"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }

    @Test
    @WithMockUser(username = "fede", roles = {"USER"})
    void by_recipient_ok() throws Exception {
        var message = new Message();
        message.setId(3L);
        message.setSender(user());
        message.setRecipient("NotificacionHub");
        message.setContent("Mensaje a Slack");
        message.setStatus(MessageStatus.SUCCESS);
        message.setProvider(Provider.SLACK);
        message.setProviderResponse("OK");
        message.setCreatedAt(Instant.parse("2025-10-10T10:00:00Z"));

        when(messages.findByRecipientOrderByCreatedAtDesc("NotificacionHub")).thenReturn(List.of(message));

        mvc.perform(get("/api/messages/for/NotificacionHub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].recipient").value("NotificacionHub"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }
}
