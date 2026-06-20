package com.ddaa.authservice.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de UnauthorizedDomainException.
 *
 * Esta excepción traduce errores de dominio Google a un OAuth2AuthenticationException
 * con código controlado para el flujo de seguridad.
 */
class UnauthorizedDomainExceptionTest {

    @Test
    void constructorShouldExposeInvalidDomainOAuth2Error() {
        // Arrange & Act: se crea la excepción con mensaje de dominio no autorizado.
        UnauthorizedDomainException exception =
                new UnauthorizedDomainException("Unauthorized Google Workspace domain");

        // Assert: el error OAuth2 conserva código y descripción esperados.
        assertThat(exception.getError().getErrorCode()).isEqualTo("invalid_domain");
        assertThat(exception.getError().getDescription()).isEqualTo("Unauthorized Google Workspace domain");
        assertThat(exception.getMessage()).contains("Unauthorized Google Workspace domain");
    }
}