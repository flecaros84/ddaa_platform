package com.ddaa.ddaaservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

/**
 * Configuración central de RabbitMQ para los eventos del dominio DDAA.
 *
 * En esta rama, ddaa-service publicará eventos cada vez que un derecho de agua
 * sea creado, actualizado o eliminado.
 *
 * Por ahora definimos:
 * - un exchange directo;
 * - tres routing keys;
 * - una cola de validación para comprobar que el flujo funciona.
 */
@Configuration
public class DdaaRabbitMqConfig {

    /**
     * Exchange principal para eventos relacionados con DDAA.
     */
    public static final String DDAA_EVENTS_EXCHANGE = "ddaa.events.exchange";

    /**
     * Routing key usada cuando se crea un DDAA.
     */
    public static final String DDAA_CREATED_ROUTING_KEY = "ddaa.created";

    /**
     * Routing key usada cuando se actualiza un DDAA.
     */
    public static final String DDAA_UPDATED_ROUTING_KEY = "ddaa.updated";

    /**
     * Routing key usada cuando se elimina un DDAA.
     */
    public static final String DDAA_DELETED_ROUTING_KEY = "ddaa.deleted";

    /**
     * Cola temporal de validación.
     *
     * Más adelante esta cola podrá ser consumida por notification-service
     * para enviar correos.
     */
    public static final String DDAA_NOTIFICATION_QUEUE = "ddaa.notification.queue";

    /**
     * Declara el exchange directo de eventos DDAA.
     *
     * Un direct exchange enruta mensajes según la routing key exacta.
     */
    @Bean
    public DirectExchange ddaaEventsExchange() {
        return new DirectExchange(DDAA_EVENTS_EXCHANGE);
    }

    /**
     * Declara la cola que recibirá eventos para validación local.
     */
    @Bean
    public Queue ddaaNotificationQueue() {
        return new Queue(DDAA_NOTIFICATION_QUEUE, true);
    }

    /**
     * Enlaza la cola con eventos de creación.
     */
    @Bean
    public Binding ddaaCreatedBinding(
            Queue ddaaNotificationQueue,
            DirectExchange ddaaEventsExchange
    ) {
        return BindingBuilder
                .bind(ddaaNotificationQueue)
                .to(ddaaEventsExchange)
                .with(DDAA_CREATED_ROUTING_KEY);
    }

    /**
     * Enlaza la cola con eventos de actualización.
     */
    @Bean
    public Binding ddaaUpdatedBinding(
            Queue ddaaNotificationQueue,
            DirectExchange ddaaEventsExchange
    ) {
        return BindingBuilder
                .bind(ddaaNotificationQueue)
                .to(ddaaEventsExchange)
                .with(DDAA_UPDATED_ROUTING_KEY);
    }

    /**
     * Enlaza la cola con eventos de eliminación.
     */
    @Bean
    public Binding ddaaDeletedBinding(
            Queue ddaaNotificationQueue,
            DirectExchange ddaaEventsExchange
    ) {
        return BindingBuilder
                .bind(ddaaNotificationQueue)
                .to(ddaaEventsExchange)
                .with(DDAA_DELETED_ROUTING_KEY);
    }

    /**
     * Conversor JSON para enviar objetos Java como mensajes legibles.
     *
     * Sin este conversor, RabbitTemplate puede intentar enviar objetos usando
     * serialización Java. Para eventos entre microservicios es preferible JSON.
     */
    /**
     * Conversor JSON para enviar objetos Java como mensajes legibles.
     *
     * Spring AMQP usa este conversor cuando RabbitTemplate envía objetos
     * mediante convertAndSend. Así evitamos serialización Java y dejamos
     * los eventos preparados para ser consumidos por otros microservicios.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}