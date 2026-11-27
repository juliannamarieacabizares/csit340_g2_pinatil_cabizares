// API calls for the player pages (schedule and booking).
// Same fetch style as authApi.js: session cookie + error message from the server.

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

export const getCourts = () => request('/api/player/courts');

export const getScheduleAll = (date) => request(`/api/player/schedule-all?date=${date}`);

// Court details, end time, max hours, court fee and equipment stock for one choice.
export function getBookingOptions({ courtId, date, startTime, hours }) {
  const params = new URLSearchParams({ courtId, date, startTime, hours });
  return request(`/api/player/booking-options?${params}`);
}

// payload: { courtId, date, startTime, hours, equipment: [{ equipmentId, quantity }] }
export function createBooking(payload) {
  return request('/api/player/bookings', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

// Booking summary, hold countdown, payment status and the GCash account.
export const getBooking = (bookingId) => request(`/api/player/bookings/${bookingId}`);

// Sends the GCash reference number. The server sets the amount (full payment).
export function submitPayment(bookingId, referenceNumber) {
  return request(`/api/player/bookings/${bookingId}/payment`, {
    method: 'POST',
    body: JSON.stringify({ referenceNumber }),
  });
}
