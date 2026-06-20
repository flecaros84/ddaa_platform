import { beforeEach, describe, expect, it, vi } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import App from './App';
import { fetchCurrentUser } from './services/authService';

vi.mock('./services/authService', () => ({
    // Se mockea la llamada de sesión para controlar estados de App.
    fetchCurrentUser: vi.fn()
}));

vi.mock('./pages/LoginPage', () => ({
    default: () => <div>LoginPage Test</div>
}));

vi.mock('./pages/HomePage', () => ({
    default: ({ user }) => <div>HomePage Test {user.email}</div>
}));

describe('App', () => {
    beforeEach(() => {
        // Limpia mocks antes de cada escenario.
        vi.clearAllMocks();
    });

    it('should show loading state before session resolves', () => {
        // Arrange: promesa pendiente para mantener loading activo.
        fetchCurrentUser.mockReturnValue(new Promise(() => {}));

        // Act: se renderiza App.
        render(<App />);

        // Assert: se muestra estado de carga.
        expect(screen.getByText('Cargando...')).toBeInTheDocument();
    });

    it('should render HomePage when user is authenticated', async () => {
        // Arrange: sesión autenticada.
        fetchCurrentUser.mockResolvedValue({
            authenticated: true,
            email: 'usuario@camanchaca.cl'
        });

        // Act: se renderiza App.
        render(<App />);

        // Assert: se renderiza HomePage con usuario.
        expect(await screen.findByText('HomePage Test usuario@camanchaca.cl')).toBeInTheDocument();
    });

    it('should render LoginPage when user is anonymous', async () => {
        // Arrange: sesión no autenticada.
        fetchCurrentUser.mockResolvedValue({
            authenticated: false
        });

        // Act: se renderiza App.
        render(<App />);

        // Assert: se renderiza LoginPage.
        expect(await screen.findByText('LoginPage Test')).toBeInTheDocument();
    });

    it('should render LoginPage when session request fails', async () => {
        // Arrange: error consultando sesión.
        fetchCurrentUser.mockRejectedValue(new Error('BFF unavailable'));

        // Act: se renderiza App.
        render(<App />);

        // Assert: ante error se vuelve a login.
        await waitFor(() => {
            expect(screen.getByText('LoginPage Test')).toBeInTheDocument();
        });
    });
});