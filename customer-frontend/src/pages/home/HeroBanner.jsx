import { useState, useEffect } from 'react';
import { Button, Typography, Tag, Carousel, Spin } from 'antd';
import { ShoppingOutlined, ThunderboltOutlined, RightCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import api from '../../api/axiosClient';

const { Title, Paragraph } = Typography;

export default function HeroBanner() {
  const navigate = useNavigate();
  const [banners, setBanners] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    api.get('/banners')
      .then((res) => { if (!ignore) setBanners(res.data); })
      .catch(() => {})
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, []);

  return (
    <div
      style={{
        position: 'relative',
        background: 'linear-gradient(135deg, #1e1b4b 0%, #312e81 30%, #4f46e5 60%, #6366f1 100%)',
        overflow: 'hidden',
        minHeight: 480,
        display: 'flex',
      }}
    >
      <div
        style={{
          position: 'absolute',
          top: '-50%',
          right: '-10%',
          width: 600,
          height: 600,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(236,72,153,0.15) 0%, transparent 70%)',
          pointerEvents: 'none',
        }}
      />
      <div
        style={{
          position: 'absolute',
          bottom: '-30%',
          left: '-5%',
          width: 400,
          height: 400,
          borderRadius: '50%',
          background: 'radial-gradient(circle, rgba(99,102,241,0.2) 0%, transparent 70%)',
          pointerEvents: 'none',
        }}
      />

      <div style={{
        flex: '0 0 40%', maxWidth: '40%',
        color: '#fff', padding: '80px 40px 80px 60px',
        position: 'relative', zIndex: 1,
        display: 'flex', flexDirection: 'column', justifyContent: 'center',
      }}>
        <Tag
          color="magenta"
          style={{
            borderRadius: 100, padding: '4px 16px', fontSize: 12, fontWeight: 600,
            marginBottom: 16, border: 'none',
            background: 'rgba(236,72,153,0.25)', color: '#f9a8d4', alignSelf: 'flex-start',
          }}
          icon={<ThunderboltOutlined />}
        >
          GIẢM GIÁ SỐC MÙA HÈ
        </Tag>

        <Title style={{ color: '#fff', fontSize: 44, margin: 0, lineHeight: 1.1, fontWeight: 800, letterSpacing: '-1px' }}>
          Điện thoại{' '}
          <span style={{ background: 'linear-gradient(135deg, #f472b6, #fb923c)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
            chính hãng
          </span>
        </Title>

        <Title level={2} style={{ color: 'rgba(255,255,255,0.8)', margin: '10px 0 0', fontWeight: 400, fontSize: 18 }}>
          Giá tốt nhất thị trường - Bảo hành 12 tháng
        </Title>

        <Paragraph style={{ color: 'rgba(255,255,255,0.6)', fontSize: 14, marginTop: 12, lineHeight: 1.7, maxWidth: 380 }}>
          iPhone, Samsung, Xiaomi chính hãng — Miễn phí giao hàng toàn quốc — Đổi trả trong 30 ngày
        </Paragraph>

        <div style={{ display: 'flex', gap: 10, marginTop: 20 }}>
          <Button type="primary" size="large" icon={<ShoppingOutlined />} onClick={() => navigate('/shop')} style={{ height: 48, padding: '0 30px', fontSize: 15, fontWeight: 600, borderRadius: 12, background: 'linear-gradient(135deg, #ec4899, #f472b6)', border: 'none', boxShadow: '0 8px 24px rgba(236,72,153,0.35)' }}>
            Mua ngay
          </Button>
          <Button size="large" icon={<RightCircleOutlined />} onClick={() => document.getElementById('products')?.scrollIntoView({ behavior: 'smooth' })} style={{ height: 48, padding: '0 24px', fontSize: 14, fontWeight: 600, borderRadius: 12, background: 'rgba(255,255,255,0.1)', border: '1px solid rgba(255,255,255,0.2)', color: '#fff' }}>
            Khám phá
          </Button>
        </div>
      </div>

      <div style={{ flex: '0 0 60%', maxWidth: '60%', position: 'relative', zIndex: 1, padding: '40px 20px' }}>
        {loading ? (
          <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Spin size="large" />
          </div>
        ) : banners.length > 0 ? (
          <Carousel autoplay autoplaySpeed={4000} style={{ borderRadius: 16, overflow: 'hidden', height: '100%' }}>
            {banners.map((banner) => (
              <div key={banner.id} style={{ height: '100%' }}>
                <div style={{ height: 400, borderRadius: 16, overflow: 'hidden' }}>
                  {banner.image ? (
                    <img src={banner.image} alt={banner.title || ''} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                  ) : (
                    <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(255,255,255,0.1)', color: '#fff', fontSize: 20 }}>
                      {banner.title || 'Banner'}
                    </div>
                  )}
                </div>
              </div>
            ))}
          </Carousel>
        ) : (
          <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <img src="https://pngimg.com/uploads/iphone_15/iphone_15_PNG56.png" alt="iPhone 15" style={{ height: 320, filter: 'drop-shadow(0 20px 60px rgba(0,0,0,0.4))', animation: 'float 3s ease-in-out infinite' }} />
          </div>
        )}
      </div>

      <style>{`
        @keyframes float {
          0%, 100% { transform: translateY(0); }
          50% { transform: translateY(-12px); }
        }
      `}</style>
    </div>
  );
}
