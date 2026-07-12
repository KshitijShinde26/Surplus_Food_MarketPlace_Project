import apiClient from './apiClient';

export async function getBusinessAnalytics() {
  const { data } = await apiClient.get('/analytics/business');
  return data;
}

export async function getAdminAnalytics() {
  const { data } = await apiClient.get('/analytics/admin');
  return data;
}

export async function getNgoAnalytics() {
  const { data } = await apiClient.get('/analytics/ngo');
  return data;
}
