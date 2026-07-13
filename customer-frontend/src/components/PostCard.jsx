import { Card, Typography, Tag } from 'antd';
import { ClockCircleOutlined, ArrowRightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/600x400?text=No+Image';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
  return `/uploads/${v}`;
};

export default function PostCard({ post, delay = 0 }) {
  const navigate = useNavigate();

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
          <div style={{ height: 200, overflow: 'hidden' }}>
            <img
              alt={post.title}
              src={getImageSrc(post.img)}
              style={{
                height: '100%',
                width: '100%',
                objectFit: 'cover',
                transition: 'transform 0.5s cubic-bezier(0.4, 0, 0.2, 1)',
              }}
            />
          </div>
        }
        onClick={() => navigate(`/blog/${post.id}`)}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 8 }}>
          <Tag
            color="purple"
            style={{
              borderRadius: 100,
              padding: '2px 10px',
              fontSize: 11,
              fontWeight: 600,
              margin: 0,
            }}
          >
            {post.categoryName || 'Tin công nghệ'}
          </Tag>
          <Text style={{ fontSize: 12, color: 'var(--gray-400)' }}>
            <ClockCircleOutlined /> {post.createdAt ? new Date(post.createdAt).toLocaleDateString('vi-VN') : 'Hôm nay'}
          </Text>
        </div>
        <Title level={5} style={{ margin: '0 0 8px', fontSize: 16, lineHeight: 1.4 }}>
          {post.title}
        </Title>
        <Paragraph
          ellipsis={{ rows: 2 }}
          style={{ color: 'var(--gray-500)', fontSize: 13, margin: 0, lineHeight: 1.6 }}
        >
          {post.content?.replace(/<[^>]*>/g, '').substring(0, 120)}
        </Paragraph>
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 4,
            marginTop: 12,
            color: 'var(--primary)',
            fontSize: 13,
            fontWeight: 600,
          }}
        >
          Đọc thêm <ArrowRightOutlined style={{ fontSize: 12 }} />
        </div>
      </Card>
    </div>
  );
}
