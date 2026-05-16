package com.ddaa.apigateway.bff;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BffService {

    private static final ParameterizedTypeReference<Map<String, Object>> SESSION_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient authClient;
    private final WebClient ddaaClient;

    public BffService(WebClient.Builder webClientBuilder) {
        this.authClient = webClientBuilder
                .baseUrl("http://auth-service")
                .build();
        this.ddaaClient = webClientBuilder
                .baseUrl("http://ddaa-service")
                .build();
    }

    public Mono<Map<String, Object>> getSession(ServerWebExchange exchange) {
        return authClient.get()
                .uri("/auth/me")
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return response.bodyToMono(SESSION_RESPONSE_TYPE)
                                .defaultIfEmpty(anonymousSession());
                    }

                    return response.releaseBody()
                            .thenReturn(anonymousSession());
                })
                .onErrorResume(ex -> Mono.just(anonymousSession()));
    }

    public Mono<ResponseEntity<Object>> listDdaa(ServerWebExchange exchange) {
        return ddaaClient.get()
                .uri("/api/ddaa")
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .exchangeToMono(this::toObjectResponse);
    }

    public Mono<ResponseEntity<Object>> getDdaa(long id, ServerWebExchange exchange) {
        return ddaaClient.get()
                .uri("/api/ddaa/{id}", id)
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .exchangeToMono(this::toObjectResponse);
    }

    public Mono<ResponseEntity<Object>> getDdaaFormOptions(ServerWebExchange exchange) {
        Mono<Object> comunas = getDdaaBody("/api/catalogos/comunas", exchange);
        Mono<Object> ruts = getDdaaBody("/api/catalogos/ruts", exchange);
        Mono<Object> instalaciones = getDdaaBody("/api/catalogos/instalaciones", exchange);
        Mono<Object> cuencas = getDdaaBody("/api/catalogos/cuencas", exchange);
        Mono<Object> subcuencas = getDdaaBody("/api/catalogos/subcuencas", exchange);
        Mono<Object> fuentes = getDdaaBody("/api/catalogos/fuentes", exchange);

        return Mono.zip(comunas, ruts, instalaciones, cuencas, subcuencas, fuentes)
                .map(tuple -> {
                    Map<String, Object> options = new LinkedHashMap<>();
                    options.put("comunas", tuple.getT1());
                    options.put("ruts", tuple.getT2());
                    options.put("instalaciones", tuple.getT3());
                    options.put("cuencas", tuple.getT4());
                    options.put("subcuencas", tuple.getT5());
                    options.put("fuentes", tuple.getT6());
                    return ResponseEntity.ok((Object) options);
                });
    }

    public Mono<ResponseEntity<Object>> createDdaa(Mono<Map<String, Object>> payload, ServerWebExchange exchange) {
        return payload.flatMap(body -> ddaaClient.post()
                .uri("/api/ddaa")
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .bodyValue(body)
                .exchangeToMono(this::toObjectResponse));
    }

    public Mono<ResponseEntity<Object>> updateDdaa(long id, Mono<Map<String, Object>> payload, ServerWebExchange exchange) {
        return payload.flatMap(body -> ddaaClient.put()
                .uri("/api/ddaa/{id}", id)
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .bodyValue(body)
                .exchangeToMono(this::toObjectResponse));
    }

    public Mono<ResponseEntity<Object>> deleteDdaa(long id, ServerWebExchange exchange) {
        return ddaaClient.delete()
                .uri("/api/ddaa/{id}", id)
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .exchangeToMono(this::toObjectResponse);
    }

    private Mono<Object> getDdaaBody(String path, ServerWebExchange exchange) {
        return ddaaClient.get()
                .uri(path)
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .retrieve()
                .bodyToMono(Object.class);
    }

    private Mono<ResponseEntity<Object>> toObjectResponse(ClientResponse response) {
        HttpStatusCode status = response.statusCode();
        HttpHeaders responseHeaders = new HttpHeaders();
        URI location = response.headers().asHttpHeaders().getLocation();
        if (location != null) {
            responseHeaders.setLocation(location);
        }

        if (status.isError()) {
            return response.bodyToMono(Object.class)
                    .defaultIfEmpty(Map.of("message", "Error al procesar la solicitud DDAA"))
                    .map(body -> ResponseEntity.status(status).headers(responseHeaders).body(body));
        }

        return response.bodyToMono(Object.class)
                .map(body -> ResponseEntity.status(status).headers(responseHeaders).body(body))
                .defaultIfEmpty(ResponseEntity.status(status).headers(responseHeaders).build());
    }

    private void forwardSessionHeaders(ServerWebExchange exchange, HttpHeaders headers) {
        copyHeader(exchange, headers, HttpHeaders.COOKIE);
        copyHeader(exchange, headers, HttpHeaders.AUTHORIZATION);
    }

    private void copyHeader(ServerWebExchange exchange, HttpHeaders headers, String headerName) {
        List<String> values = exchange.getRequest().getHeaders().get(headerName);

        if (values != null && !values.isEmpty()) {
            headers.put(headerName, values);
        }
    }

    private Map<String, Object> anonymousSession() {
        Map<String, Object> session = new LinkedHashMap<>();
        session.put("authenticated", false);
        return session;
    }
}
