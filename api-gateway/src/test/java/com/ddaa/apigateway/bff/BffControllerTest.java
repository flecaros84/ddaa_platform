package com.ddaa.apigateway.bff;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de BffController.
 *
 * El controller no contiene lógica de negocio compleja.
 * Por eso se valida que delega correctamente cada endpoint al BffService.
 */
@ExtendWith(MockitoExtension.class)
class BffControllerTest {

    @Mock
    private BffService bffService;

    @InjectMocks
    private BffController controller;

    @Test
    void sessionShouldDelegateToService() {
        // Arrange: exchange simulado y respuesta esperada.
        ServerWebExchange exchange = exchange("/bff/session");
        Mono<Map<String, Object>> expected = Mono.just(Map.of("authenticated", true));

        when(bffService.getSession(exchange)).thenReturn(expected);

        // Act: se llama al endpoint de sesión.
        Mono<Map<String, Object>> result = controller.session(exchange);

        // Assert: el controller retorna exactamente el Mono del service.
        assertThat(result).isSameAs(expected);
    }

    @Test
    void listDdaaShouldDelegateToService() {
        // Arrange: respuesta esperada desde el service.
        ServerWebExchange exchange = exchange("/bff/ddaa");
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.ok(Map.of("ok", true)));

        when(bffService.listDdaa(exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa.
        assertThat(controller.listDdaa(exchange)).isSameAs(expected);
    }

    @Test
    void getDdaaFormOptionsShouldDelegateToService() {
        // Arrange: respuesta esperada para opciones de formulario.
        ServerWebExchange exchange = exchange("/bff/ddaa/form-options");
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.ok(Map.of("comunas", java.util.List.of())));

        when(bffService.getDdaaFormOptions(exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa.
        assertThat(controller.getDdaaFormOptions(exchange)).isSameAs(expected);
    }

    @Test
    void getDdaaShouldDelegateToService() {
        // Arrange: respuesta esperada para detalle DDAA.
        ServerWebExchange exchange = exchange("/bff/ddaa/10");
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.ok(Map.of("id", 10)));

        when(bffService.getDdaa(10L, exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa con path variable.
        assertThat(controller.getDdaa(10L, exchange)).isSameAs(expected);
    }

    @Test
    void createDdaaShouldDelegateToService() {
        // Arrange: payload reactivo y respuesta esperada.
        ServerWebExchange exchange = exchange("/bff/ddaa");
        Mono<Map<String, Object>> payload = Mono.just(Map.of("estadoDerecho", "Activo"));
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.status(201).body(Map.of("id", 99)));

        when(bffService.createDdaa(payload, exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa del POST.
        assertThat(controller.createDdaa(payload, exchange)).isSameAs(expected);
    }

    @Test
    void updateDdaaShouldDelegateToService() {
        // Arrange: payload reactivo y respuesta esperada.
        ServerWebExchange exchange = exchange("/bff/ddaa/10");
        Mono<Map<String, Object>> payload = Mono.just(Map.of("estadoDerecho", "Inactivo"));
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.noContent().build());

        when(bffService.updateDdaa(10L, payload, exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa del PUT.
        assertThat(controller.updateDdaa(10L, payload, exchange)).isSameAs(expected);
    }

    @Test
    void deleteDdaaShouldDelegateToService() {
        // Arrange: respuesta esperada para delete.
        ServerWebExchange exchange = exchange("/bff/ddaa/10");
        Mono<ResponseEntity<Object>> expected = Mono.just(ResponseEntity.noContent().build());

        when(bffService.deleteDdaa(10L, exchange)).thenReturn(expected);

        // Act & Assert: se valida delegación directa del DELETE.
        assertThat(controller.deleteDdaa(10L, exchange)).isSameAs(expected);
    }

    /**
     * Construye un exchange mínimo para pruebas unitarias del controller.
     */
    private ServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }
}