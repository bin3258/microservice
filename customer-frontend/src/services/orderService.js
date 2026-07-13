import api from '../api/axiosClient';

export const orderService = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  getByUserId: (userId) => api.get(`/orders/user/${userId}`),
  create: (data) => api.post('/orders', data),
  cancel: (id) => api.put(`/orders/${id}/cancel`),
  update: (id, data) => api.put(`/orders/${id}`, data),
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
