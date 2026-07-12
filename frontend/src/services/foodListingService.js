import apiClient from './apiClient';

export async function createListing(payload) {
  const { data } = await apiClient.post('/food-listings', payload);
  return data;
}

export async function updateListing(listingId, payload) {
  const { data } = await apiClient.put(`/food-listings/${listingId}`, payload);
  return data;
}

export async function deleteListing(listingId) {
  const { data } = await apiClient.delete(`/food-listings/${listingId}`);
  return data;
}

export async function getMyListings(page = 0, size = 20) {
  const { data } = await apiClient.get('/food-listings/me', {
    params: { page, size },
  });
  return data;
}

export async function getListingById(listingId) {
  const { data } = await apiClient.get(`/food-listings/${listingId}`);
  return data;
}

export async function searchListings({
  categoryId,
  type,
  vegetarian,
  vegan,
  keyword,
  latitude,
  longitude,
  radius,
  page = 0,
  size = 20,
} = {}) {
  const { data } = await apiClient.get('/food-listings', {
    params: {
      categoryId,
      type,
      vegetarian,
      vegan,
      keyword,
      latitude,
      longitude,
      radius,
      page,
      size,
    },
  });
  return data;
}

export async function createFoodListing(payload) {
  return createListing(payload);
}

export async function getMyFoodListings(page = 0, size = 20) {
  return getMyListings(page, size);
}

export async function getActiveFoodListings(
  categoryId,
  type,
  vegetarian,
  vegan,
  keyword,
  page = 0,
  size = 20
) {
  return searchListings({
    categoryId,
    type,
    vegetarian,
    vegan,
    keyword,
    page,
    size,
  });
}

export async function getNearbyActiveFoodListings(
  latitude,
  longitude,
  radius,
  categoryId,
  type,
  vegetarian,
  vegan,
  keyword,
  page = 0,
  size = 20
) {
  return searchListings({
    latitude,
    longitude,
    radius,
    categoryId,
    type,
    vegetarian,
    vegan,
    keyword,
    page,
    size,
  });
}

