package com.example.notificationhub.messages;

import com.example.notificationhub.auth.JwtAuth;
import com.example.notificationhub.users.Rol;
import com.example.notificationhub.users.User;
import com.example.notificationhub.users.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMessageController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminMessageControllerTest {

    @Autowired MockMvc mvc;

    @MockBean JwtAuth jwtAuth;
    @MockBean MessageFilterService filterService;
    @MockBean MessageRepository messages;
    @MockBean UserRepository users;

    @Test
    void all_ok() throws Exception {
        when(filterService.findFiltered(isNull(), any(), any(), any(), any())).thenReturn(List.of());

        mvc.perform(get("/api/messages/admin/all"))
                .andExpect(status().isOk());
    }

    @Test
    void metrics_ok() throws Exception {
        var admin = User.builder()
                .id(1L)
                .username("admin")
                .rol(Rol.ADMIN)
                .dailyLimit(200)
                .build();

        when(users.findAll()).thenReturn(List.of(admin));
        when(messages.countBySender(admin)).thenReturn(10L);
        when(messages.countBySenderAndCreatedAtBetween(eq(admin), any(), any())).thenReturn(3L);

        mvc.perform(get("/api/messages/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].totalSent").value(10))
                .andExpect(jsonPath("$[0].remainingToday").value(197));
    }
}
