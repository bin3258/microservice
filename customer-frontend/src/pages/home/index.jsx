import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Button, Row, Col, Spin } from 'antd';
import { RightOutlined, FireOutlined, ClockCircleOutlined } from '@ant-design/icons';
import HeroBanner from './HeroBanner';
import ProductCard from '../../components/ProductCard';
import LatestBlog from './LatestBlog';
import { productService } from '../../services/productService';
import { blogService } from '../../services/blogService';
import { cartService } from '../../services/orderService';
import { useAuth } from '../../context/useAuth';
import { useCart } from '../../context/useCart';
import { message } from 'antd';

const { Title } = Typography;

export default function HomePage() {
  const navigate = useNavigate();
  const { categoryId } = useParams();
  const { user } = useAuth();
  const { refreshCartCount } = useCart();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [posts, setPosts] = useState([]);
  const [postsLoading, setPostsLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    productService.getAll().then((r) => {
      if (!ignore) setProducts(r.data);
    }).finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, []);

  useEffect(() => {
    if (!categoryId) {
      let ignore = false;
      blogService.getAll()
        .then((res) => {
          if (!ignore) setPosts(res.data.filter((p) => p.status === 'PUBLISHED').slice(0, 3));
        })
        .finally(() => { if (!ignore) setPostsLoading(false); });
      return () => { ignore = true; };
    }
  }, [categoryId]);

  const addToCart = async (product) => {
    if (!user) { message.info('Vui lòng đăng nhập để mua hàng'); return; }
    const unitPrice = product.salePrice > 0 ? product.salePrice : product.price;
    try {
      await cartService.addItem(user.userId, {
        productId: product.id,
        productName: product.name,
        productImg: product.img,
        quantity: 1,
        unitPrice,
      });
      message.success('Đã thêm vào giỏ hàng');
      refreshCartCount();
    } catch (err) {
      message.error(err.response?.data?.message || 'Lỗi khi thêm vào giỏ hàng');
    }
  };

  const newProducts = [...products].sort((a, b) => b.id - a.id).slice(0, 4);
  const hotProducts = [...products].sort((a, b) => a.id - b.id).slice(0, 4);

  if (categoryId) {
    const filtered = products.filter((p) => p.categoryId === Number(categoryId));
    return (
      <div>
        <HeroBanner />
        <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 20px' }}>
          <Title level={3} style={{ marginBottom: 24, fontWeight: 700, fontSize: 20, display: 'flex', alignItems: 'center', gap: 8 }}>
            <span style={{ width: 4, height: 20, borderRadius: 2, background: 'linear-gradient(180deg, var(--primary), var(--accent))', display: 'inline-block' }} />
            Danh mục sản phẩm
          </Title>
          <Row gutter={[20, 20]}>
            {filtered.map((p, idx) => (
              <Col key={p.id} xs={12} sm={8} md={6} lg={6}>
                <ProductCard product={p} onAddToCart={addToCart} delay={idx * 0.05} />
              </Col>
            ))}
          </Row>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div>
        <HeroBanner />
        <div style={{ textAlign: 'center', padding: '60px 0' }}><Spin size="large" /></div>
      </div>
    );
  }

  return (
    <div>
      <HeroBanner />
      <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 20px' }}>
        <div style={{ marginBottom: 40 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <Title level={3} style={{ margin: 0, fontWeight: 700, fontSize: 20, display: 'flex', alignItems: 'center', gap: 8 }}>
              <FireOutlined style={{ color: 'var(--primary)' }} />
              Sản phẩm hot
            </Title>
          </div>
          <Row gutter={[20, 20]}>
            {hotProducts.map((p, idx) => (
              <Col key={p.id} xs={12} sm={8} md={6} lg={6}>
                <ProductCard product={p} onAddToCart={addToCart} delay={idx * 0.05} />
              </Col>
            ))}
          </Row>
          <div style={{ textAlign: 'center', marginTop: 20 }}>
            <Button type="primary" icon={<RightOutlined />} onClick={() => navigate('/shop')}>
              Xem tất cả sản phẩm
            </Button>
          </div>
        </div>

        <div style={{ marginBottom: 40 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
            <Title level={3} style={{ margin: 0, fontWeight: 700, fontSize: 20, display: 'flex', alignItems: 'center', gap: 8 }}>
              <ClockCircleOutlined style={{ color: 'var(--accent)' }} />
              Sản phẩm mới
            </Title>
          </div>
          <Row gutter={[20, 20]}>
            {newProducts.map((p, idx) => (
              <Col key={p.id} xs={12} sm={8} md={6} lg={6}>
                <ProductCard product={p} onAddToCart={addToCart} delay={idx * 0.05} />
              </Col>
            ))}
          </Row>
          <div style={{ textAlign: 'center', marginTop: 20 }}>
            <Button type="primary" icon={<RightOutlined />} onClick={() => navigate('/shop')}>
              Xem tất cả sản phẩm
            </Button>
          </div>
        </div>

        <LatestBlog posts={posts} loading={postsLoading} />
      </div>
    </div>
  );
}
