import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import HomePage from './HomePage';
import { logout } from '../services/authService';
import {
    createDdaa,
    deleteDdaa,
    fetchDdaaDetail,
    fetchDdaaFormOptions,
    fetchDdaaList,
    updateDdaa
} from '../services/ddaaService';

vi.mock('../services/authService', () => ({
    // Se mockea logout para no redirigir durante pruebas.
    logout: vi.fn()
}));

vi.mock('../services/ddaaService', () => ({
    // Se mockean todas las llamadas BFF para aislar la UI.
    fetchDdaaList: vi.fn(),
    fetchDdaaDetail: vi.fn(),
    fetchDdaaFormOptions: vi.fn(),
    createDdaa: vi.fn(),
    updateDdaa: vi.fn(),
    deleteDdaa: vi.fn()
}));

describe('HomePage', () => {
    beforeEach(() => {
        // Estado limpio para cada test de UI.
        vi.clearAllMocks();

        fetchDdaaList.mockResolvedValue(sampleList());
        fetchDdaaFormOptions.mockResolvedValue(sampleOptions());
        fetchDdaaDetail.mockResolvedValue(sampleDetail());
    });

    it('should load initial DDAA list, form options and selected detail', async () => {
        // Arrange & Act: se renderiza la página principal con usuario autenticado.
        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);

        // Assert: muestra usuario, listado y detalle cargado.
        expect(screen.getByText('Usuario Test')).toBeInTheDocument();
        expect(await screen.findByText('1 registros')).toBeInTheDocument();
        expect(screen.getByText('Titular Uno')).toBeInTheDocument();
        expect(screen.getByText('Fuente Derecho Uno')).toBeInTheDocument();

        // El detalle se carga después de seleccionar automáticamente el primer registro.
        expect(await screen.findByText('Cuenca Uno')).toBeInTheDocument();
        expect(screen.getByText('Subcuenca Uno')).toBeInTheDocument();

        expect(fetchDdaaList).toHaveBeenCalledTimes(1);
        expect(fetchDdaaFormOptions).toHaveBeenCalledTimes(1);
        expect(fetchDdaaDetail).toHaveBeenCalledWith(1);
    });

    it('should call logout when user clicks logout button', async () => {
        // Arrange: página cargada.
        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('1 registros');

        // Act: usuario cierra sesión.
        fireEvent.click(screen.getByRole('button', { name: 'Cerrar sesión' }));

        // Assert: se delega al servicio de auth.
        expect(logout).toHaveBeenCalledTimes(1);
    });

    it('should create DDAA using default catalog values', async () => {
        // Arrange: creación exitosa y recarga posterior del listado.
        createDdaa.mockResolvedValue({ id: 2 });
        fetchDdaaList.mockResolvedValue(sampleList());

        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('1 registros');

        // Act: el formulario ya queda precargado con catálogos mínimos.
        await userEvent.click(screen.getByRole('button', { name: 'Crear DDAA' }));

        // Assert: se envía payload normalizado con números donde corresponde.
        await waitFor(() => {
            expect(createDdaa).toHaveBeenCalledWith({
                comunaId: '001',
                rutTitular: 11111111,
                instalacionId: 5,
                fuenteId: 3,
                nombreFuenteDerecho: 'Fuente Uno',
                naturalezaDerecho: 'Consuntivo',
                tipoDerecho: 'Aprovechamiento',
                estadoDerecho: 'Vigente'
            });
        });

        expect(await screen.findByText('Derecho creado.')).toBeInTheDocument();
    });

    it('should edit selected DDAA and send updated payload', async () => {
        // Arrange: update exitoso.
        updateDdaa.mockResolvedValue(null);

        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('1 registros');

        // Act: se entra a modo edición.
        await userEvent.click(screen.getByRole('button', { name: 'Editar' }));

        const estadoInput = screen.getByLabelText('Estado');
        await userEvent.clear(estadoInput);
        await userEvent.type(estadoInput, 'Inactivo');

        await userEvent.click(screen.getByRole('button', { name: 'Guardar cambios' }));

        // Assert: se invoca update con ID seleccionado y payload convertido.
        await waitFor(() => {
            expect(updateDdaa).toHaveBeenCalledWith(1, expect.objectContaining({
                comunaId: '001',
                rutTitular: 11111111,
                instalacionId: 5,
                fuenteId: 3,
                nombreFuenteDerecho: 'Fuente Derecho Uno',
                estadoDerecho: 'Inactivo'
            }));
        });

        expect(await screen.findByText('Derecho actualizado.')).toBeInTheDocument();
    });

    it('should delete DDAA when user confirms deletion', async () => {
        // Arrange: confirmación positiva y delete exitoso.
        // La primera llamada carga el listado inicial con 1 registro.
        // La segunda llamada simula el listado vacío después de eliminar.
        vi.spyOn(window, 'confirm').mockReturnValue(true);
        deleteDdaa.mockResolvedValue(null);
        fetchDdaaList
            .mockResolvedValueOnce(sampleList())
            .mockResolvedValueOnce([]);

        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);

        // Assert inicial: primero debe existir el registro antes de eliminarlo.
        await screen.findByText('1 registros');

        // Act: se elimina el registro seleccionado.
        await userEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        // Assert: se llama al servicio con el ID seleccionado.
        await waitFor(() => {
            expect(deleteDdaa).toHaveBeenCalledWith(1);
        });

        // Assert final: después de eliminar, el listado queda vacío.
        expect(await screen.findByText('Derecho eliminado.')).toBeInTheDocument();
        expect(await screen.findByText('0 registros')).toBeInTheDocument();
    });

    it('should not delete DDAA when user cancels confirmation', async () => {
        // Arrange: usuario cancela confirmación.
        vi.spyOn(window, 'confirm').mockReturnValue(false);

        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('1 registros');

        // Act: intenta eliminar.
        await userEvent.click(screen.getByRole('button', { name: 'Eliminar' }));

        // Assert: no se llama al backend.
        expect(deleteDdaa).not.toHaveBeenCalled();
    });

    it('should show error when initial data loading fails', async () => {
        // Arrange: error cargando datos iniciales.
        fetchDdaaList.mockRejectedValue(new Error('No se pudo cargar DDAA'));

        // Act: se renderiza página.
        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);

        // Assert: muestra mensaje de error.
        expect(await screen.findByText('No se pudo cargar DDAA')).toBeInTheDocument();
    });

    it('should show warning and disable submit when required catalogs are missing', async () => {
        // Arrange: catálogos requeridos vacíos.
        fetchDdaaList.mockResolvedValue([]);
        fetchDdaaFormOptions.mockResolvedValue({
            comunas: [],
            ruts: [],
            instalaciones: [],
            cuencas: [],
            subcuencas: [],
            fuentes: []
        });

        // Act: se renderiza página sin catálogos.
        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('0 registros');

        // Assert: el formulario advierte y bloquea creación.
        expect(screen.getByText('Faltan datos de catálogo para crear registros. Carga al menos comuna, titular y fuente.')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Crear DDAA' })).toBeDisabled();
    });

    it('should select row and keep visible detail panel', async () => {
        // Arrange: listado cargado.
        render(<HomePage user={{ name: 'Usuario Test', email: 'usuario@camanchaca.cl' }} />);
        await screen.findByText('1 registros');

// Se usa una celda estable de la fila para evitar problemas con el texto "#1",
// que en el DOM puede renderizarse separado como "#" y "1".
        const row = screen.getByText('Titular Uno').closest('tr');
        fireEvent.click(row);

        // Assert: el panel de detalle queda asociado al registro.
        const detailPanel = screen.getByRole('heading', { name: 'Detalle' }).closest('section');
        expect(within(detailPanel).getByText('DDAA #1')).toBeInTheDocument();
    });

    function sampleList() {
        return [
            {
                id: 1,
                comunaId: '001',
                comunaNombre: 'Santiago',
                rutTitular: 11111111,
                titularNombre: 'Titular Uno',
                instalacionId: 5,
                instalacionNombre: 'Instalacion Uno',
                fuenteId: 3,
                fuenteNombre: 'Fuente Uno',
                fuenteTipo: 'Superficial',
                nombreFuenteDerecho: 'Fuente Derecho Uno',
                naturalezaDerecho: 'Consuntivo',
                tipoDerecho: 'Aprovechamiento',
                estadoDerecho: 'Vigente',
                cuencaId: 10,
                cuencaNombre: 'Cuenca Uno',
                subcuencaId: 20,
                subcuencaNombre: 'Subcuenca Uno'
            }
        ];
    }

    function sampleOptions() {
        return {
            comunas: [
                { id: '001', nombre: 'Santiago' }
            ],
            ruts: [
                { rut: 11111111, nombre: 'Titular Uno' }
            ],
            instalaciones: [
                { id: 5, nombre: 'Instalacion Uno' }
            ],
            cuencas: [
                { id: 10, nombre: 'Cuenca Uno' }
            ],
            subcuencas: [
                { id: 20, nombre: 'Subcuenca Uno' }
            ],
            fuentes: [
                { id: 3, nombre: 'Fuente Uno', tipo: 'Superficial' }
            ]
        };
    }

    function sampleDetail() {
        return {
            ddaa: {
                id: 1,
                cuencaNombre: 'Cuenca Uno',
                subcuencaNombre: 'Subcuenca Uno',
                fuenteNombre: 'Fuente Uno',
                fuenteTipo: 'Superficial'
            },
            expedientes: [{ id: 100 }],
            pagosNoUso: [{ id: 200 }],
            ejercicios: [{ id: 300 }]
        };
    }
});