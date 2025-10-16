package com.example.notificationhub.config;

import com.example.notificationhub.users.User;
import com.example.notificationhub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {
    private final UserRepository users;

    public User getOrThrow() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null)
            throw new IllegalStateException("No hay usuario autenticado");

        String username = String.valueOf(auth.getPrincipal());
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + username));
    }
}
