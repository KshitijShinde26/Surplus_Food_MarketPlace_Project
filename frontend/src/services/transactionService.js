import apiClient from './apiClient';

export async function getBusinessTransactions(page = 0, size = 20) {
  const { data } = await apiClient.get('/transactions', {
    params: { page, size },
  });
  return data;
}

export async function getTotalSalesEarnings() {
  const { data } = await apiClient.get('/transactions/earnings');
  return data;
}
