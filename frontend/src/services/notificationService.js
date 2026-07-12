import apiClient from './apiClient';

export async function getMyNotifications(page = 0, size = 20) {
  const { data } = await apiClient.get('/notifications', {
    params: { page, size },
  });
  return data;
}

export async function markAsRead(notificationId) {
  const { data } = await apiClient.patch(`/notifications/${notificationId}/read`);
  return data;
}

export async function markAllAsRead() {
  const { data } = await apiClient.post('/notifications/read-all');
  return data;
}
