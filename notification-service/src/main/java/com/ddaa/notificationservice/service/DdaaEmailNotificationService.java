package com.ddaa.notificationservice.service;

import com.ddaa.notificationservice.event.DdaaEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de enviar notificaciones por email
 * cuando notification-service recibe eventos DDAA desde RabbitMQ.
 */
@Service
public class DdaaEmailNotificationService {

    private final JavaMailSender mailSender;
    private final String mailFrom;
    private final String mailTo;

    public DdaaEmailNotificationService(
            JavaMailSender mailSender,
            @Value("${app.mail.from}") String mailFrom,
            @Value("${app.mail.to}") String mailTo
    ) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
        this.mailTo = mailTo;
    }

    /**
     * Envía una notificación por email a partir de un evento DDAA.
     *
     * @param event evento recibido desde RabbitMQ.
     */
    public void sendDdaaEventNotification(DdaaEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();

        // Definimos remitente y destinatario desde application.yml/local.properties.
        message.setFrom(mailFrom);
        message.setTo(mailTo);

        // Construimos asunto y cuerpo usando los datos del evento.
        message.setSubject(buildSubject(event));
        message.setText(buildBody(event));

        // Enviamos el correo usando la configuración SMTP de Spring.
        mailSender.send(message);
    }

    /**
     * Construye el asunto del correo según el tipo de evento.
     */
    private String buildSubject(DdaaEvent event) {
        return switch (event.eventType()) {
            case CREATED -> "DDAA creado: " + event.nombreFuenteDerecho();
            case UPDATED -> "DDAA actualizado: " + event.nombreFuenteDerecho();
            case DELETED -> "DDAA eliminado: " + event.nombreFuenteDerecho();
        };
    }

    /**
     * Construye el cuerpo del correo con la información principal del DDAA.
     */
    private String buildBody(DdaaEvent event) {
        return """
                Se ha registrado un evento en la plataforma DDAA.

                Tipo de evento: %s
                ID DDAA: %s
                Nombre fuente derecho: %s
                Estado derecho: %s
                Naturaleza derecho: %s
                Tipo derecho: %s
                Comuna ID: %s
                RUT titular: %s
                Instalación ID: %s
                Fuente ID: %s
                Fecha evento: %s

                Este correo fue enviado automáticamente por notification-service.
                """.formatted(
                event.eventType(),
                event.ddaaId(),
                event.nombreFuenteDerecho(),
                event.estadoDerecho(),
                event.naturalezaDerecho(),
                event.tipoDerecho(),
                event.comunaId(),
                event.rutTitular(),
                event.instalacionId(),
                event.fuenteId(),
                event.occurredAt()
        );
    }
}