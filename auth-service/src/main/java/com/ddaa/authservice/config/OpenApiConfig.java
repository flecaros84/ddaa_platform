package com.ddaa.authservice.config;

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
    public OpenAPI authServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auth Service API")
                        .version("0.0.1")
                        .description("API acotada de autenticacion. El inicio de sesion OAuth se prueba desde navegador usando /oauth2/authorization/google."))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("API Gateway local"),
                        new Server().url("http://localhost:8081").description("Auth Service local directo")
                ))
                .components(new Components()
                        .addSecuritySchemes("sessionCookie", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID")
                                .description("Sesion creada tras autenticacion OAuth con Google.")));
    }
}
