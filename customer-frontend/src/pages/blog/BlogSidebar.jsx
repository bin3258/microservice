import { useState, useEffect } from 'react';
import { Typography, Spin, Space } from 'antd';
import { ClockCircleOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { blogService } from '../../services/blogService';

const { Title, Text } = Typography;

export default function BlogSidebar() {
  const navigate = useNavigate();
  const [recentPosts, setRecentPosts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    blogService.getAll()
      .then((res) => {
        if (!ignore) {
          setRecentPosts(res.data.filter((p) => p.status === 'PUBLISHED').slice(0, 4));
        }
      })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, []);

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 24 }}>
        <Spin size="small" />
      </div>
    );
  }

  return (
    <div style={{ padding: 20 }}>
      <Title level={5} style={{ fontWeight: 600, marginBottom: 12 }}>
        Bài viết gần đây
      </Title>
      <Space direction="vertical" size={12} style={{ width: '100%' }}>
        {recentPosts.map((post) => (
          <div
            key={post.id}
            style={{
              display: 'flex',
              gap: 10,
              cursor: 'pointer',
              padding: '8px 0',
              borderBottom: '1px solid var(--gray-100)',
            }}
            onClick={() => navigate(`/blog/${post.id}`)}
          >
            <img
              src={
                (() => {
                  const img = post.img;
                  if (!img) return 'https://placehold.co/60x60';
                  if (img.startsWith('http://') || img.startsWith('https://') || img.startsWith('/')) return img;
                  return `/uploads/${img}`;
                })()
              }
              alt={post.title}
              style={{
                width: 60,
                height: 60,
                borderRadius: 8,
                objectFit: 'cover',
                flexShrink: 0,
              }}
            />
            <div>
              <Text style={{ fontSize: 13, fontWeight: 500, display: 'block', lineHeight: 1.3 }}>
                {post.title}
              </Text>
              <Text style={{ fontSize: 11, color: 'var(--gray-400)', display: 'flex', alignItems: 'center', gap: 4, marginTop: 4 }}>
                <ClockCircleOutlined /> {post.createdAt ? new Date(post.createdAt).toLocaleDateString('vi-VN') : ''}
              </Text>
            </div>
          </div>
        ))}
      </Space>
    </div>
  );
}
