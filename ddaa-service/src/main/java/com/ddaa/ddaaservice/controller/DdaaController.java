package com.ddaa.ddaaservice.controller;

import com.ddaa.ddaaservice.dto.CatalogDtos.CuencaDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.FuenteDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.SubcuencaDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaCreateDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaDetailDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaSummaryDto;
import com.ddaa.ddaaservice.dto.DdaaDtos.DdaaUpdateDto;
import com.ddaa.ddaaservice.dto.DocumentDtos.ExpedienteDto;
import com.ddaa.ddaaservice.service.DdaaQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@Tag(name = "DDAA", description = "Operaciones de consulta y administracion de derechos de aprovechamiento de aguas.")
@SecurityRequirement(name = "sessionCookie")
public class DdaaController {

    private final DdaaQueryService queryService;

    public DdaaController(DdaaQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/ddaa")
    @Operation(summary = "Listar derechos DDAA", description = "Entrega un resumen de todos los derechos registrados.")
    @ApiResponse(responseCode = "200", description = "Listado de derechos DDAA")
    public List<DdaaSummaryDto> listDdaa() {
        return queryService.findAllDdaa();
    }

    @GetMapping("/ddaa/{id}")
    @Operation(summary = "Obtener detalle de un DDAA", description = "Entrega el derecho, expedientes, pagos de no uso y ejercicios asociados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Detalle encontrado"),
            @ApiResponse(responseCode = "404", description = "Derecho DDAA no encontrado", content = @Content(schema = @Schema(implementation = java.util.Map.class)))
    })
    public DdaaDetailDto getDdaa(@PathVariable long id) {
        return queryService.findDdaaDetail(id);
    }

    @GetMapping("/ddaa/{id}/expedientes")
    @Operation(summary = "Listar expedientes de un DDAA", description = "Entrega los expedientes vinculados a un derecho especifico.")
    @ApiResponse(responseCode = "200", description = "Listado de expedientes")
    public List<ExpedienteDto> listDdaaExpedientes(@PathVariable long id) {
        return queryService.findExpedientesByDdaaId(id);
    }

    @GetMapping("/catalogos/cuencas")
    @Operation(summary = "Listar cuencas", description = "Entrega cuencas con sus subcuencas asociadas.")
    @ApiResponse(responseCode = "200", description = "Listado de cuencas")
    public List<CuencaDto> listCuencas() {
        return queryService.findCuencas();
    }

    @GetMapping("/catalogos/subcuencas")
    @Operation(summary = "Listar subcuencas", description = "Entrega subcuencas disponibles para formularios y filtros.")
    @ApiResponse(responseCode = "200", description = "Listado de subcuencas")
    public List<SubcuencaDto> listSubcuencas() {
        return queryService.findSubcuencas();
    }

    @GetMapping("/catalogos/fuentes")
    @Operation(summary = "Listar fuentes", description = "Entrega fuentes disponibles para formularios y filtros.")
    @ApiResponse(responseCode = "200", description = "Listado de fuentes")
    public List<FuenteDto> listFuentes() {
        return queryService.findFuentes();
    }

    @PostMapping("/ddaa")
    @Operation(summary = "Crear un DDAA", description = "Crea un nuevo derecho de aprovechamiento de aguas con los campos basicos del MVP.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "DDAA creado"),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida")
    })
    public ResponseEntity<?> createDdaa(@RequestBody DdaaCreateDto dto) {
        long id = queryService.createDdaa(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(java.util.Map.of("id", id));
    }

    @PutMapping("/ddaa/{id}")
    @Operation(summary = "Actualizar un DDAA", description = "Actualiza los campos basicos de un derecho existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "DDAA actualizado"),
            @ApiResponse(responseCode = "404", description = "DDAA no encontrado")
    })
    public ResponseEntity<?> updateDdaa(@PathVariable long id, @RequestBody DdaaUpdateDto dto) {
        int updated = queryService.updateDdaa(id, dto);
        if (updated > 0) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/ddaa/{id}")
    @Operation(summary = "Eliminar un DDAA", description = "Elimina un derecho por identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "DDAA eliminado"),
            @ApiResponse(responseCode = "404", description = "DDAA no encontrado")
    })
    public ResponseEntity<?> deleteDdaa(@PathVariable long id) {
        int deleted = queryService.deleteDdaa(id);
        if (deleted > 0) return ResponseEntity.noContent().build();
        return ResponseEntity.notFound().build();
    }
}
