import { useState } from 'react';
import { Form, Input, Button, Typography, message, Card } from 'antd';
import { MailOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, Link } from 'react-router-dom';
import { forgotPassword } from '../api/authApi';

const { Title, Text } = Typography;

export default function ForgotPasswordPage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [sent, setSent] = useState(false);

  const handleSubmit = async (values) => {
    setLoading(true);
    try {
      await forgotPassword(values.email);
      setSent(true);
    } catch {
      setSent(true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 72px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #f0f4ff 0%, #fdf2f8 100%)',
        padding: 24,
      }}
    >
      <Card
        style={{
          width: 440,
          borderRadius: 16,
          boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
          border: '1px solid rgba(0,0,0,0.04)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ margin: 0, fontWeight: 700 }}>
            Quên mật khẩu
          </Title>
          <Text style={{ color: 'var(--gray-500)' }}>
            {sent ? 'Email đặt lại mật khẩu đã được gửi' : 'Nhập email để nhận link đặt lại mật khẩu'}
          </Text>
        </div>

        {sent ? (
          <div style={{ textAlign: 'center' }}>
            <div
              style={{
                width: 64, height: 64, borderRadius: '50%',
                background: '#d4edda', display: 'inline-flex',
                alignItems: 'center', justifyContent: 'center',
                fontSize: 32, marginBottom: 16, color: '#155724',
              }}
            >
              ✓
            </div>
            <Text style={{ display: 'block', marginBottom: 8 }}>
              Nếu email tồn tại, link đặt lại mật khẩu sẽ được gửi đến hộp thư của bạn.
            </Text>
            <Text style={{ display: 'block', marginBottom: 20, color: 'var(--gray-500)', fontSize: 13 }}>
              Vui lòng kiểm tra email và nhấp vào link để tạo mật khẩu mới.
            </Text>
            <Link to="/login">
              <Button icon={<ArrowLeftOutlined />}>Quay lại đăng nhập</Button>
            </Link>
          </div>
        ) : (
          <Form onFinish={handleSubmit} layout="vertical" size="large">
            <Form.Item
              name="email"
              rules={[{ required: true, type: 'email', message: 'Email không hợp lệ' }]}
            >
              <Input
                prefix={<MailOutlined style={{ color: 'var(--gray-400)' }} />}
                placeholder="Email của bạn"
                style={{ borderRadius: 8 }}
              />
            </Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              style={{ height: 44, borderRadius: 10, fontWeight: 600, fontSize: 15 }}
            >
              Gửi link đặt lại mật khẩu
            </Button>
            <div style={{ textAlign: 'center', marginTop: 16 }}>
              <Link to="/login" style={{ color: 'var(--primary)' }}>Quay lại đăng nhập</Link>
            </div>
          </Form>
        )}
      </Card>
    </div>
  );
}
