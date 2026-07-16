import api from '../api/axiosClient';

export const orderService = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  getByUserId: (userId) => api.get(`/orders/user/${userId}`),
  create: (data) => api.post('/orders', data),
  cancel: (id) => api.put(`/orders/${id}/cancel`),
  update: (id, data) => api.put(`/orders/${id}`, data),
  getInvoice: (id) => api.get(`/orders/${id}/invoice`, { responseType: 'blob' }),
};

export const paymentService = {
  create: (data) => api.post('/payments', data),
  getByOrderId: (orderId) => api.get(`/payments/order/${orderId}`),
};

export const cartService = {
  getCart: (userId) => api.get(`/cart/${userId}`),
  addItem: (userId, data) => api.post(`/cart/${userId}`, data),
  updateItem: (itemId, data) => api.put(`/cart/items/${itemId}`, data),
  removeItem: (itemId) => api.delete(`/cart/items/${itemId}`),
  clearCart: (userId) => api.delete(`/cart/${userId}`),
};

export const customerService = {
  getByAuthId: (authUserId) => api.get(`/customers/auth/${authUserId}`),
  create: (data) => api.post('/customers', data),
  update: (id, data) => api.put(`/customers/${id}`, data),
};

export const addressService = {
  getByCustomer: (customerId) => api.get(`/addresses/customer/${customerId}`),
  create: (data) => api.post('/addresses', data),
  update: (id, data) => api.put(`/addresses/${id}`, data),
  delete: (id) => api.delete(`/addresses/${id}`),
  setDefault: (id, customerId) => api.put(`/addresses/${id}/default?customerId=${customerId}`),
};

export const discountService = {
  validate: (data) => api.post('/discounts/validate', data),
  getAvailable: (userId) => api.get('/discounts/available', { params: { userId } }),
};

export const reviewService = {
  getByProduct: (productId) => api.get(`/reviews/product/${productId}`),
  getProductStats: (productId) => api.get(`/reviews/product/${productId}/stats`),
  getMyReview: (userId, productId, orderId) => api.get('/reviews/my', { params: { userId, productId, orderId } }),
  create: (formData) => api.post('/reviews', formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  update: (id, formData) => api.put(`/reviews/${id}`, formData, { headers: { 'Content-Type': 'multipart/form-data' } }),
  delete: (id) => api.delete(`/reviews/${id}`),
};
