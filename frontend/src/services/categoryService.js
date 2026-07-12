import apiClient from './apiClient';

export async function getCategories() {
  const { data } = await apiClient.get('/categories');
  return data;
}
