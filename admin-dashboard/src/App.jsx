import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ConfigProvider, Spin, Result, Button } from 'antd';
import { AuthProvider } from './context/AuthProvider';
import { useAuth } from './context/useAuth';
import AdminLayout from './components/AdminLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Products from './pages/Products';
import Trash from './pages/Trash';
import Categories from './pages/Categories';
import PostCategories from './pages/PostCategories';
import Orders from './pages/Orders';
import OrderDetail from './pages/OrderDetail';
import Banners from './pages/Banners';
import Posts from './pages/Posts';
import Users from './pages/Users';
import Customers from './pages/Customers';
import Inventory from './pages/Inventory';
import Warehouses from './pages/Warehouses';

function ProtectedRoute({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  return user ? children : <Navigate to="/login" replace />;
}

function RoleGuard({ roles, children }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (!roles.includes(user.role)) {
    return (
      <Result
        status="403"
        title="Không có quyền truy cập"
        subTitle="Chỉ quản lý mới có quyền truy cập trang này."
        extra={<Button type="primary" onClick={() => window.history.back()}>Quay lại</Button>}
      />
    );
  }
  return children;
}

function AppRoutes() {
  const { user, loading } = useAuth();
  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/" element={<ProtectedRoute><AdminLayout /></ProtectedRoute>}>
        <Route index element={<Dashboard />} />
        <Route path="products" element={<Products />} />
        <Route path="products/trash" element={<Trash />} />
        <Route path="categories" element={<Categories />} />
        <Route path="post-categories" element={<PostCategories />} />
        <Route path="banners" element={<Banners />} />
        <Route path="orders" element={<RoleGuard roles={['ADMIN', 'MANAGER']}><Orders /></RoleGuard>} />
        <Route path="orders/:id" element={<RoleGuard roles={['ADMIN', 'MANAGER']}><OrderDetail /></RoleGuard>} />
        <Route path="posts" element={<Posts />} />
        <Route path="users" element={<RoleGuard roles={['MANAGER']}><Users /></RoleGuard>} />
        <Route path="customers" element={<Customers />} />
        <Route path="inventory" element={<Inventory />} />
        <Route path="warehouses" element={<Warehouses />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <ConfigProvider theme={{ token: { fontSize: 18 } }}>
      <BrowserRouter>
        <AuthProvider>
          <AppRoutes />
        </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}
