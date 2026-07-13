import { useState } from 'react';
import api from '../api/axiosClient';
import { AuthContext } from './AuthContext';

const stored = localStorage.getItem('user');
const token = localStorage.getItem('token');

export function AuthProvider({ children }) {
  const [user, setUser] = useState(stored && token ? JSON.parse(stored) : null);
  const [loading, setLoading] = useState(false);

  const login = async (username, password) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/login', { username, password });
      const data = res.data;
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data));
      setUser(data);
      return data;
    } finally {
      setLoading(false);
    }
  };

  const register = async (data) => {
    setLoading(true);
    try {
      const res = await api.post('/auth/register', data);
      const result = res.data;
      localStorage.setItem('token', result.token);
      localStorage.setItem('user', JSON.stringify(result));
      setUser(result);
      return result;
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    setLoading(true);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
    setLoading(false);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
