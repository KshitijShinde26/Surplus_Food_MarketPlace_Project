import apiClient from './apiClient';

export async function register(payload) {
  const { data } = await apiClient.post('/auth/register', payload);
  persistSession(data);
  return data;
}

export async function login(payload) {
  const { data } = await apiClient.post('/auth/login', payload);
  persistSession(data);
  return data;
}

export async function refreshSession() {
  const refreshToken = localStorage.getItem('refreshToken');
  const { data } = await apiClient.post('/auth/refresh', { refreshToken });
  persistSession(data);
  return data;
}

export async function logout() {
  const refreshToken = localStorage.getItem('refreshToken');
  if (refreshToken) {
    await apiClient.post('/auth/logout', { refreshToken });
  }
  clearSession();
}

export async function getCurrentUser() {
  const { data } = await apiClient.get('/auth/me');
  return data;
}

function persistSession(authResponse) {
  localStorage.setItem('accessToken', authResponse.accessToken);
  localStorage.setItem('refreshToken', authResponse.refreshToken);
  localStorage.setItem('currentUser', JSON.stringify(authResponse.user));
}

function clearSession() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('currentUser');
}
