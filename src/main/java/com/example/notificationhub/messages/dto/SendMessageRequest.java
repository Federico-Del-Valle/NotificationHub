package com.example.notificationhub.messages.dto;
import com.example.notificationhub.messages.Provider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Endpoint para enviar mensajes a multiples plataformas")
public record SendMessageRequest(

        @Schema(description = "Usuario destinatario", example = "federico.delvalle")
        @NotBlank @Size(max = 120) String recipient,

        @Schema(description = "Contenido del mensaje", example = "La vida es bella")
        @NotBlank String content,

        @Schema(
                description = "Lista de destinos donde se enviaran los mensajes (slack o telegram)",
                example = "[{\"provider\":\"SLACK\",\"destination\":\"general\"}]"
        )
        @Size(min = 1) List<@Valid Target> targets
) {
    public record Target(

            @Schema(description = "Proveedor donde se enviara el mensaje", example = "SLACK")
            Provider provider,

            @Schema(description = "Chat destino", example = "Fede")
            @NotBlank String destination) {}
}
