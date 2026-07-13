import { Row, Col, Spin, Empty } from 'antd';
import ProductCard from '../../components/ProductCard';

export default function ProductList({ products, loading, onAddToCart }) {
  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '80px 0' }}>
        <Spin size="large" />
        <p style={{ marginTop: 16, color: 'var(--gray-400)' }}>Đang tải sản phẩm...</p>
      </div>
    );
  }

  if (!products?.length) {
    return (
      <div style={{ padding: '60px 0' }}>
        <Empty description="Không tìm thấy sản phẩm phù hợp" />
      </div>
    );
  }

  return (
    <Row gutter={[20, 20]}>
      {products.map((p, index) => (
        <Col key={p.id} xs={12} sm={12} md={8} lg={8}>
          <ProductCard product={p} onAddToCart={onAddToCart} delay={index * 0.05} />
        </Col>
      ))}
    </Row>
  );
}
