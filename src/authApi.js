
const apiBaseUrl =
  import.meta.env.VITE_API_URL ||
  `http://${window.location.hostname}:8080`;

async function request(path, options = {}) {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    credentials: 'include',
    headers: {
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...options.headers,
    },
  });

  const contentType = response.headers.get('content-type');
  const data = contentType?.includes('application/json')
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    throw new Error(
      typeof data === 'string'
        ? data || 'Request failed.'
        : data.message || data.error || 'Request failed.'
    );
  }

  return data;
}

export function registerAccount(accountData) {
  return request('/api/users/register', {
    method: 'POST',
    body: JSON.stringify(accountData),
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
  return request('/api/auth/logout', {
    method: 'POST',
  });
}

export function requestPasswordReset(email) {
  return request('/api/auth/password-reset/request', {
    method: 'POST',
    body: JSON.stringify({ email }),
  });
}

export function completePasswordReset(token, newPassword) {
  return request('/api/auth/password-reset/complete', {
    method: 'POST',
    body: JSON.stringify({ token, newPassword }),
  });
}

export function adminPing() {
  return request('/api/admin/ping');
}

export function listItems(search = '', page = 0, size = 10) {
  const params = new URLSearchParams({
    search,
    page: String(page),
    size: String(size),
  });

  return request(`/api/items?${params.toString()}`);
}

export function createItem(itemData) {
  return request('/api/items', {
    method: 'POST',
    body: JSON.stringify(itemData),
  });
}

export function updateItem(itemId, itemData) {
  return request(`/api/items/${itemId}`, {
    method: 'PUT',
    body: JSON.stringify(itemData),
  });
}

export function deleteItem(itemId) {
  return request(`/api/items/${itemId}`, {
    method: 'DELETE',
  });
}

export function getActiveCourt() {
  return request('/api/bookings/court');
}

export function getBookingTimeSlots() {
  return request('/api/bookings/time-slots');
}

export function getBookingAvailability(date) {
  return request(
    `/api/bookings/availability?date=${encodeURIComponent(date)}`
  );
}

export function createBooking(bookingData) {
  return request('/api/bookings', {
    method: 'POST',
    body: JSON.stringify(bookingData),
  });
}

export function getMyBookings() {
  return request('/api/bookings/my-bookings');
}

export function requestBookingCancellation(bookingId, reason) {
  return request(`/api/bookings/${bookingId}/cancellation`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  });
}

export function getMyCancellations() {
  return request('/api/bookings/my-cancellations');
}

export function getPendingCancellations() {
  return request('/api/admin/cancellations/pending');
}

export function approveCancellation(cancellationId) {
  return request(`/api/admin/cancellations/${cancellationId}/approve`, {
    method: 'PUT',
  });
}

export function rejectCancellation(cancellationId) {
  return request(`/api/admin/cancellations/${cancellationId}/reject`, {
    method: 'PUT',
  });
}
