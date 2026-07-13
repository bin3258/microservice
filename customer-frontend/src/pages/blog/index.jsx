import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Row, Col, Typography, Spin, Input, Button } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import PostCard from '../../components/PostCard';
import BlogSidebar from './BlogSidebar';
import { blogService } from '../../services/blogService';

const { Title } = Typography;

export default function BlogPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [posts, setPosts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  const selectedCategory = searchParams.get('category');

  useEffect(() => {
    let ignore = false;
    Promise.all([
      blogService.getAll(),
      blogService.getCategories(),
    ])
      .then(([postsRes, catsRes]) => {
        if (!ignore) {
          const published = postsRes.data.filter((p) => p.status === 'PUBLISHED');
          setPosts(published);
          setCategories(catsRes.data || []);
        }
      })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, []);

  const filtered = posts.filter((p) => {
    let match = true;
    if (selectedCategory) {
      match = String(p.categoryId) === selectedCategory;
    }
    if (match && searchTerm) {
      match = p.title?.toLowerCase().includes(searchTerm.toLowerCase());
    }
    return match;
  });

  const handleCategoryClick = (catId) => {
    if (catId) {
      setSearchParams({ category: catId });
    } else {
      setSearchParams({});
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 20px' }}>
      <div style={{ textAlign: 'center', marginBottom: 40 }}>
        <Title level={2} style={{ fontWeight: 700, fontSize: 32, marginBottom: 8 }}>
          Tin tức công nghệ
        </Title>
        <p style={{ color: 'var(--gray-500)', fontSize: 15, maxWidth: 500, margin: '0 auto 20px' }}>
          Cập nhật những thông tin mới nhất về công nghệ và sản phẩm
        </p>
        <Input
          placeholder="Tìm kiếm bài viết..."
          prefix={<SearchOutlined style={{ color: 'var(--gray-400)' }} />}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          style={{ maxWidth: 400 }}
          size="large"
          allowClear
        />
      </div>

      <div style={{ marginBottom: 32, textAlign: 'center' }}>
        <Button
          shape="round"
          size="large"
          type={!selectedCategory ? 'primary' : 'default'}
          onClick={() => handleCategoryClick(null)}
          style={{ margin: '0 6px 8px' }}
        >
          Tất cả
        </Button>
        {categories.map((cat) => (
          <Button
            key={cat.id}
            shape="round"
            size="large"
            type={selectedCategory === String(cat.id) ? 'primary' : 'default'}
            onClick={() => handleCategoryClick(cat.id)}
            style={{ margin: '0 6px 8px' }}
          >
            {cat.name}
          </Button>
        ))}
      </div>

      <Row gutter={[32, 24]}>
        <Col xs={24} md={18}>
          {loading ? (
            <div style={{ textAlign: 'center', padding: '60px 0' }}>
              <Spin size="large" />
            </div>
          ) : filtered.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--gray-400)' }}>
              Không tìm thấy bài viết nào.
            </div>
          ) : (
            <Row gutter={[20, 20]}>
              {filtered.map((p, index) => (
                <Col key={p.id} xs={24} sm={12}>
                  <PostCard post={p} delay={index * 0.05} />
                </Col>
              ))}
            </Row>
          )}
        </Col>
        <Col xs={24} md={6}>
          <div
            style={{
              position: 'sticky',
              top: 88,
              background: '#fff',
              borderRadius: 12,
              border: '1px solid var(--gray-100)',
            }}
          >
            <BlogSidebar />
          </div>
        </Col>
      </Row>
    </div>
  );
}
