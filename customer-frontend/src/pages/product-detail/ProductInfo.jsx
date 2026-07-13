import { Typography, Tag, Button, InputNumber, Space, Divider, Table } from 'antd';
import { ShoppingCartOutlined, CloseCircleFilled } from '@ant-design/icons';

const { Title, Text } = Typography;

const fmt = (n) => n?.toLocaleString('vi-VN') + '₫';

export default function ProductInfo({ product, quantity, onQuantityChange, onAddToCart, features }) {
  const available = product.availableQuantity;
  const hasStock = available !== null && available !== undefined;
  const inStock = hasStock && available > 0;
  const maxQty = hasStock ? Math.min(available, 99) : 99;
  const isOutOfStock = hasStock && available <= 0;
  const onSale = product.salePrice > 0;
  const currentPrice = onSale ? product.salePrice : product.price;

  const specData = [
    { label: 'RAM', value: product.ram },
    { label: 'Bộ nhớ trong', value: product.storage },
    { label: 'Độ phân giải', value: product.screenResolution },
    { label: 'Công nghệ MH', value: product.screenTechnology },
    { label: 'Pin', value: product.battery },
    { label: 'Màu sắc', value: product.color },
    { label: 'Danh mục', value: product.categoryName },
  ].filter(s => s.value);

  const specColumns = [
    { dataIndex: 'label', width: 140, style: { fontWeight: 600, color: 'var(--gray-600)' } },
    { dataIndex: 'value', style: { color: 'var(--gray-800)' } },
  ];

  return (
    <div>
      <Tag color="blue" style={{ borderRadius: 100, padding: '2px 14px', fontWeight: 600, fontSize: 11, textTransform: 'uppercase', letterSpacing: '0.5px', marginBottom: 12 }}>
        Chính hãng
      </Tag>

      <Title level={2} style={{ fontWeight: 700, margin: '0 0 8px', fontSize: 28 }}>
        {product.name}
      </Title>

      <div style={{
        background: 'linear-gradient(135deg, rgba(236,72,153,0.08), rgba(79,70,229,0.08))',
        borderRadius: 12, padding: '20px 24px', marginBottom: 20,
      }}>
        <div style={{ display: 'flex', alignItems: 'baseline', gap: 12 }}>
          <Title level={3} style={{ color: 'var(--primary)', margin: 0, fontSize: 32, fontWeight: 700 }}>
            {fmt(currentPrice)}
          </Title>
          {onSale && (
            <>
              <Text delete style={{ color: 'var(--gray-400)', fontSize: 16 }}>{fmt(product.price)}</Text>
              <Tag color="red" style={{ borderRadius: 100, fontWeight: 600 }}>
                -{Math.round((1 - product.salePrice / product.price) * 100)}%
              </Tag>
            </>
          )}
        </div>
      </div>

      {specData.length > 0 && (
        <div style={{ marginBottom: 20 }}>
          <Table
            dataSource={specData}
            columns={specColumns}
            pagination={false}
            showHeader={false}
            bordered
            size="small"
            style={{ fontSize: 14 }}
          />
        </div>
      )}

      {product.description && (
        <div style={{ marginBottom: 16 }}>
          <Text style={{ color: 'var(--gray-600)', lineHeight: 1.7 }}>
            {product.description.replace(/<[^>]*>/g, '').substring(0, 200)}
          </Text>
        </div>
      )}

      <Divider style={{ margin: '16px 0' }} />

      <div style={{ marginBottom: 20 }}>
        <Text strong style={{ display: 'block', marginBottom: 8, fontSize: 14 }}>Số lượng</Text>
        <Space>
          <InputNumber
            min={1} max={maxQty}
            value={quantity > maxQty ? 1 : quantity}
            onChange={onQuantityChange}
            size="large" style={{ width: 80 }}
            disabled={!inStock}
          />
          {hasStock ? (
            available > 10 ? (
              <Text style={{ color: 'var(--success)', fontSize: 13 }}>{available} sản phẩm có sẵn</Text>
            ) : available > 0 ? (
              <Text style={{ color: 'var(--warning)', fontSize: 13 }}>Chỉ còn {available} sản phẩm</Text>
            ) : (
              <Text style={{ color: 'var(--error)', fontSize: 13 }}><CloseCircleFilled /> Hết hàng</Text>
            )
          ) : (
            <Text style={{ color: 'var(--gray-400)', fontSize: 13 }}>Đang cập nhật tồn kho</Text>
          )}
        </Space>
      </div>

      <Button
        type="primary" size="large" block
        icon={<ShoppingCartOutlined />}
        onClick={onAddToCart}
        disabled={isOutOfStock}
        style={{ height: 52, fontSize: 16, fontWeight: 600, borderRadius: 12, marginBottom: 20 }}
      >
        {isOutOfStock ? 'Hết hàng' : 'Thêm vào giỏ hàng'}
      </Button>

      {features?.length > 0 && (
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          {features.map((f, i) => (
            <div key={i} style={{
              display: 'flex', alignItems: 'center', gap: 8,
              padding: '8px 12px', background: 'var(--gray-50)', borderRadius: 8,
              color: 'var(--gray-600)', fontSize: 13,
            }}>
              <span style={{ color: 'var(--success)', fontSize: 14 }}>{f.icon}</span>
              <span>{f.text}</span>
            </div>
          ))}
        </Space>
      )}
    </div>
  );
}
