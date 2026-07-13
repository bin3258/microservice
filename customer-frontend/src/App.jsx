import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ConfigProvider, Layout } from 'antd';
import { AuthProvider } from './context/AuthProvider';
import { CartProvider } from './context/CartProvider';
import Header from './components/Header';
import Footer from './components/Footer';

import HomePage from './pages/home/index';
import ShopPage from './pages/shop/index';
import ProductDetailPage from './pages/product-detail/index';
import BlogPage from './pages/blog/index';
import BlogDetailPage from './pages/blog/BlogDetail';
import CartPage from './pages/cart/index';
import ContactPage from './pages/contact/index';
import CheckoutPage from './pages/checkout/index';
import LoginPage from './pages/Login';
import ProfilePage from './pages/profile';
import OrdersPage from './pages/orders';
import OrderDetailPage from './pages/orders/OrderDetail';

const { Content } = Layout;

function AppLayout() {
  return (
    <Layout style={{ minHeight: '100vh', background: 'var(--gray-50)' }}>
      <Header />
      <Content>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/category/:categoryId" element={<HomePage />} />
          <Route path="/shop" element={<ShopPage />} />
          <Route path="/product/:id" element={<ProductDetailPage />} />
          <Route path="/blog" element={<BlogPage />} />
          <Route path="/blog/:id" element={<BlogDetailPage />} />
          <Route path="/cart" element={<CartPage />} />
          <Route path="/contact" element={<ContactPage />} />
          <Route path="/checkout" element={<CheckoutPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/orders" element={<OrdersPage />} />
          <Route path="/orders/:id" element={<OrderDetailPage />} />
        </Routes>
      </Content>
      <Footer />
    </Layout>
  );
}

export default function App() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: '#4f46e5',
          borderRadius: 8,
          fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
        },
      }}
    >
      <BrowserRouter>
          <AuthProvider>
            <CartProvider>
              <AppLayout />
            </CartProvider>
          </AuthProvider>
      </BrowserRouter>
    </ConfigProvider>
  );
}
