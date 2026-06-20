import { beforeEach, describe, expect, it, vi } from 'vitest';
import {
    createDdaa,
    deleteDdaa,
    fetchDdaaDetail,
    fetchDdaaFormOptions,
    fetchDdaaList,
    updateDdaa
} from './ddaaService';

describe('ddaaService', () => {
    beforeEach(() => {
        // Limpia mocks globales antes de cada escenario.
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    it('fetchDdaaList should request DDAA list from BFF', async () => {
        // Arrange: respuesta JSON exitosa.
        const list = [{ id: 1, estadoDerecho: 'Activo' }];
        mockJsonResponse(200, list);

        // Act: se consulta listado.
        const result = await fetchDdaaList();

        // Assert: se llama endpoint correcto y retorna listado.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa', expect.objectContaining({
            credentials: 'include'
        }));
        expect(result).toEqual(list);
    });

    it('fetchDdaaDetail should request DDAA detail by id', async () => {
        // Arrange: detalle compuesto.
        const detail = {
            ddaa: { id: 10 },
            expedientes: [],
            pagosNoUso: [],
            ejercicios: []
        };
        mockJsonResponse(200, detail);

        // Act: se consulta detalle.
        const result = await fetchDdaaDetail(10);

        // Assert: se usa path variable.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa/10', expect.any(Object));
        expect(result).toEqual(detail);
    });

    it('fetchDdaaFormOptions should request catalog options', async () => {
        // Arrange: opciones de formulario.
        const options = {
            comunas: [],
            ruts: [],
            instalaciones: [],
            cuencas: [],
            subcuencas: [],
            fuentes: []
        };
        mockJsonResponse(200, options);

        // Act: se consultan catálogos.
        const result = await fetchDdaaFormOptions();

        // Assert: retorna opciones.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa/form-options', expect.any(Object));
        expect(result).toEqual(options);
    });

    it('createDdaa should send POST with JSON body', async () => {
        // Arrange: payload de creación.
        const payload = {
            comunaId: '001',
            rutTitular: 11111111,
            fuenteId: 3,
            nombreFuenteDerecho: 'Fuente Test'
        };

        mockJsonResponse(201, { id: 99 });

        // Act: se crea DDAA.
        const result = await createDdaa(payload);

        // Assert: se envía POST con body serializado.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa', expect.objectContaining({
            method: 'POST',
            body: JSON.stringify(payload)
        }));
        expect(result).toEqual({ id: 99 });
    });

    it('updateDdaa should return null when backend responds 204', async () => {
        // Arrange: respuesta sin contenido.
        mockNoContentResponse();

        // Act: se actualiza DDAA.
        const result = await updateDdaa(10, { estadoDerecho: 'Inactivo' });

        // Assert: el servicio interpreta 204 como null.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa/10', expect.objectContaining({
            method: 'PUT'
        }));
        expect(result).toBeNull();
    });

    it('deleteDdaa should send DELETE request', async () => {
        // Arrange: eliminación exitosa.
        mockNoContentResponse();

        // Act: se elimina DDAA.
        const result = await deleteDdaa(10);

        // Assert: usa método DELETE.
        expect(fetch).toHaveBeenCalledWith('/bff/ddaa/10', expect.objectContaining({
            method: 'DELETE'
        }));
        expect(result).toBeNull();
    });

    it('request should throw backend JSON message when response is not ok', async () => {
        // Arrange: backend retorna error JSON.
        mockJsonResponse(400, { message: 'Datos inválidos' }, false);

        // Act & Assert: se propaga mensaje del backend.
        await expect(fetchDdaaList()).rejects.toThrow('Datos inválidos');
    });

    it('request should throw default message when error body has no message', async () => {
        // Arrange: backend retorna texto plano sin mensaje estructurado.
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false,
            status: 500,
            headers: {
                get: vi.fn().mockReturnValue('text/plain')
            },
            text: vi.fn().mockResolvedValue('error interno')
        });

        // El mensaje real usa tilde en "operación".
        await expect(fetchDdaaList()).rejects.toThrow('No se pudo completar la operación');
    });

    function mockJsonResponse(status, body, ok = true) {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok,
            status,
            headers: {
                get: vi.fn().mockReturnValue('application/json')
            },
            json: vi.fn().mockResolvedValue(body)
        });
    }

    function mockNoContentResponse() {
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            status: 204,
            headers: {
                get: vi.fn()
            }
        });
    }
});