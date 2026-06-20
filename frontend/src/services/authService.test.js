import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fetchCurrentUser, loginWithGoogle, logout } from './authService';

describe('authService', () => {
    beforeEach(() => {
        // Cada test parte con mocks limpios para evitar contaminación cruzada.
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    it('fetchCurrentUser should return authenticated user data', async () => {
        // Arrange: BFF responde sesión autenticada.
        const user = {
            authenticated: true,
            name: 'Usuario Test',
            email: 'usuario@camanchaca.cl'
        };

        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(user)
        });

        // Act: se consulta sesión actual.
        const result = await fetchCurrentUser();

        // Assert: se usa la ruta BFF y se retorna el JSON.
        expect(fetch).toHaveBeenCalledWith('/bff/session', {
            credentials: 'include'
        });
        expect(result).toEqual(user);
    });

    it('fetchCurrentUser should throw when response is not ok', async () => {
        // Arrange: BFF responde error.
        globalThis.fetch = vi.fn().mockResolvedValue({
            ok: false
        });

        // El servicio real retorna mensaje en español.
        await expect(fetchCurrentUser()).rejects.toThrow('No se pudo obtener el usuario autenticado');
    });

    it('loginWithGoogle should redirect to Google OAuth endpoint', () => {
        // Arrange: se reemplaza location por un objeto simple para evitar navegación real.
        vi.stubGlobal('location', { href: '' });

        // Act: se inicia login.
        loginWithGoogle();

        // Assert: se redirige al endpoint OAuth del gateway.
        expect(globalThis.location.href).toBe('/oauth2/authorization/google');
    });

    it('logout should redirect to logout endpoint', () => {
        // Arrange: se reemplaza location por un objeto simple para evitar navegación real.
        vi.stubGlobal('location', { href: '' });

        // Act: se cierra sesión.
        logout();

        // Assert: se redirige al endpoint de logout.
        expect(globalThis.location.href).toBe('/logout');
    });
});