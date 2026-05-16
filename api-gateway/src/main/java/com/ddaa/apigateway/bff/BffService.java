package com.ddaa.apigateway.bff;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BffService {

    private static final ParameterizedTypeReference<Map<String, Object>> SESSION_RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient authClient;

    public BffService(WebClient.Builder webClientBuilder) {
        this.authClient = webClientBuilder
                .baseUrl("http://auth-service")
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
