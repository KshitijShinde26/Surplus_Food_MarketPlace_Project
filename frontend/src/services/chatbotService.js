import apiClient from './apiClient';

export async function extractDetails(description) {
  const { data } = await apiClient.post('/chatbot/extract', { description });
  return data;
}
