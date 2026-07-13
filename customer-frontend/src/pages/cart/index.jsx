import { useState, useEffect } from 'react';
import { Typography, Spin, Empty, Button, message } from 'antd';
import { ShoppingOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import CartTable from './CartTable';
import { cartService } from '../../services/orderService';
import { useAuth } from '../../context/useAuth';
import { useCart } from '../../context/useCart';

const { Title } = Typography;

export default function CartPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { refreshCartCount } = useCart();
  const [cartItems, setCartItems] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) return;
    let ignore = false;
    cartService.getCart(user.userId)
      .then((res) => { if (!ignore) setCartItems(res.data?.items || res.data || []); })
      .catch(() => { if (!ignore) setCartItems([]); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [user]);

  const updateQuantity = async (itemId, newQty) => {
    if (newQty < 1) return;
    try {
      await cartService.updateItem(itemId, { quantity: newQty });
      setCartItems((prev) =>
        prev.map((item) =>
          item.id === itemId ? { ...item, quantity: newQty } : item,
        ),
      );
      refreshCartCount();
    } catch {
      message.error('Lỗi cập nhật số lượng');
    }
  };

  const removeItem = async (itemId) => {
    try {
      await cartService.removeItem(itemId);
      setCartItems((prev) => prev.filter((item) => item.id !== itemId));
      refreshCartCount();
      message.success('Đã xóa sản phẩm');
    } catch {
      message.error('Lỗi xóa sản phẩm');
    }
  };

  const total = cartItems.reduce(
    (sum, item) => sum + (item.unitPrice || item.product?.price || 0) * (item.quantity || 1),
    0,
  );

  if (!user) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Title level={3}>Vui lòng đăng nhập</Title>
        <p style={{ color: 'var(--gray-500)', marginBottom: 20 }}>Đăng nhập để xem giỏ hàng của bạn</p>
        <Button type="primary" size="large" onClick={() => navigate('/login')}>
          Đăng nhập
        </Button>
      </div>
    );
  }

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!cartItems.length) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Empty description="Giỏ hàng trống" />
        <Button
          type="primary"
          size="large"
          icon={<ShoppingOutlined />}
          onClick={() => navigate('/shop')}
          style={{ marginTop: 16 }}
        >
          Mua sắm ngay
        </Button>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: '32px 20px' }}>
      <Title level={2} style={{ fontWeight: 700, marginBottom: 24, fontSize: 24 }}>
        Giỏ hàng ({cartItems.length} sản phẩm)
      </Title>

      <CartTable
        items={cartItems}
        onUpdateQuantity={updateQuantity}
        onRemoveItem={removeItem}
        total={total}
      />
    </div>
  );
}
