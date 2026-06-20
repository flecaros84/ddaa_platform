import { describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import LoginPage from './LoginPage';
import { loginWithGoogle } from '../services/authService';

vi.mock('../services/authService', () => ({
    // Se mockea navegación OAuth para probar el click sin redirigir el navegador.
    loginWithGoogle: vi.fn()
}));

describe('LoginPage', () => {
    it('should render login content and call Google login action', () => {
        // Arrange: se renderiza página pública.
        render(<LoginPage />);

        // Assert: muestra marca y texto principal.
        expect(screen.getByRole('heading', { name: 'DDAA Platform' })).toBeInTheDocument();
        expect(screen.getByText('Inicia sesión con tu cuenta corporativa de Google.')).toBeInTheDocument();

        // Act: usuario presiona botón de login.
        fireEvent.click(screen.getByRole('button', { name: 'Iniciar sesión con Google' }));

        // Assert: delega en authService.
        expect(loginWithGoogle).toHaveBeenCalledTimes(1);
    });
});