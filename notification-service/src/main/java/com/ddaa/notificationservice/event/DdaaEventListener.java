package com.ddaa.notificationservice.event;

import com.ddaa.notificationservice.config.RabbitMqConfig;
import com.ddaa.notificationservice.service.DdaaEmailNotificationService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * Listener RabbitMQ para eventos DDAA.
 *
 * Este componente consume mensajes desde la cola de notificaciones.
 * Al recibir un evento válido, envía una notificación por email.
 */
@Component
public class DdaaEventListener {

    private final ObjectMapper objectMapper;
    private final DdaaEmailNotificationService emailNotificationService;

    public DdaaEventListener(
            ObjectMapper objectMapper,
            DdaaEmailNotificationService emailNotificationService
    ) {
        this.objectMapper = objectMapper;
        this.emailNotificationService = emailNotificationService;
    }

    /**
     * Consume eventos DDAA desde RabbitMQ.
     *
     * Recibimos el cuerpo como bytes para evitar que Spring intente deserializar
     * automáticamente usando el header __TypeId__ generado por ddaa-service.
     *
     * @param body cuerpo crudo del mensaje RabbitMQ.
     */
    @RabbitListener(queues = RabbitMqConfig.DDAA_NOTIFICATION_QUEUE)
    public void handleDdaaEvent(byte[] body) {
        try {
            // Convertimos manualmente el cuerpo del mensaje a texto JSON.
            String payload = new String(body, StandardCharsets.UTF_8);

            // Convertimos manualmente el JSON al DTO local de notification-service.
            DdaaEvent event = objectMapper.readValue(payload, DdaaEvent.class);

            System.out.println("==================================================");
            System.out.println("Evento DDAA recibido en notification-service");
            System.out.println("Tipo evento: " + event.eventType());
            System.out.println("ID DDAA: " + event.ddaaId());
            System.out.println("Nombre fuente derecho: " + event.nombreFuenteDerecho());
            System.out.println("Estado derecho: " + event.estadoDerecho());
            System.out.println("Fecha evento: " + event.occurredAt());
            System.out.println("==================================================");

            // Enviamos la notificación por email.
            emailNotificationService.sendDdaaEventNotification(event);

            System.out.println("Email de notificación enviado correctamente para DDAA ID: " + event.ddaaId());

        } catch (Exception ex) {
            // Capturamos el error para evitar que el mensaje entre en un ciclo de reintentos infinito.
            System.err.println("Error al procesar evento DDAA desde RabbitMQ");
            ex.printStackTrace();
        }
    }
}