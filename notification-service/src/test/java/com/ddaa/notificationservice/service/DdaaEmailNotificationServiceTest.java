package com.ddaa.notificationservice.service;

import com.ddaa.notificationservice.event.DdaaEvent;
import com.ddaa.notificationservice.event.DdaaEventType;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Pruebas unitarias de DdaaEmailNotificationService.
 *
 * Se valida la construcción del email sin usar SMTP real.
 * JavaMailSender se mockea para capturar el SimpleMailMessage generado.
 */
class DdaaEmailNotificationServiceTest {

    @Test
    void sendDdaaEventNotificationShouldSendCreatedEmail() {
        // Arrange: evento CREATED recibido desde RabbitMQ.
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DdaaEmailNotificationService service = new DdaaEmailNotificationService(
                mailSender,
                "notification-test@local.test",
                "admin-test@local.test"
        );

        DdaaEvent event = sampleEvent(DdaaEventType.CREATED);

        // Act: se genera y envía la notificación.
        service.sendDdaaEventNotification(event);

        // Assert: se captura el correo enviado y se valida su contrato principal.
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getFrom()).isEqualTo("notification-test@local.test");
        assertThat(message.getTo()).containsExactly("admin-test@local.test");
        assertThat(message.getSubject()).isEqualTo("DDAA creado: Fuente Derecho Test");

        assertThat(message.getText())
                .contains("Tipo de evento: CREATED")
                .contains("ID DDAA: 10")
                .contains("Nombre fuente derecho: Fuente Derecho Test")
                .contains("Estado derecho: Activo")
                .contains("RUT titular: 11111111");
    }

    @Test
    void sendDdaaEventNotificationShouldSendUpdatedEmail() {
        // Arrange: evento UPDATED recibido desde RabbitMQ.
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DdaaEmailNotificationService service = new DdaaEmailNotificationService(
                mailSender,
                "notification-test@local.test",
                "admin-test@local.test"
        );

        DdaaEvent event = sampleEvent(DdaaEventType.UPDATED);

        // Act: se genera y envía la notificación.
        service.sendDdaaEventNotification(event);

        // Assert: el asunto debe reflejar actualización.
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getSubject()).isEqualTo("DDAA actualizado: Fuente Derecho Test");
        assertThat(message.getText())
                .contains("Tipo de evento: UPDATED")
                .contains("Fuente ID: 3");
    }

    @Test
    void sendDdaaEventNotificationShouldSendDeletedEmail() {
        // Arrange: evento DELETED recibido desde RabbitMQ.
        JavaMailSender mailSender = mock(JavaMailSender.class);
        DdaaEmailNotificationService service = new DdaaEmailNotificationService(
                mailSender,
                "notification-test@local.test",
                "admin-test@local.test"
        );

        DdaaEvent event = sampleEvent(DdaaEventType.DELETED);

        // Act: se genera y envía la notificación.
        service.sendDdaaEventNotification(event);

        // Assert: el asunto debe reflejar eliminación.
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        SimpleMailMessage message = messageCaptor.getValue();

        assertThat(message.getSubject()).isEqualTo("DDAA eliminado: Fuente Derecho Test");
        assertThat(message.getText())
                .contains("Tipo de evento: DELETED")
                .contains("Este correo fue enviado automáticamente por notification-service.");
    }

    /**
     * Evento DDAA reutilizable para validar asunto y cuerpo del correo.
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