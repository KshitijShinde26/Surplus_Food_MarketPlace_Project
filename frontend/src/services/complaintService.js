import apiClient from './apiClient';

export async function fileComplaint(complaintData) {
  const { data } = await apiClient.post('/complaints', complaintData);
  return data;
}

export async function getMyComplaints(page = 0, size = 20) {
  const { data } = await apiClient.get('/complaints/me', {
    params: { page, size },
  });
  return data;
}

export async function searchComplaints(status = null, businessId = null, page = 0, size = 20) {
  const { data } = await apiClient.get('/admin/complaints', {
    params: { status, businessId, page, size },
  });
  return data;
}

export async function updateComplaintStatus(id, status) {
  const { data } = await apiClient.patch(`/admin/complaints/${id}/status`, null, {
    params: { status },
  });
  return data;
}
