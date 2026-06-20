package com.ddaa.notificationservice.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración RabbitMQ de notification-service.
 *
 * Este servicio consume eventos publicados por ddaa-service.
 * Por ahora solo necesitamos conocer el nombre de la cola de notificaciones.
 */
@Configuration
public class RabbitMqConfig {

    /**
     * Cola donde llegan los eventos DDAA que deben generar notificaciones.
     *
     * El nombre debe coincidir con la cola declarada por ddaa-service.
     */
    public static final String DDAA_NOTIFICATION_QUEUE = "ddaa.notification.queue";
}
