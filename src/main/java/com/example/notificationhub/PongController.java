package com.example.notificationhub;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PongController {

    @GetMapping("/api/auth/ping")
    public String ping() {
        return "pong";
    }
}
