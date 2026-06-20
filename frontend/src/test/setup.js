import '@testing-library/jest-dom/vitest';
import { afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/react';

// Limpia el DOM después de cada test.
// Esto evita que renders anteriores de React queden acumulados y generen errores
// como "Found multiple elements" al buscar botones o textos repetidos.
afterEach(() => {
    cleanup();

    // Restaura spies globales como window.confirm entre tests.
    // Los mocks de módulos se vuelven a configurar en los beforeEach de cada archivo.
    vi.restoreAllMocks();
});