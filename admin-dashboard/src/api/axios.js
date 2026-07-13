import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && window.location.pathname !== '/login') {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  register: (data) => api.post('/auth/register', data),
  validate: (data) => api.post('/auth/validate', data),
  changePassword: (userId, data) => api.put(`/auth/${userId}/password`, data),
  deleteUser: (userId) => api.delete(`/auth/${userId}`),
  changeRole: (userId, role) => api.put(`/auth/${userId}/role?role=${role}`),
};

export const productAPI = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  create: (data) => api.post('/products', data),
  update: (id, data) => api.put(`/products/${id}`, data),
  delete: (id) => api.delete(`/products/${id}`),
  getTrash: () => api.get('/products/trash'),
  restore: (id) => api.put(`/products/${id}/restore`),
  hardDelete: (id) => api.delete(`/products/${id}/hard`),
};

export const categoryAPI = {
  getAll: () => api.get('/categories'),
  getById: (id) => api.get(`/categories/${id}`),
  create: (data) => api.post('/categories', data),
  update: (id, data) => api.put(`/categories/${id}`, data),
  delete: (id) => api.delete(`/categories/${id}`),
};

export const orderAPI = {
  getAll: () => api.get('/orders'),
  getById: (id) => api.get(`/orders/${id}`),
  updateStatus: (id, data) => api.put(`/orders/${id}/status`, data),
  update: (id, data) => api.put(`/orders/${id}`, data),
  delete: (id) => api.delete(`/orders/${id}`),
};

export const postAPI = {
  getAll: () => api.get('/posts'),
  getById: (id) => api.get(`/posts/${id}`),
  create: (data) => api.post('/posts', data),
  update: (id, data) => api.put(`/posts/${id}`, data),
  delete: (id) => api.delete(`/posts/${id}`),
};

export const postCategoryAPI = {
  getAll: () => api.get('/post-categories'),
  create: (data) => api.post('/post-categories', data),
  update: (id, data) => api.put(`/post-categories/${id}`, data),
  delete: (id) => api.delete(`/post-categories/${id}`),
};

export const userAPI = {
  getAll: () => api.get('/users'),
  getById: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`),
  getByRole: (role) => api.get(`/users/role/${role}`),
  updateRole: (id, role) => api.put(`/users/${id}/role`, null, { params: { role } }),
};

export const customerAPI = {
  getAll: () => api.get('/customers'),
  getById: (id) => api.get(`/customers/${id}`),
  create: (data) => api.post('/customers', data),
  update: (id, data) => api.put(`/customers/${id}`, data),
  delete: (id) => api.delete(`/customers/${id}`),
};

export const inventoryAPI = {
  getAll: () => api.get('/inventory'),
  getDetails: (productId) => api.get(`/inventory/${productId}/details`),
  update: (productId, data) => api.put(`/inventory/${productId}`, data),
  delete: (productId) => api.delete(`/inventory/${productId}`),
  deleteByWarehouse: (productId, warehouseId) => api.delete(`/inventory/${productId}/${warehouseId}`),
};

export const warehouseAPI = {
  getAll: () => api.get('/warehouses'),
  getById: (id) => api.get(`/warehouses/${id}`),
  create: (data) => api.post('/warehouses', data),
  update: (id, data) => api.put(`/warehouses/${id}`, data),
  delete: (id) => api.delete(`/warehouses/${id}`),
};

export const paymentAPI = {
  getAll: () => api.get('/payments'),
  updateStatus: (id, data) => api.put(`/payments/${id}/status`, data),
};

export const addressAPI = {
  getByCustomer: (customerId) => api.get(`/addresses/customer/${customerId}`),
  create: (data) => api.post('/addresses', data),
  update: (id, data) => api.put(`/addresses/${id}`, data),
  setDefault: (id, customerId) => api.put(`/addresses/${id}/default?customerId=${customerId}`),
  delete: (id) => api.delete(`/addresses/${id}`),
};
