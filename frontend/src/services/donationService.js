import apiClient from './apiClient';

export async function claimDonation(payload) {
  const { data } = await apiClient.post('/donations/claim', payload);
  return data;
}

export async function getMyClaims(page = 0, size = 20) {
  const { data } = await apiClient.get('/donations/me', {
    params: { page, size },
  });
  return data;
}

export async function getBusinessClaims(page = 0, size = 20) {
  const { data } = await apiClient.get('/donations/business', {
    params: { page, size },
  });
  return data;
}

export async function getDonationDetails(donationId) {
  const { data } = await apiClient.get(`/donations/${donationId}`);
  return data;
}

export async function approveDonation(donationId) {
  const { data } = await apiClient.post(`/donations/${donationId}/approve`);
  return data;
}

export async function cancelDonation(donationId) {
  const { data } = await apiClient.post(`/donations/${donationId}/cancel`);
  return data;
}
