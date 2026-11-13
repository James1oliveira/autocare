// All requests go through the API gateway, which routes them to the right service.
export const API_URL = (import.meta.env.VITE_API_URL || 'http://localhost:8080').replace(/\/$/, '');

export async function api(path, { method = 'GET', body, token } = {}) {
  const headers = { Accept: 'application/json' };
  if (body !== undefined) headers['Content-Type'] = 'application/json';
  if (token) headers.Authorization = `Bearer ${token}`;

  let response;
  try {
    response = await fetch(`${API_URL}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new Error("Can't reach the server. It may be starting up, so try again in a minute.");
  }

  const text = await response.text();
  const data = text ? safeJson(text) : null;
  if (!response.ok) {
    const error = new Error(data?.error || `Something went wrong (${response.status})`);
    error.status = response.status;
    throw error;
  }
  return data;
}

function safeJson(text) {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
}

export function formatPrice(from, to) {
  const rand = (n) => `R${n.toLocaleString('en-ZA')}`;
  if (from == null) return 'Price on inspection';
  return from === to ? rand(from) : `${rand(from)} – ${rand(to)}`;
}

export const STATUS_LABELS = {
  BOOKED: 'Booked',
  IN_PROGRESS: 'In progress',
  READY: 'Ready to collect',
  COLLECTED: 'Collected',
  CANCELLED: 'Cancelled',
};

export const ACTION_LABELS = {
  IN_PROGRESS: 'Start job',
  READY: 'Mark ready',
  COLLECTED: 'Mark collected',
  CANCELLED: 'Cancel booking',
};
