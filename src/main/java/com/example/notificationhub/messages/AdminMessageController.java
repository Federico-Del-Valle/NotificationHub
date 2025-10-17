package com.example.notificationhub.messages;
import com.example.notificationhub.messages.dto.MessageResponse;
import com.example.notificationhub.users.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.List;
import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/api/messages/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Endpoints", description = "Operaciones de adminis sobre metricas y users")
@SecurityRequirement(name = "bearerAuth")
public class AdminMessageController {

    private final MessageFilterService filterService;
    private final MessageRepository messages;
    private final UserRepository users;


    @Operation(
            summary = "Listar todos los mensajes de todos los users(ADMIN)",
            description = "Devuelve todos los mensajes con filtros opcionales por estado, proveedor y rango de fechas"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN)")
    })

    @GetMapping("/all")
    public List<MessageResponse> all(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) Provider provider,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE) LocalDate to
    ) {
        ZoneId zone = ZoneId.systemDefault();
        Instant fromI = (from == null) ? null : from.atStartOfDay(zone).toInstant();
        Instant toI   = (to   == null) ? null : to.plusDays(1).atStartOfDay(zone).toInstant();

        return filterService.findFiltered(null, status, provider, fromI, toI)
                .stream()
                .map(m -> new MessageResponse(
                        m.getId(),
                        m.getSender().getUsername(),
                        m.getRecipient(),
                        m.getContent(),
                        m.getStatus().name(),
                        m.getProvider().name(),
                        m.getProviderResponse(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    record UserMetricsResponse(String username, long totalSent, long remainingToday) {}


    @Operation(
            summary = "Metricas globales por usuario (ADMIN)",
            description = "Devuelve, para cada usuario, el total historico enviado y cuantos mensajes le quedan en el dia"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserMetricsResponse.class)))),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "Sin permisos (requiere rol ADMIN)")
    })

    @GetMapping("/metrics")
    public List<UserMetricsResponse> metrics() {
        ZoneId zone = ZoneId.systemDefault();
        Instant start = LocalDate.now(zone).atStartOfDay(zone).toInstant();
        Instant end   = start.plusSeconds(24 * 3600);

        return users.findAll().stream().map(u -> {
            long total     = messages.countBySender(u);
            long sentToday = messages.countBySenderAndCreatedAtBetween(u, start, end);
            long remaining = Math.max(0, u.getDailyLimit() - sentToday);
            return new UserMetricsResponse(u.getUsername(), total, remaining);
        }).toList();
    }
}

