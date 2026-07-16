import api from '../api/axiosClient';

export const productService = {
  getAll: () => api.get('/products'),
  getById: (id) => api.get(`/products/${id}`),
  getByCategory: (categoryId) => api.get(`/products/category/${categoryId}`),
};

export const categoryService = {
  getAll: () => api.get('/categories'),
  getTree: () => api.get('/categories/tree'),
  getSubcategoryIds: (id) => api.get(`/categories/${id}/subcategory-ids`),
};
