export async function fetchCurrentUser() {
    const response = await fetch('/bff/session', {
        credentials: 'include'
    });

    if (!response.ok) {
        throw new Error('Could not fetch authenticated user');
    }

    return response.json();
}

export function loginWithGoogle() {
    window.location.href = '/oauth2/authorization/google';
}

export function logout() {
    window.location.href = '/logout';
}
