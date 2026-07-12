import apiClient from './apiClient';

export async function createReview(reviewData) {
  const { data } = await apiClient.post('/reviews', reviewData);
  return data;
}

export async function getBusinessReviews(businessId, page = 0, size = 10) {
  const { data } = await apiClient.get(`/reviews/business/${businessId}`, {
    params: { page, size },
  });
  return data;
}

export async function getAverageRating(businessId) {
  const { data } = await apiClient.get(`/reviews/business/${businessId}/average`);
  return data;
}
