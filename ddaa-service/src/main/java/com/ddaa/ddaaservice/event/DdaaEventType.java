package com.ddaa.ddaaservice.event;

/**
 * Tipos de eventos que puede publicar ddaa-service.
 *
 * Estos valores permiten que un consumidor, por ejemplo notification-service,
 * sepa qué ocurrió sobre un derecho de agua.
 */
public enum DdaaEventType {

    /**
     * Evento emitido cuando se crea un nuevo DDAA.
     */
    CREATED,

    /**
     * Evento emitido cuando se actualiza un DDAA existente.
     */
    UPDATED,

    /**
     * Evento emitido cuando se elimina un DDAA existente.
     */
    DELETED
}