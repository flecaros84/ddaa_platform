package com.ddaa.ddaaservice;

import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaCreateDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaUpdateDto;
import com.ddaa.ddaaservice.repository.DdaaQueryRepository;
import com.ddaa.ddaaservice.service.DdaaQueryService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.sql.init.mode=never",
        "app.security.jwt-secret=ddaa-test-secret-key-for-integration-tests-only-123456789"
})
@Transactional
public class DdaaCrudIntegrationTest {

    @Autowired
    private DdaaQueryService service;

    @Autowired
    private DdaaQueryRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void createUpdateDeleteLifecycle() {
        // ensure referential rows exist
        // H2 está creando las tablas con los nombres definidos por las entidades JPA.
// En este servicio, los nombres físicos están en minúscula, por ejemplo: comuna, ruts y fuente.
// Por eso las consultas del test deben usar los mismos nombres para evitar errores de case sensitivity.
        jdbcTemplate.update(
                "MERGE INTO comuna (id_comuna, nombre) KEY(id_comuna) VALUES (?, ?)",
                "001",
                "Comuna Test"
        );

// La entidad Rut usa un identificador numérico Long.
// Por eso el dato semilla del test debe omitir guion y dígito verificador,
// manteniendo consistencia con los DTO que referencian el titular por rutTitular.
        jdbcTemplate.update(
                "MERGE INTO ruts (rut, nombre) KEY(rut) VALUES (?, ?)",
                11111111L,
                "Titular Test"
        );

        jdbcTemplate.update(
                "MERGE INTO fuente (id_fuente, nombre) KEY(id_fuente) VALUES (?, ?)",
                1L,
                "Fuente Test"
        );

        // El DDAA creado debe apuntar al mismo RUT semilla insertado arriba.
        DdaaCreateDto create = new DdaaCreateDto(
                "001",
                11111111L,
                null,
                1L,
                "Fuente X",
                "Privado",
                "Titulo",
                "Activo"
        );

        long id = service.createDdaa(create);
        assertThat(id).isGreaterThan(0);

        var found = repository.findDdaaById(id);
        assertThat(found).isPresent();

        // ensure comuna for update exists
        // Segunda comuna requerida para probar el cambio de comuna durante el update.
        jdbcTemplate.update(
                "MERGE INTO comuna (id_comuna, nombre) KEY(id_comuna) VALUES (?, ?)",
                "002",
                "Comuna Update Test"
        );
        // El update mantiene el mismo titular para probar solo el cambio de comuna y datos del derecho.
        DdaaUpdateDto update = new DdaaUpdateDto(
                "002",
                11111111L,
                null,
                1L,
                "Fuente Y",
                "Publico",
                "Titulo",
                "Inactivo"
        );

        int updated = service.updateDdaa(id, update);
        assertThat(updated).isGreaterThanOrEqualTo(0);

        int deleted = service.deleteDdaa(id);
        assertThat(deleted).isGreaterThanOrEqualTo(0);
    }
}
