package com.example.notificationhub.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(description = "Nombre de usuario", example = "fede")
        @NotBlank String username,

        @NotBlank String password)
{}