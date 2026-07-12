import apiClient from './apiClient';

export async function uploadImage(file) {
  const formData = new FormData();
  formData.append('file', file);

  const { data } = await apiClient.post('/images/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return data;
}
