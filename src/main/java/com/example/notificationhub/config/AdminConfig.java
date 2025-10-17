package com.example.notificationhub.config;
import com.example.notificationhub.users.Rol;
import com.example.notificationhub.users.User;
import com.example.notificationhub.users.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class AdminConfig {

    private final PasswordEncoder encoder;

    @Bean
    CommandLineRunner initAdmin(UserRepository users) {
        return args -> {
            users.findByUsername("admin").ifPresentOrElse(
                    existing -> System.out.println("Admin ya existente: " + existing.getUsername()),
                    () -> {
                        User admin = User.builder()
                                .username("admin")
                                .passwordHash(encoder.encode("admin123"))
                                .rol(Rol.ADMIN)
                                .dailyLimit(100)
                                .build();
                        users.save(admin);
                        System.out.println(" Admin creado automáticamente (admin / admin123)");
                    }
            );
        };
    }
}
