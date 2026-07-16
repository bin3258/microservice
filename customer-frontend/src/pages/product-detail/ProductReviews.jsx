import { useState, useEffect } from 'react';
import { Typography, Rate, Divider, Empty, Spin, Tag, Image, Space } from 'antd';
import { reviewService } from '../../services/orderService';

const { Title, Text } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/500x500?text=No+Image';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
  return `/uploads/${v}`;
};

export default function ProductReviews({ productId }) {
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!productId) return;
    setLoading(true);
    Promise.all([
      reviewService.getByProduct(productId),
      reviewService.getProductStats(productId),
    ])
      .then(([revRes, statRes]) => {
        setReviews(revRes.data || []);
        setStats(statRes.data);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [productId]);

  if (loading) return <div style={{ textAlign: 'center', padding: 40 }}><Spin /></div>;

  return (
    <div>
      <Divider />
      <Title level={4} style={{ fontWeight: 600, marginBottom: 16 }}>
        Đánh giá từ khách hàng
      </Title>

      {stats && stats.totalReviews > 0 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 24, marginBottom: 24, padding: 16, background: '#fafafa', borderRadius: 12 }}>
          <div style={{ textAlign: 'center' }}>
            <div style={{ fontSize: 36, fontWeight: 700, color: '#fadb14' }}>{stats.averageRating}</div>
            <Rate disabled value={Math.round(stats.averageRating)} style={{ fontSize: 14 }} />
            <div style={{ fontSize: 12, color: '#888', marginTop: 4 }}>{stats.totalReviews} đánh giá</div>
          </div>
          <div style={{ flex: 1 }}>
            {[5, 4, 3, 2, 1].map((star) => {
              const count = stats.distribution?.[star] || 0;
              const pct = stats.totalReviews > 0 ? (count / stats.totalReviews) * 100 : 0;
              return (
                <div key={star} style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <Text style={{ fontSize: 12, width: 30 }}>{star} sao</Text>
                  <div style={{ flex: 1, height: 8, background: '#eee', borderRadius: 4, overflow: 'hidden' }}>
                    <div style={{ width: `${pct}%`, height: '100%', background: '#fadb14', borderRadius: 4 }} />
                  </div>
                  <Text style={{ fontSize: 12, color: '#888', width: 30 }}>{count}</Text>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {reviews.length === 0 ? (
        <Empty description="Chưa có đánh giá nào cho sản phẩm này" />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {reviews.map((review) => (
            <div key={review.id} style={{ padding: 16, background: '#fafafa', borderRadius: 12 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                <Space>
                  <Text strong>{review.name}</Text>
                  <Rate disabled value={review.rating} style={{ fontSize: 12 }} />
                </Space>
                <Text style={{ fontSize: 12, color: '#888' }}>
                  {review.createdAt ? new Date(review.createdAt).toLocaleDateString('vi-VN') : ''}
                </Text>
              </div>
              {review.description && (
                <Text style={{ display: 'block', marginBottom: 8 }}>{review.description}</Text>
              )}
              {review.images && review.images.length > 0 && (
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  {review.images.map((img, idx) => (
                    <Image key={idx} src={getImageSrc(img)} style={{ width: 80, height: 80, objectFit: 'cover', borderRadius: 8 }} />
                  ))}
                </div>
              )}
              {review.adminReply && (
                <div style={{ marginTop: 8, padding: '8px 12px', background: '#e6f7ff', borderRadius: 8, fontSize: 13 }}>
                  <Text style={{ color: '#1890ff', fontWeight: 600 }}>Phản hồi từ shop: </Text>
                  <Text style={{ fontStyle: 'italic' }}>{review.adminReply}</Text>
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
