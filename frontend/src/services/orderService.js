import apiClient from './apiClient';

export async function placeOrder(payload) {
  const { data } = await apiClient.post('/orders', payload);
  return data;
}

export async function getMyOrders(page = 0, size = 20) {
  const { data } = await apiClient.get('/orders/me', {
    params: { page, size },
  });
  return data;
}

export async function getBusinessOrders(page = 0, size = 20) {
  const { data } = await apiClient.get('/orders/business', {
    params: { page, size },
  });
  return data;
}

export async function getOrderDetails(orderId) {
  const { data } = await apiClient.get(`/orders/${orderId}`);
  return data;
}

export async function cancelOrder(orderId) {
  const { data } = await apiClient.post(`/orders/${orderId}/cancel`);
  return data;
}
