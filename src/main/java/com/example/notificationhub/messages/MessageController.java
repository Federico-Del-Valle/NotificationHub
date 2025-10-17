package com.example.notificationhub.messages;
import com.example.notificationhub.config.CurrentUser;
import com.example.notificationhub.messages.dto.MessageResponse;
import com.example.notificationhub.messages.dto.SendMessageRequest;
import com.example.notificationhub.users.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;


@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "ENvio y consulta de mensajes del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {

    private final MessageRepository messages;
    private final CurrentUser currentUser;
    private final NotificationService notificationService;
    private final MessageFilterService filterService;


    //
    @Operation(
            summary = "Enviar mensaje a multiples plataformas",
            description = "Envia y gaurda en bd un mensaje respetando el límite diario del usuario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mensaje enviado",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Payload invalido"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "429", description = "Limite diario superado")
    })

    //Endpoint para enviar un mensaje a multiples plataformas
    @PostMapping("/send")
    public MessageResponse send(@RequestBody @Valid SendMessageRequest req) {
        var user = currentUser.getOrThrow();
        var saved = notificationService.sendAndPersist(user, req, user.getDailyLimit());
        return toResponse(saved);
    }


    //
    @Operation(
            summary = "Listar mis mensajes",
            description = "Devuelve los mensajes del usuario autenticado. Permite filtrar por estado, proveedor y rango de fechas (inclusive)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })

    //Endpoint que muestra los mensajes del usuario autenticado, con filtros opcionales
    @GetMapping("/mine")
    public List<MessageResponse> mine(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        User me = currentUser.getOrThrow();
        ZoneId zone = ZoneId.systemDefault();
        Instant fromI = (from == null) ? null : from.atStartOfDay(zone).toInstant();
        Instant toI   = (to   == null) ? null : to.plusDays(1).atStartOfDay(zone).toInstant();

        return filterService.findFiltered(me, status, provider, fromI, toI)
                .stream().map(this::toResponse).toList();
    }



    @Operation(
            summary = "Listar mensajes recibidos por destinatario",
            description = "Devuelve los mensajes para un destinatario particular"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })

    //Endpoint que muestra los mensajes enviados a un destinatario en particular
    @GetMapping("/for/{recipient}")
    public List<MessageResponse> byRecipient(@PathVariable String recipient) {
        return messages.findByRecipientOrderByCreatedAtDesc(recipient)
                .stream().map(this::toResponse).toList();
    }


    private MessageResponse toResponse(Message m) {
        return new MessageResponse(
                m.getId(),
                m.getSender().getUsername(),
                m.getRecipient(),
                m.getContent(),
                m.getStatus().name(),
                m.getProvider().name(),
                m.getProviderResponse(),
                m.getCreatedAt()
        );
    }
}
