import { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { Row, Col, Typography, Spin, Divider, message } from 'antd';
import { CheckCircleFilled, SafetyCertificateOutlined, TruckOutlined, SwapOutlined } from '@ant-design/icons';
import ProductInfo from './ProductInfo';
import ProductReviews from './ProductReviews';
import ProductCard from '../../components/ProductCard';
import { productService } from '../../services/productService';
import { cartService } from '../../services/orderService';
import { useAuth } from '../../context/useAuth';
import { useCart } from '../../context/useCart';

const { Title } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/500x500?text=No+Image';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
  return `/uploads/${v}`;
};

export default function ProductDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const { refreshCartCount } = useCart();
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [relatedProducts, setRelatedProducts] = useState([]);
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    let ignore = false;
    productService.getById(id)
      .then((res) => { if (!ignore) setProduct(res.data); })
      .catch(() => { if (!ignore) message.error('Không tìm thấy sản phẩm'); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [id]);

  useEffect(() => {
    if (!product?.categoryId) return;
    let ignore = false;
    productService.getByCategory(product.categoryId)
      .then((res) => {
        if (!ignore) {
          const list = (Array.isArray(res.data) ? res.data : []).filter(p => p.id !== product.id);
          setRelatedProducts(list);
        }
      })
      .catch(() => {});
    return () => { ignore = true; };
  }, [product?.categoryId, product?.id]);

  const addToCart = async () => {
    if (!user) { message.info('Vui lòng đăng nhập để mua hàng'); return; }
    try {
      const unitPrice = product.salePrice > 0 ? product.salePrice : product.price;
      await cartService.addItem(user.userId, {
        productId: product.id,
        productName: product.name,
        productImg: product.img,
        quantity,
        unitPrice,
      });
      message.success('Đã thêm vào giỏ hàng');
      refreshCartCount();
    } catch (err) {
      message.error(err.response?.data?.message || 'Lỗi khi thêm vào giỏ hàng');
    }
  };

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!product) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0', color: 'var(--gray-400)' }}>
        Không tìm thấy sản phẩm.
      </div>
    );
  }

  const features = [
    { icon: <CheckCircleFilled />, text: 'Chính hãng 100%' },
    { icon: <SafetyCertificateOutlined />, text: 'Bảo hành 12 tháng' },
    { icon: <TruckOutlined />, text: 'Miễn phí giao hàng' },
    { icon: <SwapOutlined />, text: 'Đổi trả 30 ngày' },
  ];

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 20px' }}>
      <Row gutter={[40, 32]}>
        <Col xs={24} md={12}>
          <div
            style={{
              background: '#fff',
              borderRadius: 16,
              padding: 40,
              border: '1px solid var(--gray-100)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              minHeight: 400,
            }}
          >
            <img
              alt={product.name}
              src={getImageSrc(product.img)}
              style={{
                maxHeight: 380,
                maxWidth: '100%',
                objectFit: 'contain',
              }}
            />
          </div>
        </Col>
        <Col xs={24} md={12}>
          <ProductInfo
            product={product}
            quantity={quantity}
            onQuantityChange={setQuantity}
            onAddToCart={addToCart}
            features={features}
          />
        </Col>
      </Row>

        <div
          style={{
            marginTop: 40,
            background: '#fff',
            borderRadius: 16,
            padding: 32,
            border: '1px solid var(--gray-100)',
          }}
        >
          <Title level={4} style={{ fontWeight: 600, marginBottom: 16 }}>
            Mô tả sản phẩm
          </Title>
          <Divider style={{ margin: '0 0 16px' }} />
          <div
            style={{ color: 'var(--gray-600)', lineHeight: 1.8, fontSize: 15 }}
            dangerouslySetInnerHTML={{ __html: product.description || 'Chưa có mô tả cho sản phẩm này.' }}
          />
        </div>

        <div
          style={{
            marginTop: 40,
            background: '#fff',
            borderRadius: 16,
            padding: 32,
            border: '1px solid var(--gray-100)',
          }}
        >
          <ProductReviews productId={product.id} />
        </div>

        {relatedProducts.length > 0 && (
          <div style={{ marginTop: 48 }}>
            <Title level={4} style={{ fontWeight: 600, marginBottom: 24 }}>
              Sản phẩm liên quan
            </Title>
            <Row gutter={[16, 24]}>
              {relatedProducts.map((p, i) => (
                <Col xs={12} sm={8} md={6} lg={6} key={p.id}>
                  <ProductCard
                    product={p}
                    delay={i * 0.05}
                    onAddToCart={async (prod) => {
                      if (!user) { message.info('Vui lòng đăng nhập để mua hàng'); return; }
                      try {
                        const unitPrice = prod.salePrice > 0 ? prod.salePrice : prod.price;
                        await cartService.addItem(user.userId, {
                          productId: prod.id,
                          productName: prod.name,
                          productImg: prod.img,
                          quantity: 1,
                          unitPrice,
                        });
                        message.success('Đã thêm vào giỏ hàng');
                        refreshCartCount();
                      } catch (err) {
                        message.error(err.response?.data?.message || 'Lỗi khi thêm vào giỏ hàng');
                      }
                    }}
                  />
                </Col>
              ))}
            </Row>
          </div>
        )}
      </div>
    );
  }
