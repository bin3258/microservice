import { Table, InputNumber, Button, Typography, Space, Empty } from 'antd';
import { DeleteOutlined, ShoppingOutlined, ArrowRightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Text, Title } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/80x80';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/') || v.startsWith('uploads/')) return v;
  return `/uploads/${v}`;
};

export default function CartTable({ items, onUpdateQuantity, onRemoveItem, total }) {
  const navigate = useNavigate();

  if (!items?.length) {
    return <Empty description="Giỏ hàng trống" />;
  }

  const columns = [
    {
      title: 'Sản phẩm',
      dataIndex: 'productName',
      key: 'product',
      render: (name, record) => (
        <div
          style={{ display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer' }}
          onClick={() => navigate(`/product/${record.productId || record.product?.id}`)}
        >
          <img
            src={getImageSrc(record.productImg || record.product?.img)}
            alt={name}
            style={{
              width: 64,
              height: 64,
              borderRadius: 8,
              objectFit: 'cover',
              border: '1px solid var(--gray-100)',
            }}
          />
          <Text strong style={{ fontSize: 14 }}>
            {name || record.product?.name}
          </Text>
        </div>
      ),
    },
    {
      title: 'Đơn giá',
      dataIndex: 'unitPrice',
      key: 'price',
      width: 120,
      align: 'center',
      render: (price, record) => (
        <Text style={{ color: 'var(--primary)', fontWeight: 600, fontSize: 15 }}>
          {(price || record.product?.price || 0).toLocaleString('vi-VN')}₫
        </Text>
      ),
    },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      key: 'qty',
      width: 140,
      align: 'center',
      render: (qty, record) => (
        <InputNumber
          min={1}
          max={99}
          value={qty}
          onChange={(v) => onUpdateQuantity(record.id, v)}
          style={{ width: 70 }}
        />
      ),
    },
    {
      title: 'Thành tiền',
      key: 'subtotal',
      width: 130,
      align: 'center',
      render: (_, record) => (
        <Text strong style={{ fontSize: 15 }}>
          {((record.unitPrice || record.product?.price || 0) * (record.quantity || 1)).toLocaleString('vi-VN')}₫
        </Text>
      ),
    },
    {
      title: '',
      key: 'action',
      width: 60,
      align: 'center',
      render: (_, record) => (
        <Button
          type="text"
          danger
          icon={<DeleteOutlined />}
          onClick={() => onRemoveItem(record.id)}
        />
      ),
    },
  ];

  return (
    <div>
      <div
        style={{
          background: '#fff',
          borderRadius: 12,
          border: '1px solid var(--gray-100)',
          overflow: 'hidden',
        }}
      >
        <Table
          columns={columns}
          dataSource={items}
          rowKey={(r) => r.id || r.productId || r.product?.id || Math.random()}
          pagination={false}
          style={{ background: '#fff' }}
        />
      </div>

      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          flexWrap: 'wrap',
          gap: 16,
          marginTop: 24,
          padding: '24px',
          background: '#fff',
          borderRadius: 12,
          border: '1px solid var(--gray-100)',
        }}
      >
        <Space>
          <Button onClick={() => navigate('/shop')} icon={<ShoppingOutlined />}>
            Tiếp tục mua sắm
          </Button>
        </Space>
        <Space size={24} align="center">
          <div style={{ textAlign: 'right' }}>
            <Text style={{ color: 'var(--gray-500)', display: 'block', marginBottom: 4 }}>Tổng cộng:</Text>
            <Title level={3} style={{ color: 'var(--primary)', margin: 0, fontSize: 28 }}>
              {total.toLocaleString('vi-VN')}₫
            </Title>
          </div>
          <Button
            type="primary"
            size="large"
            icon={<ArrowRightOutlined />}
            iconPosition="end"
            onClick={() => navigate('/checkout')}
            style={{ height: 48, padding: '0 32px', fontSize: 16, fontWeight: 600, borderRadius: 10 }}
          >
            Thanh toán
          </Button>
        </Space>
      </div>
    </div>
  );
}
