// Consulta la sesión actual al BFF.
// Si el backend devuelve un JWT, lo guarda en localStorage para usarlo en futuras llamadas protegidas.
export async function fetchCurrentUser() {
    const response = await fetch('/bff/session', {
        credentials: 'include'
    });

    if (!response.ok) {
        throw new Error('No se pudo obtener el usuario autenticado');
    }

    const data = await response.json();

    // Guarda el token JWT emitido por auth-service.
    if (data?.accessToken) {
        localStorage.setItem('ddaa_access_token', data.accessToken);
    } else {
        localStorage.removeItem('ddaa_access_token');
    }

    return data;
}

export function loginWithGoogle() {
    window.location.href = '/oauth2/authorization/google';
}

// Cierra sesión y elimina el JWT almacenado en el navegador.
export function logout() {
    localStorage.removeItem('ddaa_access_token');
    window.location.href = '/logout';
}
