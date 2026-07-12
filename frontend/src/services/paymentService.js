import apiClient from './apiClient';

export async function createPaymentIntent(orderId) {
  const { data } = await apiClient.post(`/payments/create-intent/${orderId}`);
  return data;
}

export async function simulateWebhookSuccess(stripePaymentIntentId) {
  const { data } = await apiClient.post('/payments/webhook', {
    type: 'payment_intent.succeeded',
    data: {
      object: {
        id: stripePaymentIntentId
      }
    }
  });
  return data;
}
