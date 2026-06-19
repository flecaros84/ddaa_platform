package com.ddaa.ddaaservice.service;

import com.ddaa.ddaaservice.dto.CatalogDtos.CuencaDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.FuenteDto;
import com.ddaa.ddaaservice.dto.CatalogDtos.ComunaDto;
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
import com.ddaa.ddaaservice.model.Ddaa;
import com.ddaa.ddaaservice.model.DdaaEjercicio;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.ArrayList;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

@Service
public class DdaaQueryService {

    private final DdaaQueryRepository queryRepository;
    private final DdaaRepository ddaaRepository;
    private final ComunaRepository comunaRepository;
    private final RutRepository rutRepository;
    private final InstalacionRepository instalacionRepository;
    private final FuenteRepository fuenteRepository;
    private final DdaaPagoNoUsoRepository pagoNoUsoRepository;
    private final DdaaEjercicioRepository ejercicioRepository;
    private final DdaaCaudalRepository caudalRepository;
    private final DdaaCaudalEcologicoRepository caudalEcologicoRepository;

    public DdaaQueryService(DdaaQueryRepository queryRepository, DdaaRepository ddaaRepository,
                            ComunaRepository comunaRepository, RutRepository rutRepository,
                            InstalacionRepository instalacionRepository, FuenteRepository fuenteRepository,
                            DdaaPagoNoUsoRepository pagoNoUsoRepository,
                            DdaaEjercicioRepository ejercicioRepository,
                            DdaaCaudalRepository caudalRepository,
                            DdaaCaudalEcologicoRepository caudalEcologicoRepository) {
        this.queryRepository = queryRepository;
        this.ddaaRepository = ddaaRepository;
        this.comunaRepository = comunaRepository;
        this.rutRepository = rutRepository;
        this.instalacionRepository = instalacionRepository;
        this.fuenteRepository = fuenteRepository;
        this.pagoNoUsoRepository = pagoNoUsoRepository;
        this.ejercicioRepository = ejercicioRepository;
        this.caudalRepository = caudalRepository;
        this.caudalEcologicoRepository = caudalEcologicoRepository;
    }

    /**
     * Lista todos los derechos de aprovechamiento de aguas.
     *
     * Esta consulta se cachea porque alimenta el listado principal del frontend
     * y suele repetirse varias veces sin cambios entre operaciones de escritura.
     *
     * La caché se guarda en Redis usando el TTL configurado en application.yml.
     */
    @Cacheable(cacheNames = "ddaa-list")
    public List<DdaaSummaryDto> findAllDdaa() {
        return queryRepository.findAllDdaa();
    }

    /**
     * Obtiene el detalle completo de un derecho de aprovechamiento de aguas.
     *
     * Esta consulta se cachea por ID porque reúne varias consultas relacionadas:
     * datos principales, expedientes, pagos de no uso y ejercicios.
     *
     * La clave usa el ID del DDAA para guardar cada detalle por separado.
     */
    @Cacheable(cacheNames = "ddaa-detail", key = "#id")
    public DdaaDetailDto findDdaaDetail(long id) {
        DdaaSummaryDto ddaa = queryRepository.findDdaaById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontro el derecho de agua " + id));

        List<ExpedienteDto> expedientes = queryRepository.findExpedientesByDdaaId(id);
        List<PagoNoUsoDto> pagosNoUso = queryRepository.findPagosNoUsoByDdaaId(id);
        List<EjercicioDto> ejercicios = queryRepository.findEjerciciosByDdaaId(id);

        return new DdaaDetailDto(ddaa, expedientes, pagosNoUso, ejercicios);
    }

    public List<ExpedienteDto> findExpedientesByDdaaId(long ddaaId) {
        return queryRepository.findExpedientesByDdaaId(ddaaId);
    }

    /**
     * Lista cuencas disponibles para formularios y filtros.
     *
     * Es un catálogo de baja volatilidad, por lo que se cachea con TTL Redis.
     */
    @Cacheable(cacheNames = "ddaa-catalog-cuencas")
    public List<CuencaDto> findCuencas() {
        return queryRepository.findCuencas();
    }

    /**
     * Lista subcuencas disponibles para formularios y filtros.
     *
     * Es un catálogo de baja volatilidad, por lo que se cachea con TTL Redis.
     */
    @Cacheable(cacheNames = "ddaa-catalog-subcuencas")
    public List<SubcuencaDto> findSubcuencas() {
        return queryRepository.findSubcuencas();
    }

    /**
     * Lista fuentes disponibles para formularios y filtros.
     *
     * Es un catálogo de baja volatilidad, por lo que se cachea con TTL Redis.
     */
    @Cacheable(cacheNames = "ddaa-catalog-fuentes")
    public List<FuenteDto> findFuentes() {
        return queryRepository.findFuentes();
    }

    /**
     * Lista comunas disponibles para formularios.
     *
     * Se retorna como ArrayList concreta para que Redis/Jackson pueda
     * serializar y deserializar correctamente la lista cacheada.
     */
    @Cacheable(cacheNames = "ddaa-catalog-comunas")
    public List<ComunaDto> findComunas() {
        return new ArrayList<>(
                comunaRepository.findAll(Sort.by("nombre")).stream()
                        .map(comuna -> new ComunaDto(comuna.getId(), comuna.getNombre()))
                        .toList()
        );
    }

    /**
     * Lista titulares/RUT disponibles para formularios.
     *
     * Se retorna como ArrayList concreta para evitar problemas de deserialización
     * al leer la respuesta desde Redis.
     */
    @Cacheable(cacheNames = "ddaa-catalog-ruts")
    public List<RutDto> findRuts() {
        return new ArrayList<>(
                rutRepository.findAll(Sort.by("nombre")).stream()
                        .map(rut -> new RutDto(rut.getRut(), rut.getNombre()))
                        .toList()
        );
    }

    /**
     * Lista instalaciones disponibles para formularios.
     *
     * Se retorna como ArrayList concreta para mantener un formato compatible
     * con el serializador JSON usado por Redis.
     */
    @Cacheable(cacheNames = "ddaa-catalog-instalaciones")
    public List<InstalacionDto> findInstalaciones() {
        return new ArrayList<>(
                instalacionRepository.findAll(Sort.by("nombre")).stream()
                        .map(instalacion -> new InstalacionDto(instalacion.getId().longValue(), instalacion.getNombre()))
                        .toList()
        );
    }

    @Transactional
// Invalida el listado cacheado cuando se crea un nuevo derecho de agua.
// Así evitamos que el frontend vea una lista antigua después de crear.
    @CacheEvict(cacheNames = "ddaa-list", allEntries = true)
    public long createDdaa(DdaaCreateDto dto) {
        Ddaa ddaa = new Ddaa();
        applyDdaaFields(ddaa, dto.comunaId(), dto.rutTitular(), dto.instalacionId(), dto.fuenteId(),
                dto.nombreFuenteDerecho(), dto.naturalezaDerecho(), dto.tipoDerecho(), dto.estadoDerecho());
        return ddaaRepository.save(ddaa).getId();
    }

    @Transactional
// Invalida las cachés afectadas cuando se edita un derecho de agua.
// Se limpia el listado completo y también el detalle específico del DDAA editado.
    @Caching(evict = {
            @CacheEvict(cacheNames = "ddaa-list", allEntries = true),
            @CacheEvict(cacheNames = "ddaa-detail", key = "#id")
    })
    public int updateDdaa(long id, DdaaUpdateDto dto) {
        return ddaaRepository.findById(toIntegerId(id))
                .map(ddaa -> {
                    applyDdaaFields(ddaa, dto.comunaId(), dto.rutTitular(), dto.instalacionId(), dto.fuenteId(),
                            dto.nombreFuenteDerecho(), dto.naturalezaDerecho(), dto.tipoDerecho(), dto.estadoDerecho());
                    ddaaRepository.save(ddaa);
                    return 1;
                })
                .orElse(0);
    }

    @Transactional
// Invalida las cachés afectadas cuando se elimina un derecho de agua.
// Se limpia el listado completo y también el detalle específico del DDAA eliminado.
    @Caching(evict = {
            @CacheEvict(cacheNames = "ddaa-list", allEntries = true),
            @CacheEvict(cacheNames = "ddaa-detail", key = "#id")
    })
    public int deleteDdaa(long id) {
        Integer entityId = toIntegerId(id);
        if (!ddaaRepository.existsById(entityId)) {
            return 0;
        }
        Ddaa ddaa = ddaaRepository.getReferenceById(entityId);
        ddaa.getExpedientes().clear();
        ddaaRepository.save(ddaa);

        pagoNoUsoRepository.deleteByDdaa_Id(entityId);
        for (DdaaEjercicio ejercicio : ejercicioRepository.findByDdaa_Id(entityId)) {
            caudalRepository.deleteByEjercicio_Id(ejercicio.getId());
            caudalEcologicoRepository.deleteByEjercicio_Id(ejercicio.getId());
        }
        ejercicioRepository.deleteByDdaa_Id(entityId);
        ddaaRepository.delete(ddaa);
        return 1;
    }

    private void applyDdaaFields(Ddaa ddaa, String comunaId, Long rutTitular, Long instalacionId, Long fuenteId,
                                 String nombreFuenteDerecho, String naturalezaDerecho, String tipoDerecho,
                                 String estadoDerecho) {
        ddaa.setComuna(comunaRepository.getReferenceById(comunaId));
        ddaa.setTitular(rutRepository.getReferenceById(rutTitular));
        ddaa.setInstalacion(instalacionId != null ? instalacionRepository.getReferenceById(toIntegerId(instalacionId)) : null);
        ddaa.setFuente(fuenteRepository.getReferenceById(toIntegerId(fuenteId)));
        ddaa.setNombreFuenteDerecho(nombreFuenteDerecho);
        ddaa.setNaturalezaDerecho(naturalezaDerecho);
        ddaa.setTipoDerecho(tipoDerecho);
        ddaa.setEstadoDerecho(estadoDerecho);
    }

    private Integer toIntegerId(Long id) {
        if (id == null) {
            return null;
        }
        return Math.toIntExact(id);
    }

    private Integer toIntegerId(long id) {
        return Math.toIntExact(id);
    }
}
