package com.ddaa.authservice.config;

import com.ddaa.authservice.service.CustomOidcUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private final CustomOidcUserService customOidcUserService;
    private final String frontendSuccessUrl;

    public SecurityConfig(CustomOidcUserService customOidcUserService,
                          @Value("${app.frontend.success-url:http://localhost:5173/}") String frontendSuccessUrl) {
        this.customOidcUserService = customOidcUserService;
        this.frontendSuccessUrl = frontendSuccessUrl;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/test").permitAll()
                        .requestMatchers("/auth/login").permitAll()
                        .requestMatchers("/auth/error").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .loginPage("/auth/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .defaultSuccessUrl(frontendSuccessUrl, true)
                        .failureUrl("/auth/error")
                )
                .logout(logout -> logout
                        .logoutSuccessUrl(frontendSuccessUrl)
                        .permitAll()
                );

        return http.build();
    }
}
