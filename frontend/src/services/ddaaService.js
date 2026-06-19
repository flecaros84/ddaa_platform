async function request(path, options = {}) {
  // Recupera el JWT guardado en el navegador.
  const token = localStorage.getItem('ddaa_access_token');

  // Ejecuta la petición al BFF, agregando Authorization si existe token.
  const response = await fetch(path, {
    credentials: 'include',
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  });

  // Si el backend rechaza la petición, limpiamos el token local.
  // No redirigimos automáticamente para evitar parpadeos o loops.
  if (response.status === 401) {
    localStorage.removeItem('ddaa_access_token');
    throw new Error('Sesión expirada o token JWT no enviado');
  }

  // DELETE puede responder sin contenido.
  if (response.status === 204) {
    return null;
  }

  // Lee la respuesta según su tipo de contenido.
  const contentType = response.headers.get('content-type') || '';
  const body = contentType.includes('application/json')
      ? await response.json()
      : await response.text();

  // Si la respuesta no fue exitosa, muestra el mensaje del backend cuando exista.
  if (!response.ok) {
    const message =
        typeof body === 'object' && body?.message
            ? body.message
            : 'No se pudo completar la operación';

    throw new Error(message);
  }

  return body;
}

export function fetchDdaaList() {
  return request('/bff/ddaa');
}

export function fetchDdaaDetail(id) {
  return request(`/bff/ddaa/${id}`);
}

export function fetchDdaaFormOptions() {
  return request('/bff/ddaa/form-options');
}

export function createDdaa(payload) {
  return request('/bff/ddaa', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export function updateDdaa(id, payload) {
  return request(`/bff/ddaa/${id}`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export function deleteDdaa(id) {
  return request(`/bff/ddaa/${id}`, {
    method: 'DELETE'
  });
}
