package com.ddaa.ddaaservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de ApiExceptionHandler.
 *
 * Se valida que las excepciones de dominio usadas para "no encontrado"
 * se transformen en una respuesta HTTP 404 con cuerpo consistente.
 */
class ApiExceptionHandlerTest {

    @Test
    void handleNotFoundShouldReturn404Body() {
        // Arrange: excepción equivalente a un DDAA no encontrado.
        ApiExceptionHandler handler = new ApiExceptionHandler();
        IllegalArgumentException exception = new IllegalArgumentException("No se encontro el derecho de agua 99");

        // Act: el handler transforma la excepción en respuesta HTTP.
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(exception);

        // Assert: se mantiene contrato de error usado por la API.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("error", "not_found");
        assertThat(response.getBody()).containsEntry("message", "No se encontro el derecho de agua 99");
    }
}