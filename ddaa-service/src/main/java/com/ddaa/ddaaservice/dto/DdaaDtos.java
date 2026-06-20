package com.ddaa.ddaaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public final class DdaaDtos {

    private DdaaDtos() {
    }

    @Schema(description = "Resumen de un derecho de aprovechamiento de aguas registrado en la plataforma.")
    public record DdaaSummaryDto(

            @Schema(description = "Identificador interno del DDAA.", example = "1")
            Long id,

            @Schema(description = "Código de la comuna asociada al derecho.", example = "10201")
            String comunaId,

            @Schema(description = "Nombre de la comuna asociada al derecho.", example = "Puerto Montt")
            String comunaNombre,

            @Schema(description = "RUT numérico del titular del derecho, sin dígito verificador.", example = "76123456")
            Long rutTitular,

            @Schema(description = "Nombre o razón social del titular del derecho.", example = "Empresa Titular SpA")
            String titularNombre,

            @Schema(description = "Identificador de la instalación asociada.", example = "10")
            Long instalacionId,

            @Schema(description = "Nombre de la instalación asociada.", example = "Centro Lago Azul")
            String instalacionNombre,

            @Schema(description = "Identificador de la fuente de agua.", example = "5")
            Long fuenteId,

            @Schema(description = "Nombre de la fuente de agua.", example = "Río Maullín")
            String fuenteNombre,

            @Schema(description = "Tipo de fuente de agua.", example = "SUPERFICIAL")
            String fuenteTipo,

            @Schema(description = "Nombre de la fuente informado en el derecho.", example = "Río Maullín sector norte")
            String nombreFuenteDerecho,

            @Schema(description = "Naturaleza del derecho.", example = "CONSUNTIVO")
            String naturalezaDerecho,

            @Schema(description = "Tipo de ejercicio del derecho.", example = "PERMANENTE_Y_CONTINUO")
            String tipoDerecho,

            @Schema(description = "Estado administrativo del derecho.", example = "VIGENTE")
            String estadoDerecho,

            @Schema(description = "Identificador de la cuenca asociada.", example = "1")
            Long cuencaId,

            @Schema(description = "Nombre de la cuenca asociada.", example = "Cuenca del río Maullín")
            String cuencaNombre,

            @Schema(description = "Identificador de la subcuenca asociada.", example = "2")
            Long subcuencaId,

            @Schema(description = "Nombre de la subcuenca asociada.", example = "Subcuenca Maullín Alto")
            String subcuencaNombre
    ) {
    }

    @Schema(description = "Detalle completo de un derecho DDAA, incluyendo información documental y operacional asociada.")
    public record DdaaDetailDto(

            @Schema(description = "Datos principales del derecho.")
            DdaaSummaryDto ddaa,

            @Schema(description = "Expedientes administrativos vinculados al derecho.")
            List<DocumentDtos.ExpedienteDto> expedientes,

            @Schema(description = "Pagos de patente por no uso asociados al derecho.")
            List<DocumentDtos.PagoNoUsoDto> pagosNoUso,

            @Schema(description = "Ejercicios o usos registrados para el derecho.")
            List<DocumentDtos.EjercicioDto> ejercicios
    ) {
    }

    @Schema(description = "Solicitud para crear un nuevo derecho de aprovechamiento de aguas.")
    public record DdaaCreateDto(

            @Schema(description = "Código de la comuna asociada al derecho.", example = "10201")
            String comunaId,

            @Schema(description = "RUT numérico del titular, sin dígito verificador.", example = "76123456")
            Long rutTitular,

            @Schema(description = "Identificador de la instalación asociada.", example = "10")
            Long instalacionId,

            @Schema(description = "Identificador de la fuente de agua.", example = "5")
            Long fuenteId,

            @Schema(description = "Nombre de la fuente informado en el derecho.", example = "Río Maullín sector norte")
            String nombreFuenteDerecho,

            @Schema(description = "Naturaleza del derecho.", example = "CONSUNTIVO")
            String naturalezaDerecho,

            @Schema(description = "Tipo de ejercicio del derecho.", example = "PERMANENTE_Y_CONTINUO")
            String tipoDerecho,

            @Schema(description = "Estado administrativo inicial del derecho.", example = "VIGENTE")
            String estadoDerecho
    ) {
    }

    @Schema(description = "Solicitud para actualizar los datos principales de un derecho de aprovechamiento de aguas.")
    public record DdaaUpdateDto(

            @Schema(description = "Código de la comuna asociada al derecho.", example = "10201")
            String comunaId,

            @Schema(description = "RUT numérico del titular, sin dígito verificador.", example = "76123456")
            Long rutTitular,

            @Schema(description = "Identificador de la instalación asociada.", example = "10")
            Long instalacionId,

            @Schema(description = "Identificador de la fuente de agua.", example = "5")
            Long fuenteId,

            @Schema(description = "Nombre de la fuente informado en el derecho.", example = "Río Maullín sector norte")
            String nombreFuenteDerecho,

            @Schema(description = "Naturaleza del derecho.", example = "CONSUNTIVO")
            String naturalezaDerecho,

            @Schema(description = "Tipo de ejercicio del derecho.", example = "PERMANENTE_Y_CONTINUO")
            String tipoDerecho,

            @Schema(description = "Estado administrativo del derecho.", example = "VIGENTE")
            String estadoDerecho
    ) {
    }
}