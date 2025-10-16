package com.example.notificationhub.messages.dto;
import com.example.notificationhub.messages.Provider;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SendMessageRequest(
        @NotBlank @Size(max = 120) String recipient,
        @NotBlank String content,
        @Size(min = 1) List<@Valid Target> targets
) {
    public record Target(Provider provider, @NotBlank String destination) {}
}
