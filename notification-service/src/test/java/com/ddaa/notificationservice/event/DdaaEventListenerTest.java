package com.ddaa.notificationservice.event;

import com.ddaa.notificationservice.service.DdaaEmailNotificationService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de DdaaEventListener.
 *
 * Se valida la lógica del listener sin levantar RabbitMQ.
 * El ObjectMapper y el servicio de email se mockean para controlar los escenarios.
 */
class DdaaEventListenerTest {

    @Test
    void handleDdaaEventShouldDeserializePayloadAndSendNotification() throws Exception {
        // Arrange: mensaje JSON simulado recibido desde RabbitMQ.
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        DdaaEmailNotificationService emailService = mock(DdaaEmailNotificationService.class);
        DdaaEventListener listener = new DdaaEventListener(objectMapper, emailService);

        DdaaEvent event = sampleEvent(DdaaEventType.CREATED);

        when(objectMapper.readValue(anyString(), eq(DdaaEvent.class))).thenReturn(event);

        byte[] body = """
                {
                  "eventType": "CREATED",
                  "ddaaId": 10
                }
                """.getBytes(StandardCharsets.UTF_8);

        // Act: el listener procesa el mensaje crudo.
        listener.handleDdaaEvent(body);

        // Assert: el JSON se deserializa y el evento se entrega al servicio de email.
        verify(objectMapper).readValue(anyString(), eq(DdaaEvent.class));
        verify(emailService).sendDdaaEventNotification(event);
    }

    @Test
    void handleDdaaEventShouldNotSendNotificationWhenPayloadIsInvalid() throws Exception {
        // Arrange: el ObjectMapper falla al deserializar el mensaje.
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        DdaaEmailNotificationService emailService = mock(DdaaEmailNotificationService.class);
        DdaaEventListener listener = new DdaaEventListener(objectMapper, emailService);

        when(objectMapper.readValue(anyString(), eq(DdaaEvent.class)))
                .thenThrow(new IllegalArgumentException("JSON inválido"));

        byte[] body = "mensaje-invalido".getBytes(StandardCharsets.UTF_8);

        // Act: el listener captura el error internamente para evitar reintentos infinitos.
        listener.handleDdaaEvent(body);

        // Assert: si el payload no se puede convertir, no se envía email.
        verify(emailService, never()).sendDdaaEventNotification(org.mockito.ArgumentMatchers.any(DdaaEvent.class));
    }

    /**
     * Evento DDAA reutilizable para simular deserialización exitosa.
     */
    private DdaaEvent sampleEvent(DdaaEventType eventType) {
        return new DdaaEvent(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                eventType,
                10L,
                "001",
                11111111L,
                5L,
                3L,
                "Fuente Derecho Test",
                "Consuntivo",
                "Permanente",
                "Activo",
                Instant.parse("2026-06-20T00:00:00Z")
        );
    }
}