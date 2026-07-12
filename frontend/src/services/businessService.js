import apiClient from './apiClient';

export async function createBusinessProfile(payload) {
  const { data } = await apiClient.post('/business/profile', payload);
  return data;
}

export async function getMyBusinessProfile() {
  const { data } = await apiClient.get('/business/profile/me');
  return data;
}

export async function updateMyBusinessProfile(payload) {
  const { data } = await apiClient.put('/business/profile/me', payload);
  return data;
}

export async function searchBusinesses({ verified, keyword, page = 0, size = 20 } = {}) {
  const { data } = await apiClient.get('/admin/businesses', {
    params: {
      verified,
      keyword,
      page,
      size,
    },
  });
  return data;
}

export async function verifyBusiness(businessId) {
  const { data } = await apiClient.patch(`/admin/businesses/${businessId}/verify`);
  return data;
}

export async function blockBusiness(businessId) {
  const { data } = await apiClient.patch(`/admin/businesses/${businessId}/block`);
  return data;
}

export async function markBusinessPending(businessId) {
  const { data } = await apiClient.patch(`/admin/businesses/${businessId}/pending`);
  return data;
}

export async function getBusinessProfileAdminList(page = 0, size = 100) {
  return searchBusinesses({ verified: null, page, size });
}

export async function verifyBusinessProfile(businessId) {
  return verifyBusiness(businessId);
}

