import { useState } from 'react';
import { authAPI } from '../api/axios';
import { AuthContext } from './authContext';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('user');
    const token = localStorage.getItem('token');
    return stored && token ? JSON.parse(stored) : null;
  });
  const [loading] = useState(false);

  const login = async (username, password) => {
    const res = await authAPI.login({ username, password });
    const { token, username: uname, email, role, userId } = res.data;
    const userData = { username: uname, email, role, userId };
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
    return userData;
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}


