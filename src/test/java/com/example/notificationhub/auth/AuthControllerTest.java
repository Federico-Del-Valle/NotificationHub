package com.example.notificationhub.auth;

import com.example.notificationhub.auth.dto.LoginRequest;
import com.example.notificationhub.auth.dto.RegisterRequest;
import com.example.notificationhub.users.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserRepository users;
    @MockBean PasswordEncoder encoder;
    @MockBean JwtService jwt;

    @Test
    void register_ok() throws Exception {
        when(users.existsByUsername("fede")).thenReturn(false);
        when(encoder.encode("Secreta123!")).thenReturn("hash");
        when(jwt.generate(eq("fede"), anyMap())).thenReturn("jwt");

        var request = new RegisterRequest("fede","Secreta123!");
        var body = om.writeValueAsString(request);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"));
    }

    @Test
    void login_ok() throws Exception {
        var u = User.builder().username("fede").passwordHash("hash").rol(Rol.USER).build();
        when(users.findByUsername("fede")).thenReturn(Optional.of(u));
        when(encoder.matches("Secreta123!", "hash")).thenReturn(true);
        when(jwt.generate(eq("fede"), anyMap())).thenReturn("jwt");

        var body = om.writeValueAsString(new LoginRequest("fede","Secreta123!"));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt"));
    }
}
