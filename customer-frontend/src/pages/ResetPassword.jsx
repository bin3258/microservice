import { useState } from 'react';
import { Form, Input, Button, Typography, Card, message } from 'antd';
import { LockOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useSearchParams, useNavigate, Link } from 'react-router-dom';
import { resetPassword } from '../api/authApi';

const { Title, Text } = Typography;

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (values) => {
    if (!token) {
      message.error('Link đặt lại mật khẩu không hợp lệ hoặc đã hết hạn');
      return;
    }
    setLoading(true);
    try {
      await resetPassword(token, values.newPassword);
      message.success('Đặt lại mật khẩu thành công!');
      setTimeout(() => navigate('/login'), 1500);
    } catch (err) {
      message.error(err.response?.data || 'Liên kết không hợp lệ hoặc đã hết hạn');
    } finally {
      setLoading(false);
    }
  };

  if (!token) {
    return (
      <div
        style={{
          minHeight: 'calc(100vh - 72px)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 24,
        }}
      >
        <Card style={{ width: 440, textAlign: 'center' }}>
          <Title level={4}>Liên kết không hợp lệ</Title>
          <Text>Liên kết đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.</Text>
          <div style={{ marginTop: 16 }}>
            <Link to="/forgot-password">Yêu cầu lại link mới</Link>
          </div>
        </Card>
      </div>
    );
  }

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
            Đặt lại mật khẩu
          </Title>
          <Text style={{ color: 'var(--gray-500)' }}>
            Nhập mật khẩu mới cho tài khoản của bạn
          </Text>
        </div>

        <Form onFinish={handleSubmit} layout="vertical" size="large">
          <Form.Item
            name="newPassword"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu mới' },
              { min: 6, message: 'Mật khẩu phải có ít nhất 6 ký tự' },
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: 'var(--gray-400)' }} />}
              placeholder="Mật khẩu mới"
              style={{ borderRadius: 8 }}
            />
          </Form.Item>
          <Form.Item
            name="confirmPassword"
            dependencies={['newPassword']}
            rules={[
              { required: true, message: 'Vui lòng xác nhận mật khẩu' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('newPassword') === value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Mật khẩu xác nhận không khớp'));
                },
              }),
            ]}
          >
            <Input.Password
              prefix={<LockOutlined style={{ color: 'var(--gray-400)' }} />}
              placeholder="Xác nhận mật khẩu mới"
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
            Đặt lại mật khẩu
          </Button>
          <div style={{ textAlign: 'center', marginTop: 16 }}>
            <Link to="/login" style={{ color: 'var(--primary)' }}>Quay lại đăng nhập</Link>
          </div>
        </Form>
      </Card>
    </div>
  );
}
