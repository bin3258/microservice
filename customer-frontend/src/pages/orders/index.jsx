import { useState, useEffect } from 'react';
import { Typography, Card, Row, Col, Button, Spin, Empty, message, Tag, Space, Divider } from 'antd';
import { ShoppingCartOutlined, RightOutlined, InboxOutlined, CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined, LoadingOutlined, EnvironmentOutlined, FileTextOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { orderService } from '../../services/orderService';

const { Title, Text } = Typography;

const STATUS_MAP = {
  PENDING: { color: 'default', icon: <ClockCircleOutlined />, text: 'Chờ xác nhận' },
  CONFIRMED: { color: 'blue', icon: <CheckCircleOutlined />, text: 'Đã xác nhận' },
  SHIPPING: { color: 'cyan', icon: <LoadingOutlined />, text: 'Đang giao hàng' },
  DELIVERED: { color: 'success', icon: <CheckCircleOutlined />, text: 'Đã giao hàng' },
  CANCELLED: { color: 'error', icon: <CloseCircleOutlined />, text: 'Đã hủy' },
  PROCESSING: { color: 'processing', icon: <LoadingOutlined />, text: 'Đang xử lý' },
  PAID: { color: 'blue', icon: <CheckCircleOutlined />, text: 'Đã thanh toán' },
  COMPLETED: { color: 'success', icon: <CheckCircleOutlined />, text: 'Hoàn thành' },
  FAILED: { color: 'error', icon: <CloseCircleOutlined />, text: 'Thất bại' },
};

export default function OrdersPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    let ignore = false;
    (async () => {
      try {
        const res = await orderService.getByUserId(user.userId);
        if (ignore) return;
        setOrders(res.data);
      } catch {
        message.error('Không thể tải danh sách đơn hàng');
      } finally {
        if (!ignore) setLoading(false);
      }
    })();
    return () => { ignore = true; };
  }, [user, navigate]);

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 20px' }}>
      <Title level={2} style={{ fontWeight: 700, marginBottom: 24, fontSize: 24 }}>
        <ShoppingCartOutlined /> Đơn hàng của tôi
      </Title>

      {orders.length === 0 ? (
        <Card style={{ borderRadius: 12, textAlign: 'center', padding: 40 }}>
          <Empty
            image={<InboxOutlined style={{ fontSize: 64, color: 'var(--gray-300)' }} />}
            description="Bạn chưa có đơn hàng nào"
          >
            <Button type="primary" onClick={() => navigate('/shop')}>
              Mua sắm ngay
            </Button>
          </Empty>
        </Card>
      ) : (
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          {orders.map((order) => {
            const s = STATUS_MAP[order.status] || { color: 'default', icon: null, text: order.status };
            return (
              <Card
                key={order.orderId}
                style={{ borderRadius: 12, border: '1px solid var(--gray-100)' }}
                hoverable
                onClick={() => navigate(`/orders/${order.orderId}`)}
              >
                <Row gutter={[16, 12]} align="middle">
                  <Col xs={24} sm={8}>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Mã đơn hàng</Text>
                    <div>
                      <Text strong style={{ fontSize: 16 }}>#{order.orderId}</Text>
                    </div>
                  </Col>
                  <Col xs={12} sm={5}>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Số lượng</Text>
                    <div>
                      <Text strong>{order.totalQuantity} sản phẩm</Text>
                    </div>
                  </Col>
                  <Col xs={12} sm={5}>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Tổng tiền</Text>
                    <div>
                      <Text strong style={{ color: 'var(--primary)', fontSize: 16 }}>
                        {order.totalPrice?.toLocaleString('vi-VN')}₫
                      </Text>
                    </div>
                  </Col>
                  <Col xs={12} sm={4}>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Trạng thái</Text>
                    <div>
                      <Tag color={s.color} style={{ borderRadius: 4 }}>{s.icon} {s.text}</Tag>
                    </div>
                  </Col>
                  <Col xs={12} sm={2} style={{ textAlign: 'right' }}>
                    <Button type="text" icon={<RightOutlined />} />
                  </Col>
                </Row>
                {(order.shippingAddress || order.note) && <Divider style={{ margin: '12px 0' }} />}
                {order.shippingAddress && (
                  <div style={{ marginBottom: order.note ? 8 : 0 }}>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>
                      <EnvironmentOutlined /> Địa chỉ giao hàng
                    </Text>
                    <div><Text>{order.shippingAddress}</Text></div>
                  </div>
                )}
                {order.note && (
                  <div>
                    <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>
                      <FileTextOutlined /> Ghi chú
                    </Text>
                    <div><Text style={{ fontStyle: 'italic' }}>{order.note}</Text></div>
                  </div>
                )}
              </Card>
            );
          })}
        </Space>
      )}
    </div>
  );
}
