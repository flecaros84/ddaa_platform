async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...options.headers
    },
    ...options
  });

  if (response.status === 401) {
    window.location.href = '/oauth2/authorization/google';
    throw new Error('Sesion expirada');
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get('content-type') || '';
  const body = contentType.includes('application/json') ? await response.json() : await response.text();

  if (!response.ok) {
    const message = typeof body === 'object' && body?.message ? body.message : 'No se pudo completar la operacion';
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
