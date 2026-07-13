import { Layout, Row, Col, Typography, Space, Divider } from 'antd';
import {
  PhoneOutlined, MailOutlined, EnvironmentOutlined,
  FacebookOutlined, InstagramOutlined, YoutubeOutlined,
} from '@ant-design/icons';

const { Footer: AntFooter } = Layout;
const { Title, Text } = Typography;

export default function Footer() {
  return (
    <AntFooter
      style={{
        background: 'var(--gray-900)',
        color: '#fff',
        padding: '60px 40px 24px',
      }}
    >
      <Row gutter={[48, 32]}>
        <Col xs={24} sm={12} md={6}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 16 }}>
            <div
              style={{
                width: 36,
                height: 36,
                borderRadius: 10,
                background: 'linear-gradient(135deg, var(--primary), var(--accent))',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#fff',
                fontWeight: 800,
                fontSize: 16,
              }}
            >
              P
            </div>
            <span style={{ fontSize: 20, fontWeight: 700, color: '#fff' }}>PhoneStore</span>
          </div>
          <Text style={{ color: 'var(--gray-400)', fontSize: 14, lineHeight: 1.8, display: 'block' }}>
            Cửa hàng điện thoại di động uy tín số 1 Việt Nam. Cam kết chính hãng 100% với giá tốt nhất thị trường.
          </Text>
          <Space style={{ marginTop: 16 }} size={12}>
            {[
              { icon: <FacebookOutlined />, color: '#1877F2' },
              { icon: <InstagramOutlined />, color: '#E4405F' },
              { icon: <YoutubeOutlined />, color: '#FF0000' },
            ].map((s, i) => (
              <div
                key={i}
                style={{
                  width: 36,
                  height: 36,
                  borderRadius: 8,
                  background: 'rgba(255,255,255,0.1)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  cursor: 'pointer',
                  transition: 'background 0.2s',
                  color: s.color,
                  fontSize: 16,
                }}
                onMouseEnter={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.2)'; }}
                onMouseLeave={(e) => { e.currentTarget.style.background = 'rgba(255,255,255,0.1)'; }}
              >
                {s.icon}
              </div>
            ))}
          </Space>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Title level={5} style={{ color: '#fff', marginBottom: 20, fontWeight: 600 }}>
            Chính sách
          </Title>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {[
              'Chính sách bảo hành',
              'Chính sách đổi trả',
              'Chính sách vận chuyển',
              'Chính sách bảo mật',
              'Chính sách thanh toán',
            ].map((item) => (
              <Text
                key={item}
                style={{
                  color: 'var(--gray-400)',
                  cursor: 'pointer',
                  transition: 'color 0.2s',
                  fontSize: 14,
                }}
                onMouseEnter={(e) => { e.target.style.color = '#fff'; }}
                onMouseLeave={(e) => { e.target.style.color = 'var(--gray-400)'; }}
              >
                {item}
              </Text>
            ))}
          </div>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Title level={5} style={{ color: '#fff', marginBottom: 20, fontWeight: 600 }}>
            Hỗ trợ khách hàng
          </Title>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {[
              'Trung tâm bảo hành',
              'Hướng dẫn mua hàng',
              'Hướng dẫn trả góp',
              'Câu hỏi thường gặp',
              'Góp ý khiếu nại',
            ].map((item) => (
              <Text
                key={item}
                style={{
                  color: 'var(--gray-400)',
                  cursor: 'pointer',
                  transition: 'color 0.2s',
                  fontSize: 14,
                }}
                onMouseEnter={(e) => { e.target.style.color = '#fff'; }}
                onMouseLeave={(e) => { e.target.style.color = 'var(--gray-400)'; }}
              >
                {item}
              </Text>
            ))}
          </div>
        </Col>

        <Col xs={24} sm={12} md={6}>
          <Title level={5} style={{ color: '#fff', marginBottom: 20, fontWeight: 600 }}>
            Liên hệ
          </Title>
          <Space direction="vertical" size={12}>
            {[
              { icon: <EnvironmentOutlined />, text: '123 Nguyễn Huệ, Q.1, TP.HCM' },
              { icon: <PhoneOutlined />, text: '1900 1234' },
              { icon: <MailOutlined />, text: 'support@phonestore.vn' },
            ].map((item, i) => (
              <div key={i} style={{ display: 'flex', alignItems: 'center', gap: 10, color: 'var(--gray-400)' }}>
                <span style={{ color: 'var(--primary-light)', fontSize: 16 }}>{item.icon}</span>
                <Text style={{ color: 'var(--gray-400)', fontSize: 14 }}>{item.text}</Text>
              </div>
            ))}
          </Space>
        </Col>
      </Row>

      <Divider style={{ borderColor: 'rgba(255,255,255,0.1)', margin: '32px 0 16px' }} />

      <Row justify="space-between" align="middle">
        <Col>
          <Text style={{ color: 'var(--gray-500)', fontSize: 13 }}>
            &copy; 2026 PhoneStore. All rights reserved.
          </Text>
        </Col>
        <Col>
          <Space size={16}>
            {['Visa', 'Mastercard', 'JCB', 'PayPal'].map((item) => (
              <Text
                key={item}
                style={{
                  color: 'var(--gray-500)',
                  fontSize: 12,
                  padding: '4px 10px',
                  background: 'rgba(255,255,255,0.05)',
                  borderRadius: 4,
                }}
              >
                {item}
              </Text>
            ))}
          </Space>
        </Col>
      </Row>
    </AntFooter>
  );
}
