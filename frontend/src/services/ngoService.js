import apiClient from './apiClient';

export async function createNgoProfile(payload) {
  const { data } = await apiClient.post('/ngo/profile', payload);
  return data;
}

export async function getMyNgoProfile() {
  const { data } = await apiClient.get('/ngo/profile/me');
  return data;
}

export async function updateMyNgoProfile(payload) {
  const { data } = await apiClient.put('/ngo/profile/me', payload);
  return data;
}

export async function searchNgos({ verified, keyword, page = 0, size = 20 } = {}) {
  const { data } = await apiClient.get('/admin/ngos', {
    params: { verified, keyword, page, size },
  });
  return data;
}

export async function verifyNgo(ngoId) {
  const { data } = await apiClient.patch(`/admin/ngos/${ngoId}/verify`);
  return data;
}

export async function blockNgo(ngoId) {
  const { data } = await apiClient.patch(`/admin/ngos/${ngoId}/block`);
  return data;
}

export async function markNgoPending(ngoId) {
  const { data } = await apiClient.patch(`/admin/ngos/${ngoId}/pending`);
  return data;
}

export async function getNgoProfileAdminList(page = 0, size = 100) {
  return searchNgos({ verified: null, page, size });
}

export async function verifyNgoProfile(ngoId) {
  return verifyNgo(ngoId);
}

