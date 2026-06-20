package com.ddaa.apigateway.bff;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de BffService.
 *
 * Se usa WebClient con ExchangeFunction simulado.
 * Así se prueba la lógica BFF sin levantar auth-service, ddaa-service, Eureka ni Gateway real.
 */
class BffServiceTest {

    @Test
    void getSessionShouldReturnAuthenticatedSessionAndForwardHeaders() {
        // Arrange: auth-service simulado responde sesión autenticada.
        ExchangeFunction authExchange = request -> {
            assertThat(request.url().getPath()).isEqualTo("/auth/me");
            assertForwardedHeaders(request);

            return Mono.just(jsonResponse(
                    HttpStatus.OK,
                    """
                    {
                      "authenticated": true,
                      "email": "usuario@camanchaca.cl",
                      "role": "ADMIN"
                    }
                    """
            ));
        };

        BffService service = service(authExchange, unusedDdaaExchange());
        ServerWebExchange exchange = authenticatedExchange("/bff/session");

        // Act: se consulta la sesión.
        Map<String, Object> result = service.getSession(exchange).block();

        // Assert: la respuesta de auth-service se propaga al frontend.
        assertThat(result).containsEntry("authenticated", true);
        assertThat(result).containsEntry("email", "usuario@camanchaca.cl");
        assertThat(result).containsEntry("role", "ADMIN");
    }

    @Test
    void getSessionShouldReturnAnonymousWhenAuthServiceFails() {
        // Arrange: auth-service simulado falla.
        ExchangeFunction authExchange = request -> Mono.error(new IllegalStateException("auth unavailable"));

        BffService service = service(authExchange, unusedDdaaExchange());

        // Act: se consulta la sesión.
        Map<String, Object> result = service.getSession(exchange("/bff/session")).block();

        // Assert: ante error externo, el BFF retorna sesión anónima controlada.
        assertThat(result).containsEntry("authenticated", false);
    }

    @Test
    void listDdaaShouldProxyRequestToDdaaService() {
        // Arrange: ddaa-service simulado retorna listado.
        ExchangeFunction ddaaExchange = request -> {
            assertThat(request.method().name()).isEqualTo("GET");
            assertThat(request.url().getPath()).isEqualTo("/api/ddaa");
            assertForwardedHeaders(request);

            return Mono.just(jsonResponse(
                    HttpStatus.OK,
                    """
                    [
                      { "id": 1, "estadoDerecho": "Activo" }
                    ]
                    """
            ));
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se lista DDAA vía BFF.
        ResponseEntity<Object> response = service.listDdaa(authenticatedExchange("/bff/ddaa")).block();

        // Assert: se conserva status y body recibido desde ddaa-service.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(List.class);
    }

    @Test
    void getDdaaShouldProxyRequestToDdaaService() {
        // Arrange: ddaa-service simulado retorna detalle.
        ExchangeFunction ddaaExchange = request -> {
            assertThat(request.method().name()).isEqualTo("GET");
            assertThat(request.url().getPath()).isEqualTo("/api/ddaa/10");

            return Mono.just(jsonResponse(
                    HttpStatus.OK,
                    """
                    { "ddaa": { "id": 10, "estadoDerecho": "Activo" } }
                    """
            ));
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se consulta detalle vía BFF.
        ResponseEntity<Object> response = service.getDdaa(10L, exchange("/bff/ddaa/10")).block();

        // Assert: el body JSON se transforma a Map.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    void getDdaaFormOptionsShouldAggregateCatalogResponses() {
        // Arrange: cada catálogo responde una lista mínima.
        ExchangeFunction ddaaExchange = request -> {
            String path = request.url().getPath();

            return Mono.just(jsonResponse(
                    HttpStatus.OK,
                    """
                    [
                      { "nombre": "%s" }
                    ]
                    """.formatted(path.substring(path.lastIndexOf("/") + 1))
            ));
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: el BFF consulta catálogos y construye un único objeto de opciones.
        ResponseEntity<Object> response = service.getDdaaFormOptions(exchange("/bff/ddaa/form-options")).block();

        // Assert: el mapa contiene todas las claves requeridas por el frontend.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertThat(body).containsKeys(
                "comunas",
                "ruts",
                "instalaciones",
                "cuencas",
                "subcuencas",
                "fuentes"
        );
    }

    @Test
    void createDdaaShouldProxyPostBodyToDdaaService() {
        // Arrange: ddaa-service simulado responde creación exitosa.
        ExchangeFunction ddaaExchange = request -> {
            assertThat(request.method().name()).isEqualTo("POST");
            assertThat(request.url().getPath()).isEqualTo("/api/ddaa");

            return Mono.just(jsonResponse(
                    HttpStatus.CREATED,
                    """
                    { "id": 99 }
                    """
            ));
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se crea DDAA vía BFF.
        ResponseEntity<Object> response = service.createDdaa(
                Mono.just(Map.of("estadoDerecho", "Activo")),
                exchange("/bff/ddaa")
        ).block();

        // Assert: se conserva el 201 Created del backend.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    void updateDdaaShouldProxyPutBodyToDdaaService() {
        // Arrange: ddaa-service simulado responde actualización sin cuerpo.
        ExchangeFunction ddaaExchange = request -> {
            assertThat(request.method().name()).isEqualTo("PUT");
            assertThat(request.url().getPath()).isEqualTo("/api/ddaa/10");

            return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se actualiza DDAA vía BFF.
        ResponseEntity<Object> response = service.updateDdaa(
                10L,
                Mono.just(Map.of("estadoDerecho", "Inactivo")),
                exchange("/bff/ddaa/10")
        ).block();

        // Assert: se conserva el 204 No Content.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void deleteDdaaShouldProxyDeleteToDdaaService() {
        // Arrange: ddaa-service simulado responde eliminación sin cuerpo.
        ExchangeFunction ddaaExchange = request -> {
            assertThat(request.method().name()).isEqualTo("DELETE");
            assertThat(request.url().getPath()).isEqualTo("/api/ddaa/10");

            return Mono.just(ClientResponse.create(HttpStatus.NO_CONTENT).build());
        };

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se elimina DDAA vía BFF.
        ResponseEntity<Object> response = service.deleteDdaa(10L, exchange("/bff/ddaa/10")).block();

        // Assert: se conserva el 204 No Content.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void ddaaErrorResponseShouldReturnDefaultBodyWhenBackendBodyIsEmpty() {
        // Arrange: ddaa-service simulado retorna error sin body.
        ExchangeFunction ddaaExchange = request -> Mono.just(ClientResponse.create(HttpStatus.NOT_FOUND).build());

        BffService service = service(unusedAuthExchange(), ddaaExchange);

        // Act: se consulta un DDAA inexistente.
        ResponseEntity<Object> response = service.getDdaa(404L, exchange("/bff/ddaa/404")).block();

        // Assert: el BFF entrega un body de error controlado.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "Error al procesar la solicitud DDAA"));
    }

    /**
     * Construye BffService con WebClient simulados para auth-service y ddaa-service.
     */
    private BffService service(ExchangeFunction authExchange, ExchangeFunction ddaaExchange) {
        WebClient authClient = WebClient.builder()
                .baseUrl("http://auth-service")
                .exchangeFunction(authExchange)
                .build();

        WebClient ddaaClient = WebClient.builder()
                .baseUrl("http://ddaa-service")
                .exchangeFunction(ddaaExchange)
                .build();

        WebClient.Builder builder = mock(WebClient.Builder.class);

        when(builder.baseUrl("http://auth-service")).thenReturn(builder);
        when(builder.baseUrl("http://ddaa-service")).thenReturn(builder);
        when(builder.build()).thenReturn(authClient, ddaaClient);

        return new BffService(builder);
    }

    /**
     * Exchange con headers de sesión para validar forwarding hacia servicios internos.
     */
    private ServerWebExchange authenticatedExchange(String path) {
        return MockServerWebExchange.from(
                MockServerHttpRequest.get(path)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token")
                        .header(HttpHeaders.COOKIE, "SESSION=test-session")
        );
    }

    /**
     * Exchange mínimo sin headers.
     */
    private ServerWebExchange exchange(String path) {
        return MockServerWebExchange.from(MockServerHttpRequest.get(path));
    }

    /**
     * Respuesta JSON simulada para WebClient.
     */
    private ClientResponse jsonResponse(HttpStatus status, String body) {
        return ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(body)
                .build();
    }

    /**
     * Valida que el BFF copie cookie y authorization al request interno.
     */
    private void assertForwardedHeaders(ClientRequest request) {
        assertThat(request.headers().getFirst(HttpHeaders.AUTHORIZATION)).isEqualTo("Bearer test-token");
        assertThat(request.headers().getFirst(HttpHeaders.COOKIE)).isEqualTo("SESSION=test-session");
    }

    private ExchangeFunction unusedAuthExchange() {
        return request -> Mono.error(new AssertionError("auth-service no debia ser llamado en este test"));
    }

    private ExchangeFunction unusedDdaaExchange() {
        return request -> Mono.error(new AssertionError("ddaa-service no debia ser llamado en este test"));
    }
}