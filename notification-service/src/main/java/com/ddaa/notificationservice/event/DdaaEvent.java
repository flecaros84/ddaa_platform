package com.ddaa.notificationservice.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento recibido desde RabbitMQ cuando ocurre un cambio en un DDAA.
 *
 * Este record replica el contrato publicado por ddaa-service.
 * No contiene lógica de negocio; solo transporta datos para la notificación.
 */
public record DdaaEvent(

        /**
         * Identificador único del evento.
         */
        UUID eventId,

        /**
         * Tipo de evento recibido: CREATED, UPDATED o DELETED.
         */
        DdaaEventType eventType,

        /**
         * Identificador del DDAA afectado.
         */
        Long ddaaId,

        /**
         * Comuna asociada al DDAA.
         */
        String comunaId,

        /**
         * RUT del titular asociado al DDAA.
         */
        Long rutTitular,

        /**
         * Instalación asociada al DDAA, si existe.
         */
        Long instalacionId,

        /**
         * Fuente asociada al DDAA.
         */
        Long fuenteId,

        /**
         * Nombre de la fuente del derecho.
         */
        String nombreFuenteDerecho,

        /**
         * Naturaleza del derecho.
         */
        String naturalezaDerecho,

        /**
         * Tipo de derecho.
         */
        String tipoDerecho,

        /**
         * Estado actual del derecho.
         */
        String estadoDerecho,

        /**
         * Fecha y hora en que se generó el evento.
         */
        Instant occurredAt
) {
}
