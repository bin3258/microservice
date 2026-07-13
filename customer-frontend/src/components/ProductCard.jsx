import { Card, Typography, Button, Tag, Space } from 'antd';
import { ShoppingCartOutlined, EyeOutlined, CheckCircleFilled } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Text, Title } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/300x300?text=No+Image';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
  return `/uploads/${v}`;
};

const fmt = (n) => n?.toLocaleString('vi-VN') + '₫';

export default function ProductCard({ product, onAddToCart, delay = 0 }) {
  const navigate = useNavigate();
  const onSale = product.salePrice > 0;
  const currentPrice = onSale ? product.salePrice : product.price;
  const specs = [product.ram, product.storage, product.color].filter(Boolean);

  return (
    <div className="product-card" style={{ animationDelay: `${delay}s` }}>
      <Card
        hoverable
        style={{
          borderRadius: 12,
          overflow: 'hidden',
          border: '1px solid var(--gray-100)',
        }}
        cover={
          <div
            style={{
              height: 240,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: '#fafafa',
              padding: 24,
              cursor: 'pointer',
              position: 'relative',
            }}
            onClick={() => navigate(`/product/${product.id}`)}
          >
            <img
              alt={product.name}
              src={getImageSrc(product.img)}
              style={{
                height: '100%',
                maxWidth: '100%',
                objectFit: 'contain',
                transition: 'transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            />
            {product.availableQuantity != null && product.availableQuantity <= 0 && (
              <Tag
                color="red"
                style={{
                  position: 'absolute', top: 12, left: 12, borderRadius: 100,
                  padding: '2px 12px', fontWeight: 600, fontSize: 11,
                  textTransform: 'uppercase', letterSpacing: '0.5px',
                }}
              >Hết hàng</Tag>
            )}
            {product.availableQuantity != null && product.availableQuantity > 0 && product.availableQuantity <= 5 && (
              <Tag
                color="orange"
                style={{
                  position: 'absolute', top: 12, left: 12, borderRadius: 100,
                  padding: '2px 12px', fontWeight: 600, fontSize: 11,
                  textTransform: 'uppercase', letterSpacing: '0.5px',
                }}
              >Sắp hết</Tag>
            )}
            {(!product.availableQuantity || product.availableQuantity > 5) && (
              <Tag
                color="blue"
                style={{
                  position: 'absolute', top: 12, left: 12, borderRadius: 100,
                  padding: '2px 12px', fontWeight: 600, fontSize: 11,
                  textTransform: 'uppercase', letterSpacing: '0.5px',
                }}
              >New</Tag>
            )}
            {onSale && (
              <Tag
                color="red"
                style={{
                  position: 'absolute', top: 12, right: 12, borderRadius: 100,
                  padding: '2px 10px', fontWeight: 700, fontSize: 12,
                }}
              >-{Math.round((1 - product.salePrice / product.price) * 100)}%</Tag>
            )}
          </div>
        }
        actions={[
          <Button
            key="view"
            type="text"
            icon={<EyeOutlined />}
            onClick={() => navigate(`/product/${product.id}`)}
            style={{ fontSize: 16, color: 'var(--gray-400)' }}
          />,
          <Button
            key="cart"
            type="text"
            icon={<ShoppingCartOutlined />}
            onClick={() => onAddToCart?.(product)}
            style={{ fontSize: 16, color: 'var(--primary)' }}
          />,
        ]}
      >
        <div onClick={() => navigate(`/product/${product.id}`)} style={{ cursor: 'pointer' }}>
          <Text
            strong
            ellipsis
            style={{
              fontSize: 15, display: 'block', marginBottom: 4, color: 'var(--gray-800)',
            }}
          >{product.name}</Text>

          {specs.length > 0 && (
            <Space size={4} style={{ marginBottom: 6, flexWrap: 'wrap' }}>
              {specs.map((s, i) => (
                <Text key={i} style={{
                  fontSize: 11, color: 'var(--gray-500)',
                  background: 'var(--gray-50)', padding: '1px 8px', borderRadius: 4,
                }}>{s}</Text>
              ))}
            </Space>
          )}

          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <Title level={4} style={{ color: 'var(--primary)', margin: 0, fontSize: 18 }}>
              {fmt(currentPrice)}
            </Title>
            {onSale && (
              <Text delete style={{ color: 'var(--gray-400)', fontSize: 13 }}>
                {fmt(product.price)}
              </Text>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginTop: 6 }}>
            <CheckCircleFilled style={{ color: 'var(--success)', fontSize: 12 }} />
            <Text style={{ fontSize: 12, color: 'var(--gray-500)' }}>Chính hãng 100%</Text>
          </div>
        </div>
      </Card>
    </div>
  );
}
