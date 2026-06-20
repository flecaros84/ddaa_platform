package com.ddaa.ddaaservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;

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
                        // Servidor directo del microservicio.
                        // Se deja primero para facilitar pruebas desde Swagger UI sin depender del Gateway.
                        new Server().url("http://localhost:8082").description("DDAA Service local directo"),

                        // Servidor via API Gateway.
                        // Util para validar el flujo completo cuando Eureka y Gateway esten levantados.
                        new Server().url("http://localhost:8080").description("API Gateway local")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT enviado en el header Authorization con formato: Bearer <token>.")))
// Aplica JWT Bearer como seguridad global de la API.
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}