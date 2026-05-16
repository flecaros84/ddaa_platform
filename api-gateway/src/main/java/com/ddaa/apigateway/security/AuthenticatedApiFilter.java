package com.ddaa.apigateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class AuthenticatedApiFilter implements GlobalFilter, Ordered {

    private static final ParameterizedTypeReference<Map<String, Object>> SESSION_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final WebClient authClient;

    public AuthenticatedApiFilter(WebClient.Builder webClientBuilder) {
        this.authClient = webClientBuilder
                .baseUrl("http://auth-service")
                .build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!requiresAuthentication(exchange)) {
            return chain.filter(exchange);
        }

        return authClient.get()
                .uri("/auth/me")
                .headers(headers -> forwardSessionHeaders(exchange, headers))
                .retrieve()
                .bodyToMono(SESSION_TYPE)
                .defaultIfEmpty(Map.of("authenticated", false))
                .flatMap(session -> Boolean.TRUE.equals(session.get("authenticated"))
                        ? chain.filter(exchange)
                        : unauthorized(exchange))
                .onErrorResume(ex -> unauthorized(exchange));
    }

    private boolean requiresAuthentication(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().value();
        return path.startsWith("/api/") || path.startsWith("/bff/ddaa");
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

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
