package com.ddaa.ddaaservice.service;

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
import com.ddaa.ddaaservice.dto.DocumentDtos.EjercicioDto;
import com.ddaa.ddaaservice.dto.DocumentDtos.ExpedienteDto;
import com.ddaa.ddaaservice.dto.DocumentDtos.PagoNoUsoDto;
import com.ddaa.ddaaservice.event.DdaaEvent;
import com.ddaa.ddaaservice.event.DdaaEventPublisher;
import com.ddaa.ddaaservice.event.DdaaEventType;
import com.ddaa.ddaaservice.model.Comuna;
import com.ddaa.ddaaservice.model.Ddaa;
import com.ddaa.ddaaservice.model.DdaaEjercicio;
import com.ddaa.ddaaservice.model.Fuente;
import com.ddaa.ddaaservice.model.Instalacion;
import com.ddaa.ddaaservice.model.Rut;
import com.ddaa.ddaaservice.repository.ComunaRepository;
import com.ddaa.ddaaservice.repository.DdaaCaudalEcologicoRepository;
import com.ddaa.ddaaservice.repository.DdaaCaudalRepository;
import com.ddaa.ddaaservice.repository.DdaaEjercicioRepository;
import com.ddaa.ddaaservice.repository.DdaaPagoNoUsoRepository;
import com.ddaa.ddaaservice.repository.DdaaQueryRepository;
import com.ddaa.ddaaservice.repository.DdaaRepository;
import com.ddaa.ddaaservice.repository.FuenteRepository;
import com.ddaa.ddaaservice.repository.InstalacionRepository;
import com.ddaa.ddaaservice.repository.RutRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias de DdaaQueryService.
 *
 * Se prueban reglas de aplicación sin levantar Spring, H2, Redis, Eureka ni RabbitMQ.
 * Esto permite cubrir rápido la lógica real del servicio y mantener los tests estables.
 */
@ExtendWith(MockitoExtension.class)
class DdaaQueryServiceTest {

    @Mock
    private DdaaQueryRepository queryRepository;

    @Mock
    private DdaaRepository ddaaRepository;

    @Mock
    private ComunaRepository comunaRepository;

    @Mock
    private RutRepository rutRepository;

    @Mock
    private InstalacionRepository instalacionRepository;

    @Mock
    private FuenteRepository fuenteRepository;

    @Mock
    private DdaaPagoNoUsoRepository pagoNoUsoRepository;

    @Mock
    private DdaaEjercicioRepository ejercicioRepository;

    @Mock
    private DdaaCaudalRepository caudalRepository;

    @Mock
    private DdaaCaudalEcologicoRepository caudalEcologicoRepository;

    @Mock
    private DdaaEventPublisher ddaaEventPublisher;

    @InjectMocks
    private DdaaQueryService service;

    @Test
    void findAllDdaaShouldReturnRepositoryResults() {
        // Arrange: el repositorio devuelve el listado resumido de derechos.
        DdaaSummaryDto summary = sampleSummary(1L);
        when(queryRepository.findAllDdaa()).thenReturn(List.of(summary));

        // Act: el servicio consulta el listado principal.
        List<DdaaSummaryDto> result = service.findAllDdaa();

        // Assert: el servicio no transforma el listado, solo lo delega al repositorio.
        assertThat(result).containsExactly(summary);
        verify(queryRepository).findAllDdaa();
    }

    @Test
    void findDdaaDetailShouldAggregateMainDataAndRelatedDocuments() {
        // Arrange: existe el DDAA y se preparan colecciones relacionadas.
        long ddaaId = 10L;
        DdaaSummaryDto summary = sampleSummary(ddaaId);
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
        PagoNoUsoDto pago = new PagoNoUsoDto(
                500L,
                ddaaId,
                LocalDate.of(2026, 2, 1),
                BigDecimal.TEN,
                1,
                BigDecimal.ONE,
                BigDecimal.valueOf(1000)
        );
        EjercicioDto ejercicio = new EjercicioDto(
                20L,
                ddaaId,
                "Permanente",
                "Continuo",
                List.of(),
                List.of(),
                List.of()
        );

        when(queryRepository.findDdaaById(ddaaId)).thenReturn(Optional.of(summary));
        when(queryRepository.findExpedientesByDdaaId(ddaaId)).thenReturn(List.of(expediente));
        when(queryRepository.findPagosNoUsoByDdaaId(ddaaId)).thenReturn(List.of(pago));
        when(queryRepository.findEjerciciosByDdaaId(ddaaId)).thenReturn(List.of(ejercicio));

        // Act: el servicio arma el detalle compuesto.
        DdaaDetailDto result = service.findDdaaDetail(ddaaId);

        // Assert: el detalle contiene datos principales y documentos relacionados.
        assertThat(result.ddaa()).isEqualTo(summary);
        assertThat(result.expedientes()).containsExactly(expediente);
        assertThat(result.pagosNoUso()).containsExactly(pago);
        assertThat(result.ejercicios()).containsExactly(ejercicio);
    }

    @Test
    void findDdaaDetailShouldThrowWhenDdaaDoesNotExist() {
        // Arrange: el repositorio no encuentra el derecho solicitado.
        long ddaaId = 999L;
        when(queryRepository.findDdaaById(ddaaId)).thenReturn(Optional.empty());

        // Act & Assert: el servicio informa el caso no encontrado.
        assertThatThrownBy(() -> service.findDdaaDetail(ddaaId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se encontro el derecho de agua " + ddaaId);

        // Si no existe el DDAA, no se deben consultar relaciones dependientes.
        verify(queryRepository, never()).findExpedientesByDdaaId(ddaaId);
        verify(queryRepository, never()).findPagosNoUsoByDdaaId(ddaaId);
        verify(queryRepository, never()).findEjerciciosByDdaaId(ddaaId);
    }

    @Test
    void catalogMethodsShouldDelegateToQueryRepository() {
        // Arrange: catálogos consultados por JDBC desde el repositorio especializado.
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

        when(queryRepository.findCuencas()).thenReturn(List.of(cuenca));
        when(queryRepository.findSubcuencas()).thenReturn(List.of(subcuenca));
        when(queryRepository.findFuentes()).thenReturn(List.of(fuente));

        // Act & Assert: cada método de catálogo retorna lo que entrega el repositorio.
        assertThat(service.findCuencas()).containsExactly(cuenca);
        assertThat(service.findSubcuencas()).containsExactly(subcuenca);
        assertThat(service.findFuentes()).containsExactly(fuente);
    }

    @Test
    void entityCatalogMethodsShouldMapJpaEntitiesToDtos() {
        // Arrange: catálogos simples obtenidos desde repositories JPA.
        Comuna comuna = comuna("001", "Santiago");
        Rut rut = rut(11111111L, "Titular Test");
        Instalacion instalacion = instalacion(5, "Instalacion Test");

        when(comunaRepository.findAll(any(Sort.class))).thenReturn(List.of(comuna));
        when(rutRepository.findAll(any(Sort.class))).thenReturn(List.of(rut));
        when(instalacionRepository.findAll(any(Sort.class))).thenReturn(List.of(instalacion));

        // Act: el servicio transforma entidades JPA a DTOs usados por el frontend.
        List<ComunaDto> comunas = service.findComunas();
        List<RutDto> ruts = service.findRuts();
        List<InstalacionDto> instalaciones = service.findInstalaciones();

        // Assert: los DTOs conservan identificador y nombre.
        assertThat(comunas).containsExactly(new ComunaDto("001", "Santiago"));
        assertThat(ruts).containsExactly(new RutDto(11111111L, "Titular Test"));
        assertThat(instalaciones).containsExactly(new InstalacionDto(5L, "Instalacion Test"));
    }

    @Test
    void createDdaaShouldSaveEntityAndPublishCreatedEvent() {
        // Arrange: se preparan referencias JPA simuladas y un save que asigna ID.
        DdaaCreateDto dto = sampleCreateDto();

        when(comunaRepository.getReferenceById("001")).thenReturn(comuna("001", "Santiago"));
        when(rutRepository.getReferenceById(11111111L)).thenReturn(rut(11111111L, "Titular Test"));
        when(instalacionRepository.getReferenceById(5)).thenReturn(instalacion(5, "Instalacion Test"));
        when(fuenteRepository.getReferenceById(3)).thenReturn(fuente(3, "Fuente Test"));

        when(ddaaRepository.save(any(Ddaa.class))).thenAnswer(invocation -> {
            Ddaa saved = invocation.getArgument(0);
            saved.setId(100);
            return saved;
        });

        // Act: el servicio crea el derecho.
        long id = service.createDdaa(dto);

        // Assert: retorna el ID generado y publica evento CREATED.
        assertThat(id).isEqualTo(100L);

        ArgumentCaptor<Ddaa> ddaaCaptor = ArgumentCaptor.forClass(Ddaa.class);
        verify(ddaaRepository).save(ddaaCaptor.capture());

        Ddaa savedEntity = ddaaCaptor.getValue();
        assertThat(savedEntity.getComuna().getId()).isEqualTo("001");
        assertThat(savedEntity.getTitular().getRut()).isEqualTo(11111111L);
        assertThat(savedEntity.getInstalacion().getId()).isEqualTo(5);
        assertThat(savedEntity.getFuente().getId()).isEqualTo(3);
        assertThat(savedEntity.getNombreFuenteDerecho()).isEqualTo("Fuente Derecho Test");

        ArgumentCaptor<DdaaEvent> eventCaptor = ArgumentCaptor.forClass(DdaaEvent.class);
        verify(ddaaEventPublisher).publishAfterCommit(eventCaptor.capture());

        DdaaEvent event = eventCaptor.getValue();
        assertThat(event.eventType()).isEqualTo(DdaaEventType.CREATED);
        assertThat(event.ddaaId()).isEqualTo(100L);
        assertThat(event.comunaId()).isEqualTo("001");
        assertThat(event.rutTitular()).isEqualTo(11111111L);
    }

    @Test
    void updateDdaaShouldReturnOneAndPublishUpdatedEventWhenEntityExists() {
        // Arrange: existe un DDAA editable.
        Ddaa existing = existingDdaa(10);
        DdaaUpdateDto dto = sampleUpdateDto();

        when(ddaaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(comunaRepository.getReferenceById("002")).thenReturn(comuna("002", "Puerto Montt"));
        when(rutRepository.getReferenceById(22222222L)).thenReturn(rut(22222222L, "Titular Actualizado"));
        when(fuenteRepository.getReferenceById(4)).thenReturn(fuente(4, "Fuente Actualizada"));

        // Act: el servicio actualiza el derecho.
        int updated = service.updateDdaa(10L, dto);

        // Assert: actualiza datos, guarda entidad y publica evento UPDATED.
        assertThat(updated).isEqualTo(1);
        assertThat(existing.getComuna().getId()).isEqualTo("002");
        assertThat(existing.getTitular().getRut()).isEqualTo(22222222L);
        assertThat(existing.getInstalacion()).isNull();
        assertThat(existing.getFuente().getId()).isEqualTo(4);
        assertThat(existing.getEstadoDerecho()).isEqualTo("Inactivo");

        verify(ddaaRepository).save(existing);

        ArgumentCaptor<DdaaEvent> eventCaptor = ArgumentCaptor.forClass(DdaaEvent.class);
        verify(ddaaEventPublisher).publishAfterCommit(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(DdaaEventType.UPDATED);
    }

    @Test
    void updateDdaaShouldReturnZeroWhenEntityDoesNotExist() {
        // Arrange: el DDAA solicitado no existe.
        when(ddaaRepository.findById(404)).thenReturn(Optional.empty());

        // Act: se intenta actualizar un derecho inexistente.
        int updated = service.updateDdaa(404L, sampleUpdateDto());

        // Assert: no se guarda ni se publica evento.
        assertThat(updated).isZero();
        verify(ddaaRepository, never()).save(any(Ddaa.class));
        verify(ddaaEventPublisher, never()).publishAfterCommit(any(DdaaEvent.class));
    }

    @Test
    void deleteDdaaShouldDeleteRelationsAndPublishDeletedEventWhenEntityExists() {
        // Arrange: existe un DDAA con un ejercicio dependiente.
        Ddaa existing = existingDdaa(10);
        DdaaEjercicio ejercicio = new DdaaEjercicio();
        ejercicio.setId(20);

        when(ddaaRepository.findById(10)).thenReturn(Optional.of(existing));
        when(ejercicioRepository.findByDdaa_Id(10)).thenReturn(List.of(ejercicio));

        // Act: se elimina el DDAA.
        int deleted = service.deleteDdaa(10L);

        // Assert: se limpian relaciones dependientes y se publica evento DELETED.
        assertThat(deleted).isEqualTo(1);

        verify(ddaaRepository).save(existing);
        verify(pagoNoUsoRepository).deleteByDdaa_Id(10);
        verify(caudalRepository).deleteByEjercicio_Id(20);
        verify(caudalEcologicoRepository).deleteByEjercicio_Id(20);
        verify(ejercicioRepository).deleteByDdaa_Id(10);
        verify(ddaaRepository).delete(existing);

        ArgumentCaptor<DdaaEvent> eventCaptor = ArgumentCaptor.forClass(DdaaEvent.class);
        verify(ddaaEventPublisher).publishAfterCommit(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo(DdaaEventType.DELETED);
    }

    @Test
    void deleteDdaaShouldReturnZeroWhenEntityDoesNotExist() {
        // Arrange: el DDAA no existe.
        when(ddaaRepository.findById(404)).thenReturn(Optional.empty());

        // Act: se intenta eliminar un derecho inexistente.
        int deleted = service.deleteDdaa(404L);

        // Assert: no se eliminan dependencias ni se publica evento.
        assertThat(deleted).isZero();
        verify(pagoNoUsoRepository, never()).deleteByDdaa_Id(404);
        verify(ddaaRepository, never()).delete(any(Ddaa.class));
        verify(ddaaEventPublisher, never()).publishAfterCommit(any(DdaaEvent.class));
    }

    /**
     * DTO de resumen reutilizable para pruebas de consulta.
     */
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

    /**
     * DTO mínimo para probar creación de DDAA.
     */
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

    /**
     * DTO mínimo para probar actualización de DDAA.
     */
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

    /**
     * Entidad DDAA base para pruebas de update/delete.
     */
    private Ddaa existingDdaa(Integer id) {
        Ddaa ddaa = new Ddaa();
        ddaa.setId(id);
        ddaa.setComuna(comuna("001", "Santiago"));
        ddaa.setTitular(rut(11111111L, "Titular Test"));
        ddaa.setInstalacion(instalacion(5, "Instalacion Test"));
        ddaa.setFuente(fuente(3, "Fuente Test"));
        ddaa.setNombreFuenteDerecho("Fuente Derecho Test");
        ddaa.setNaturalezaDerecho("Consuntivo");
        ddaa.setTipoDerecho("Permanente");
        ddaa.setEstadoDerecho("Activo");
        return ddaa;
    }

    private Comuna comuna(String id, String nombre) {
        Comuna comuna = new Comuna();
        comuna.setId(id);
        comuna.setNombre(nombre);
        return comuna;
    }

    private Rut rut(Long rutValue, String nombre) {
        Rut rut = new Rut();
        rut.setRut(rutValue);
        rut.setNombre(nombre);
        return rut;
    }

    private Instalacion instalacion(Integer id, String nombre) {
        Instalacion instalacion = new Instalacion();
        instalacion.setId(id);
        instalacion.setNombre(nombre);
        return instalacion;
    }

    private Fuente fuente(Integer id, String nombre) {
        Fuente fuente = new Fuente();
        fuente.setId(id);
        fuente.setNombre(nombre);
        fuente.setTipo("Superficial");
        return fuente;
    }
}