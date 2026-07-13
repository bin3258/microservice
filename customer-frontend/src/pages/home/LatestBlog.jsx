import { Row, Col, Spin, Typography, Button } from 'antd';
import { RightOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import PostCard from '../../components/PostCard';

const { Title } = Typography;

export default function LatestBlog({ posts, loading }) {
  const navigate = useNavigate();

  if (loading) {
    return (
      <div style={{ textAlign: 'center', padding: 40 }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!posts?.length) return null;

  return (
    <div style={{ paddingTop: 40 }}>
      <div
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: 24,
        }}
      >
        <Title level={2} style={{ margin: 0, fontWeight: 700, fontSize: 24 }}>
          Bài viết mới nhất
        </Title>
        <Button
          type="link"
          icon={<RightOutlined />}
          iconPlacement="end"
          onClick={() => navigate('/blog')}
          style={{ fontWeight: 600, fontSize: 14 }}
        >
          Xem tất cả
        </Button>
      </div>
      <Row gutter={[20, 20]}>
        {posts.map((p, index) => (
          <Col key={p.id} xs={24} sm={12} md={8}>
            <PostCard post={p} delay={index * 0.1} />
          </Col>
        ))}
      </Row>
    </div>
  );
}
