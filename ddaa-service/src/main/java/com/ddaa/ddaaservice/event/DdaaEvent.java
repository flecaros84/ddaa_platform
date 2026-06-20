package com.ddaa.ddaaservice.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de dominio publicado por ddaa-service cuando ocurre un cambio
 * relevante sobre un derecho de agua.
 *
 * Este DTO está pensado para viajar por RabbitMQ en formato JSON.
 * Más adelante notification-service podrá consumirlo para enviar correos.
 */
public record DdaaEvent(

        /**
         * Identificador único del evento.
         * Sirve para trazabilidad y para evitar confundir dos mensajes distintos.
         */
        UUID eventId,

        /**
         * Tipo de evento: CREATED, UPDATED o DELETED.
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
         * Este dato será útil para construir una notificación entendible por email.
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

    /**
     * Crea un evento nuevo asignando automáticamente ID de evento y fecha.
     */
    public static DdaaEvent of(
            DdaaEventType eventType,
            Long ddaaId,
            String comunaId,
            Long rutTitular,
            Long instalacionId,
            Long fuenteId,
            String nombreFuenteDerecho,
            String naturalezaDerecho,
            String tipoDerecho,
            String estadoDerecho
    ) {
        return new DdaaEvent(
                UUID.randomUUID(),
                eventType,
                ddaaId,
                comunaId,
                rutTitular,
                instalacionId,
                fuenteId,
                nombreFuenteDerecho,
                naturalezaDerecho,
                tipoDerecho,
                estadoDerecho,
                Instant.now()
        );
    }
}