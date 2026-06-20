package com.ddaa.ddaaservice.event;

import com.ddaa.ddaaservice.config.DdaaRabbitMqConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Pruebas unitarias de DdaaEventPublisher.
 *
 * Se valida la integración lógica con RabbitTemplate sin conectarse a RabbitMQ.
 * Esto cubre la resolución de routing keys y el envío diferido post-commit.
 */
class DdaaEventPublisherTest {

    @Test
    void publishShouldSendCreatedEventWithCreatedRoutingKey() {
        // Arrange: se usa RabbitTemplate mockeado para evitar conexión real a RabbitMQ.
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        DdaaEventPublisher publisher = new DdaaEventPublisher(rabbitTemplate);
        DdaaEvent event = sampleEvent(DdaaEventType.CREATED);

        // Act: se publica un evento CREATED.
        publisher.publish(event);

        // Assert: el evento se envía al exchange con la routing key de creación.
        verify(rabbitTemplate).convertAndSend(
                DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                DdaaRabbitMqConfig.DDAA_CREATED_ROUTING_KEY,
                event
        );
    }

    @Test
    void publishShouldSendUpdatedEventWithUpdatedRoutingKey() {
        // Arrange: se prepara un evento UPDATED.
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        DdaaEventPublisher publisher = new DdaaEventPublisher(rabbitTemplate);
        DdaaEvent event = sampleEvent(DdaaEventType.UPDATED);

        // Act: se publica el evento.
        publisher.publish(event);

        // Assert: se usa la routing key de actualización.
        verify(rabbitTemplate).convertAndSend(
                DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                DdaaRabbitMqConfig.DDAA_UPDATED_ROUTING_KEY,
                event
        );
    }

    @Test
    void publishShouldSendDeletedEventWithDeletedRoutingKey() {
        // Arrange: se prepara un evento DELETED.
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        DdaaEventPublisher publisher = new DdaaEventPublisher(rabbitTemplate);
        DdaaEvent event = sampleEvent(DdaaEventType.DELETED);

        // Act: se publica el evento.
        publisher.publish(event);

        // Assert: se usa la routing key de eliminación.
        verify(rabbitTemplate).convertAndSend(
                DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                DdaaRabbitMqConfig.DDAA_DELETED_ROUTING_KEY,
                event
        );
    }

    @Test
    void publishAfterCommitShouldSendImmediatelyWhenThereIsNoActiveTransaction() {
        // Arrange: sin transacción activa, el publicador debe enviar inmediatamente.
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        DdaaEventPublisher publisher = new DdaaEventPublisher(rabbitTemplate);
        DdaaEvent event = sampleEvent(DdaaEventType.CREATED);

        // Act: se solicita publicación post-commit sin sincronización transaccional activa.
        publisher.publishAfterCommit(event);

        // Assert: al no existir transacción, el envío ocurre de inmediato.
        verify(rabbitTemplate).convertAndSend(
                DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                DdaaRabbitMqConfig.DDAA_CREATED_ROUTING_KEY,
                event
        );
    }

    @Test
    void publishAfterCommitShouldWaitUntilTransactionCommitWhenSynchronizationIsActive() {
        // Arrange: se activa sincronización transaccional simulada.
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        DdaaEventPublisher publisher = new DdaaEventPublisher(rabbitTemplate);
        DdaaEvent event = sampleEvent(DdaaEventType.UPDATED);

        TransactionSynchronizationManager.initSynchronization();

        try {
            // Act: se registra el envío para después del commit.
            publisher.publishAfterCommit(event);

            // Assert: antes del commit no debe enviarse nada.
            verifyNoInteractions(rabbitTemplate);

            // Act: se simula el afterCommit que Spring ejecutaría al confirmar la transacción.
            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            // Assert: después del commit, el evento se envía a RabbitMQ.
            verify(rabbitTemplate).convertAndSend(
                    DdaaRabbitMqConfig.DDAA_EVENTS_EXCHANGE,
                    DdaaRabbitMqConfig.DDAA_UPDATED_ROUTING_KEY,
                    event
            );
        } finally {
            // Limpieza obligatoria para no contaminar otros tests.
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private DdaaEvent sampleEvent(DdaaEventType type) {
        return DdaaEvent.of(
                type,
                10L,
                "001",
                11111111L,
                5L,
                3L,
                "Fuente Derecho Test",
                "Consuntivo",
                "Permanente",
                "Activo"
        );
    }
}