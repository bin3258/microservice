import api from '../api/axiosClient';

export const blogService = {
  getAll: () => api.get('/posts'),
  getById: (id) => api.get(`/posts/${id}`),
  getCategories: () => api.get('/post-categories'),
};
