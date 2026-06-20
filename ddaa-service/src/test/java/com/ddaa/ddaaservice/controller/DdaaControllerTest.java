package com.ddaa.ddaaservice.controller;

import com.ddaa.ddaaservice.dto.CatalogDtos.ComunaDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.CuencaDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.FuenteDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.InstalacionDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.RutDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.SubcuencaDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaCreateDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaDetailDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaSummaryDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaUpdateDto;
import com.ddaa.ddaaservice.dto.DocumentDtos.ExpedienteDto;
import com.ddaa.ddaaservice.service.DdaaQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de DdaaController.
 *
 * No se usa @WebMvcTest para evitar levantar seguridad, filtros y contexto Spring.
 * El objetivo es cubrir el comportamiento del controller: delegación al service y códigos HTTP.
 */
@ExtendWith(MockitoExtension.class)
class DdaaControllerTest {

    @Mock
    private DdaaQueryService queryService;

    @InjectMocks
    private DdaaController controller;

    @Test
    void listDdaaShouldReturnServiceResults() {
        // Arrange: el service entrega un listado resumido.
        DdaaSummaryDto summary = sampleSummary(1L);
        when(queryService.findAllDdaa()).thenReturn(List.of(summary));

        // Act & Assert: el controller retorna lo recibido desde el service.
        assertThat(controller.listDdaa()).containsExactly(summary);
    }

    @Test
    void getDdaaShouldReturnDetailFromService() {
        // Arrange: el service entrega detalle compuesto.
        DdaaDetailDto detail = new DdaaDetailDto(sampleSummary(1L), List.of(), List.of(), List.of());
        when(queryService.findDdaaDetail(1L)).thenReturn(detail);

        // Act & Assert: el controller retorna el detalle.
        assertThat(controller.getDdaa(1L)).isEqualTo(detail);
    }

    @Test
    void listDdaaExpedientesShouldReturnServiceResults() {
        // Arrange: se simula un expediente asociado al DDAA.
        ExpedienteDto expediente = new ExpedienteDto(
                1L,
                "EXP-1",
                "Solicitud",
                "Activo",
                "RES-1",
                LocalDate.of(2026, 1, 1),
                "https://example.test/res",
                100,
                LocalDate.of(2026, 1, 2),
                "https://example.test/catastro"
        );
        when(queryService.findExpedientesByDdaaId(1L)).thenReturn(List.of(expediente));

        // Act & Assert: el endpoint de expedientes delega correctamente al service.
        assertThat(controller.listDdaaExpedientes(1L)).containsExactly(expediente);
    }

    @Test
    void catalogEndpointsShouldReturnServiceResults() {
        // Arrange: se preparan catálogos mínimos usados por formularios.
        CuencaDto cuenca = new CuencaDto(1L, "Cuenca Test", List.of());
        SubcuencaDto subcuenca = new SubcuencaDto(
                2L,
                1L,
                "Cuenca Test",
                "Subcuenca Test",
                "SHAC",
                "Reserva",
                false,
                false,
                false,
                "Activo",
                true
        );
        FuenteDto fuente = new FuenteDto(3L, 2L, "Subcuenca Test", "Fuente Test", "Superficial");
        ComunaDto comuna = new ComunaDto("001", "Santiago");
        RutDto rut = new RutDto(11111111L, "Titular Test");
        InstalacionDto instalacion = new InstalacionDto(5L, "Instalacion Test");

        when(queryService.findCuencas()).thenReturn(List.of(cuenca));
        when(queryService.findSubcuencas()).thenReturn(List.of(subcuenca));
        when(queryService.findFuentes()).thenReturn(List.of(fuente));
        when(queryService.findComunas()).thenReturn(List.of(comuna));
        when(queryService.findRuts()).thenReturn(List.of(rut));
        when(queryService.findInstalaciones()).thenReturn(List.of(instalacion));

        // Act & Assert: cada endpoint de catálogo retorna lo entregado por el service.
        assertThat(controller.listCuencas()).containsExactly(cuenca);
        assertThat(controller.listSubcuencas()).containsExactly(subcuenca);
        assertThat(controller.listFuentes()).containsExactly(fuente);
        assertThat(controller.listComunas()).containsExactly(comuna);
        assertThat(controller.listRuts()).containsExactly(rut);
        assertThat(controller.listInstalaciones()).containsExactly(instalacion);
    }

    @Test
    void createDdaaShouldReturnCreatedStatusAndGeneratedId() {
        // Arrange: el service simula creación exitosa y entrega el ID generado.
        DdaaCreateDto dto = sampleCreateDto();
        when(queryService.createDdaa(dto)).thenReturn(99L);

        // Act: el controller retorna ResponseEntity sin genérico en la implementación actual.
        ResponseEntity<?> response = controller.createDdaa(dto);

        // Assert: el endpoint responde 201 Created.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // Assert: el body se valida como Map porque el controller construye Map.of("id", id).
        assertThat(response.getBody())
                .isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, Long> body = (Map<String, Long>) response.getBody();

        assertThat(body).containsEntry("id", 99L);
    }

    @Test
    void updateDdaaShouldReturnNoContentWhenUpdated() {
        // Arrange: el service informa que se actualizó un registro.
        DdaaUpdateDto dto = sampleUpdateDto();
        when(queryService.updateDdaa(1L, dto)).thenReturn(1);

        // Act: el controller retorna ResponseEntity raw, por eso se usa wildcard.
        ResponseEntity<?> response = controller.updateDdaa(1L, dto);

        // Assert: un update exitoso retorna 204 No Content.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void updateDdaaShouldReturnNotFoundWhenNoRowsWereUpdated() {
        // Arrange: el service informa que no encontró el registro.
        DdaaUpdateDto dto = sampleUpdateDto();
        when(queryService.updateDdaa(404L, dto)).thenReturn(0);

        // Act: se intenta actualizar un DDAA inexistente.
        ResponseEntity<?> response = controller.updateDdaa(404L, dto);

        // Assert: si no hay actualización, el controller retorna 404.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
    @Test
    void deleteDdaaShouldReturnNoContentWhenDeleted() {
        // Arrange: el service informa eliminación exitosa.
        when(queryService.deleteDdaa(1L)).thenReturn(1);

        // Act: el controller retorna ResponseEntity raw, por eso se usa wildcard.
        ResponseEntity<?> response = controller.deleteDdaa(1L);

        // Assert: una eliminación exitosa retorna 204 No Content.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void deleteDdaaShouldReturnNotFoundWhenNoRowsWereDeleted() {
        // Arrange: el service informa que no encontró el registro.
        when(queryService.deleteDdaa(404L)).thenReturn(0);

        // Act: se intenta eliminar un DDAA inexistente.
        ResponseEntity<?> response = controller.deleteDdaa(404L);

        // Assert: si no se eliminó nada, el controller retorna 404.
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private DdaaSummaryDto sampleSummary(Long id) {
        return new DdaaSummaryDto(
                id,
                "001",
                "Santiago",
                11111111L,
                "Titular Test",
                5L,
                "Instalacion Test",
                3L,
                "Fuente Test",
                "Superficial",
                "Fuente Derecho Test",
                "Consuntivo",
                "Permanente",
                "Activo",
                1L,
                "Cuenca Test",
                2L,
                "Subcuenca Test"
        );
    }

    private DdaaCreateDto sampleCreateDto() {
        return new DdaaCreateDto(
                "001",
                11111111L,
                5L,
                3L,
                "Fuente Derecho Test",
                "Consuntivo",
                "Permanente",
                "Activo"
        );
    }

    private DdaaUpdateDto sampleUpdateDto() {
        return new DdaaUpdateDto(
                "002",
                22222222L,
                null,
                4L,
                "Fuente Derecho Actualizada",
                "No consuntivo",
                "Eventual",
                "Inactivo"
        );
    }
}