package com.ddaa.ddaaservice.config;

import java.time.Duration;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Configuración centralizada de caché Redis para ddaa-service.
 *
 * Esta clase define cómo se guardan los valores en Redis y cuánto tiempo viven.
 * Usamos JSON para evitar depender de serialización Java nativa en los DTO.
 */
@Configuration
public class RedisCacheConfig {

    /**
     * Personaliza el CacheManager creado automáticamente por Spring Boot.
     *
     * Lee el TTL desde DDAA_CACHE_TTL o spring.cache.redis.time-to-live.
     * Si no existe configuración externa, usa 10 minutos por defecto.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(Environment environment) {
        return cacheBuilder -> {
            Duration ttl = resolveCacheTtl(environment);

            RedisCacheConfiguration configuration = RedisCacheConfiguration
                    .defaultCacheConfig()
                    // Define el tiempo de vida de cada entrada en caché.
                    .entryTtl(ttl)
                    // Evita guardar valores nulos en Redis.
                    .disableCachingNullValues()
                    // Guarda los valores como JSON para facilitar compatibilidad con records/DTO.
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(
                                    // Serializador JSON compatible con Spring Data Redis 4.
                                    GenericJacksonJsonRedisSerializer.builder()
                                            // Agrega información de tipo al JSON para que Redis pueda reconstruir DTOs concretos.
                                            // Sin esto, las listas vuelven como LinkedHashMap y fallan al serializar la respuesta.
                                            .typePropertyName("@class")
                                            .enableUnsafeDefaultTyping()
                                            .build()
                            )
                    )
                    // Prefijo común para distinguir claves de este microservicio.
                    .prefixCacheNameWith("ddaa-service::");

            cacheBuilder.cacheDefaults(configuration);
        };
    }

    /**
     * Resuelve el TTL desde variables de entorno o application.yml.
     *
     * Acepta formatos como 10m, 30s, 1h.
     */
    private Duration resolveCacheTtl(Environment environment) {
        String ttl = environment.getProperty("DDAA_CACHE_TTL");

        if (ttl == null || ttl.isBlank()) {
            ttl = environment.getProperty("spring.cache.redis.time-to-live", "10m");
        }

        return DurationStyle.detectAndParse(ttl);
    }
}