package com.ddaa.apigateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de AuthenticatedApiFilter.
 *
 * Se valida la regla de protección del gateway sin levantar servidor real.
 * El auth-service se simula mediante WebClient con ExchangeFunction.
 */
class AuthenticatedApiFilterTest {

    @Test
    void filterShouldNotValidatePublicPaths() {
        // Arrange: una ruta pública no debe consultar auth-service.
        AuthenticatedApiFilter filter = filter(request ->
                Mono.error(new AssertionError("auth-service no debia ser llamado para rutas publicas"))
        );

        ServerWebExchange exchange = exchange("/auth/me");
        GatewayFilterChain chain = mockChain();

        // Act: se ejecuta el filtro.
        filter.filter(exchange, chain).block();

        // Assert: la cadena continúa sin marcar 401.
        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void filterShouldAllowProtectedRequestWhenSessionIsAuthenticated() {
        // Arrange: auth-service responde authenticated=true.
        ExchangeFunction authExchange = request -> {
            assertThat(request.url().getPath()).isEqualTo("/auth/me");
            assertForwardedHeaders(request);

            return Mono.just(jsonResponse(
                    HttpStatus.OK,
                    """
                    { "authenticated": true }
                    """
            ));
        };

        AuthenticatedApiFilter filter = filter(authExchange);
        ServerWebExchange exchange = authenticatedExchange("/api/ddaa");
        GatewayFilterChain chain = mockChain();

        // Act: se ejecuta el filtro sobre ruta protegida.
        filter.filter(exchange, chain).block();

        // Assert: la petición continúa.
        verify(chain).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void filterShouldRejectProtectedRequestWhenSessionIsAnonymous() {
        // Arrange: auth-service responde authenticated=false.
        ExchangeFunction authExchange = request -> Mono.just(jsonResponse(
                HttpStatus.OK,
                """
                { "authenticated": false }
                """
        ));

        AuthenticatedApiFilter filter = filter(authExchange);
        ServerWebExchange exchange = exchange("/api/ddaa");
        GatewayFilterChain chain = mockChain();

        // Act: se ejecuta el filtro.
        filter.filter(exchange, chain).block();

        // Assert: la petición no continúa y se marca 401.
        verify(chain, never()).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void filterShouldRejectProtectedBffDdaaRequestWhenAuthServiceFails() {
        // Arrange: auth-service falla.
        ExchangeFunction authExchange = request -> Mono.error(new IllegalStateException("auth unavailable"));

        AuthenticatedApiFilter filter = filter(authExchange);
        ServerWebExchange exchange = exchange("/bff/ddaa");
        GatewayFilterChain chain = mockChain();

        // Act: se ejecuta el filtro sobre ruta BFF protegida.
        filter.filter(exchange, chain).block();

        // Assert: por seguridad, cualquier error de auth-service produce 401.
        verify(chain, never()).filter(exchange);
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void getOrderShouldUseHighestPrecedence() {
        // Arrange: filtro con auth-service no usado en este test.
        AuthenticatedApiFilter filter = filter(request -> Mono.empty());

        // Act & Assert: el filtro debe ejecutarse antes que otros filtros.
        assertThat(filter.getOrder()).isEqualTo(Ordered.HIGHEST_PRECEDENCE);
    }

    /**
     * Construye el filtro usando WebClient simulado.
     */
    private AuthenticatedApiFilter filter(ExchangeFunction authExchange) {
        WebClient authClient = WebClient.builder()
                .baseUrl("http://auth-service")
                .exchangeFunction(authExchange)
                .build();

        WebClient.Builder builder = mock(WebClient.Builder.class);
        when(builder.baseUrl("http://auth-service")).thenReturn(builder);
        when(builder.build()).thenReturn(authClient);

        return new AuthenticatedApiFilter(builder);
    }

    /**
     * Cadena Gateway simulada que completa exitosamente.
     */
    private GatewayFilterChain mockChain() {
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(org.mockito.ArgumentMatchers.any(ServerWebExchange.class))).thenReturn(Mono.empty());
        return chain;
    }

    /**
     * Exchange mínimo sin headers.
     */
    private ServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

    /**
     * Exchange con headers que deben reenviarse a auth-service.
     */
    private ServerWebExchange authenticatedExchange(String path) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .header(HttpHeaders.COOKIE, "SESSION=test-session")
        );
    }

    /**
     * Respuesta JSON simulada para auth-service.
     */
    private ClientResponse jsonResponse(HttpStatus status, String body) {
        return ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build();
    }

    /**
     * Valida que el filtro copie cookie y authorization hacia auth-service.
     */
    private void assertForwardedHeaders(ClientRequest request) {
        assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test-token");
        assertThat(request.headers().getFirst(HttpHeaders.COOKIE)).isEqualTo("SESSION=test-session");
    }
}