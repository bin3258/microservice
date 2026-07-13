import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Typography, Spin, Tag, Button } from 'antd';
import { ArrowLeftOutlined, ClockCircleOutlined, UserOutlined } from '@ant-design/icons';
import { blogService } from '../../services/blogService';

const { Title } = Typography;

export default function BlogDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [post, setPost] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    blogService.getById(id)
      .then((res) => { if (!ignore) setPost(res.data); })
      .catch(() => { if (!ignore) setPost(null); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [id]);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!post) {
    return (
      <div style={{ textAlign: 'center', padding: '100px 0' }}>
        <Title level={3}>Không tìm thấy bài viết</Title>
        <Button type="primary" onClick={() => navigate('/blog')} style={{ marginTop: 16 }}>
          Quay lại tin tức
        </Button>
      </div>
    );
  }

  const getImageSrc = (v) => {
    if (!v) return 'https://placehold.co/800x400?text=No+Image';
    if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
    return `/uploads/${v}`;
  };

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 20px' }}>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/blog')}
        style={{ padding: 0, marginBottom: 20, fontWeight: 500, fontSize: 14 }}
      >
        Quay lại tin tức
      </Button>

      <div
        style={{
          background: '#fff',
          borderRadius: 16,
          overflow: 'hidden',
          border: '1px solid var(--gray-100)',
        }}
      >
        <img
          src={getImageSrc(post.img)}
          alt={post.title}
          style={{
            width: '100%',
            height: 400,
            objectFit: 'cover',
          }}
        />

        <div style={{ padding: 40 }}>
          <Tag
            color="purple"
            style={{
              borderRadius: 100,
              padding: '4px 16px',
              fontWeight: 600,
              fontSize: 12,
              marginBottom: 16,
            }}
          >
            {post.categoryName || 'Tin công nghệ'}
          </Tag>

          <Title level={1} style={{ fontWeight: 700, margin: '0 0 16px', fontSize: 32, lineHeight: 1.3 }}>
            {post.title}
          </Title>

          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 20,
              marginBottom: 24,
              color: 'var(--gray-400)',
              fontSize: 13,
            }}
          >
            <span><UserOutlined /> Admin</span>
            <span><ClockCircleOutlined /> {post.createdAt ? new Date(post.createdAt).toLocaleDateString('vi-VN') : 'Hôm nay'}</span>
          </div>

          <div
            style={{
              color: 'var(--gray-600)',
              lineHeight: 1.9,
              fontSize: 16,
            }}
            dangerouslySetInnerHTML={{ __html: post.content || 'Nội dung đang được cập nhật...' }}
          />
        </div>
      </div>
    </div>
  );
}
