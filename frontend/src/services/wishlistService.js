import apiClient from './apiClient';

export async function addToWishlist(businessId) {
  const { data } = await apiClient.post(`/wishlist/${businessId}`);
  return data;
}

export async function removeFromWishlist(businessId) {
  const { data } = await apiClient.delete(`/wishlist/${businessId}`);
  return data;
}

export async function getMyWishlist() {
  const { data } = await apiClient.get('/wishlist/me');
  return data;
}

export async function checkWishlistStatus(businessId) {
  const { data } = await apiClient.get(`/wishlist/${businessId}/status`);
  return data;
}
