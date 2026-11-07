const apiBaseUrl = import.meta.env.VITE_API_URL
  || `${window.location.protocol}//${window.location.hostname}:8080`;

async function request(path, options = {}) {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  });

  if (response.status === 204) return null;

  const body = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(body.detail || body.message || 'The request could not be completed.');
  }
  return body;
}

export function registerAccount(formData) {
  return request('/api/users/register', {
    method: 'POST',
    body: JSON.stringify(formData),
  });
}

export function login(credentials) {
  return request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify(credentials),
  });
}

export function getCurrentUser() {
  return request('/api/auth/me');
}

export function logout() {
  return request('/api/auth/logout', { method: 'POST' });
}

export function requestPasswordReset(email) {
  return request('/api/auth/password-reset/request', {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

export function completePasswordReset(token, password) {
  return request('/api/auth/password-reset/complete', {
    method: 'POST',
    body: JSON.stringify({ token, password }),
  });
}

export function adminPing() {
  return request('/api/admin/ping');
}

export function listItems({ search = '', page = 0, size = 5 } = {}) {
  const params = new URLSearchParams({ search, page, size });
  return request(`/api/items?${params}`);
}
export function createItem(item) {
  return request('/api/items', { method: 'POST', body: JSON.stringify(item) });
}
export function updateItem(id, item) {
  return request(`/api/items/${id}`, { method: 'PUT', body: JSON.stringify(item) });
}
export function deleteItem(id) {
  return request(`/api/items/${id}`, { method: 'DELETE' });
}