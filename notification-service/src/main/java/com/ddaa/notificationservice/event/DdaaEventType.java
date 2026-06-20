package com.ddaa.notificationservice.event;

/**
 * Tipos de eventos DDAA que puede recibir notification-service.
 *
 * Estos valores deben coincidir con los eventos publicados por ddaa-service.
 */
public enum DdaaEventType {

    /**
     * Evento emitido cuando se crea un DDAA.
     */
    CREATED,

    /**
     * Evento emitido cuando se actualiza un DDAA.
     */
    UPDATED,

    /**
     * Evento emitido cuando se elimina un DDAA.
     */
    DELETED
}