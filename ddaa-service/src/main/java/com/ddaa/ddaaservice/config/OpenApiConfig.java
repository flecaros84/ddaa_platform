package com.ddaa.ddaaservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ddaaServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("DDAA Service API")
                        .version("0.0.1")
                        .description("API de negocio para consulta y administracion MVP de derechos de aprovechamiento de aguas."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway local"),
                        new Server().url("http://localhost:8082").description("DDAA Service local directo")
                ))
                .components(new Components()
                        .addSecuritySchemes("sessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Sesion emitida por auth-service al autenticarse por Google. Requerida al consumir /api/** via gateway.")));
    }
}
