package com.ddaa.ddaaservice.event;

import com.ddaa.ddaaservice.config.DdaaRabbitMqConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Publicador de eventos DDAA hacia RabbitMQ.
 *
 * Esta clase centraliza el envío de mensajes para que el servicio de dominio
 * no tenga que conocer detalles técnicos del exchange ni de las routing keys.
 */
@Component
public class DdaaEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public DdaaEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica un evento DDAA usando la routing key correspondiente
     * según el tipo de evento.
     *
     * @param event evento de dominio que será enviado a RabbitMQ.
     */
    public void publish(DdaaEvent event) {
        String routingKey = resolveRoutingKey(event.eventType());

        rabbitTemplate.convertAndSend(
                DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                routingKey,
                event
        );
    }

    /**
     * Traduce el tipo de evento del dominio a la routing key de RabbitMQ.
     */
    private String resolveRoutingKey(DdaaEventType eventType) {
        return switch (eventType) {
            case CREATED -> DdaaRabbitMqConfig.DDAA_CREATED_ROUTING_KEY;
            case UPDATED -> DdaaRabbitMqConfig.DDAA_UPDATED_ROUTING_KEY;
            case DELETED -> DdaaRabbitMqConfig.DDAA_DELETED_ROUTING_KEY;
        };
    }

    /**
     * Publica el evento solamente después de que la transacción de base de datos
     * se confirme correctamente.
     *
     * Esto evita informar por RabbitMQ un cambio que luego pudiera fallar
     * o revertirse en la base de datos.
     *
     * @param event evento de dominio que será enviado a RabbitMQ.
     */
    public void publishAfterCommit(DdaaEvent event) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            publish(event);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                publish(event);
            }
        });
    }
}