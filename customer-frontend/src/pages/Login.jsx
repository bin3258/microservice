import { useState } from 'react';
import { Form, Input, Button, Tabs, Typography, message, Card, Row, Col } from 'antd';
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import api from '../api/axiosClient';

const { Title, Text } = Typography;

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('login');

  const handleLogin = async (values) => {
    setLoading(true);
    try {
      await login(values.username, values.password);
      message.success('Đăng nhập thành công');
      navigate('/');
    } catch (err) {
      message.error(err.response?.data?.message || 'Sai tài khoản hoặc mật khẩu');
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (values) => {
    setLoading(true);
    try {
      const cusRes = await api.post('/customers', {
        fullName: values.fullName,
        phone: values.phone,
        email: values.email,
        password: values.password,
      });
      const { id: customerId, token } = cusRes.data;
      if (token) {
        localStorage.setItem('token', token);
        try {
          await api.post('/addresses', {
            customerId,
            street: values.street,
            ward: values.ward || '',
            city: values.city,
            default: true,
          });
        } catch (addrErr) {
          console.error('Address creation failed:', addrErr);
        }
        localStorage.removeItem('token');
      }
      message.success('Đăng ký thành công! Vui lòng đăng nhập.');
      setActiveTab('login');
    } catch (err) {
      message.error(err.response?.data?.message || 'Đăng ký thất bại');
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
          width: 460,
          borderRadius: 16,
          boxShadow: '0 8px 32px rgba(0,0,0,0.08)',
          border: '1px solid rgba(0,0,0,0.04)',
        }}
      >
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 14,
              background: 'linear-gradient(135deg, var(--primary), var(--accent))',
              display: 'inline-flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#fff',
              fontWeight: 800,
              fontSize: 24,
              marginBottom: 12,
            }}
          >
            P
          </div>
          <Title level={3} style={{ margin: 0, fontWeight: 700 }}>
            Chào mừng bạn
          </Title>
          <Text style={{ color: 'var(--gray-500)' }}>Đăng nhập hoặc tạo tài khoản mới</Text>
        </div>

        <Tabs
          centered
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'login',
              label: 'Đăng nhập',
              children: (
                <Form onFinish={handleLogin} layout="vertical" size="large">
                  <Form.Item
                    name="username"
                    rules={[{ required: true, message: 'Vui lòng nhập email' }]}
                  >
                    <Input
                      prefix={<UserOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Email"
                      style={{ borderRadius: 8 }}
                    />
                  </Form.Item>
                  <Form.Item
                    name="password"
                    rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
                  >
                    <Input.Password
                      prefix={<LockOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Mật khẩu"
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
                    Đăng nhập
                  </Button>
                </Form>
              ),
            },
            {
              key: 'register',
              label: 'Đăng ký',
              children: (
                <Form onFinish={handleRegister} layout="vertical" size="large">
                  <Form.Item
                    name="fullName"
                    rules={[{ required: true, message: 'Vui lòng nhập họ tên' }]}
                  >
                    <Input
                      prefix={<UserOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Họ tên"
                      style={{ borderRadius: 8 }}
                    />
                  </Form.Item>
                  <Form.Item
                    name="email"
                    rules={[
                      { required: true, type: 'email', message: 'Email không hợp lệ' },
                    ]}
                  >
                    <Input
                      prefix={<MailOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Email"
                      style={{ borderRadius: 8 }}
                    />
                  </Form.Item>
                  <Form.Item
                    name="phone"
                    rules={[{ required: true, message: 'Vui lòng nhập số điện thoại' }]}
                  >
                    <Input
                      prefix={<PhoneOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Số điện thoại"
                      style={{ borderRadius: 8 }}
                    />
                  </Form.Item>
                  <Form.Item
                    name="password"
                    rules={[{ required: true, min: 6, message: 'Tối thiểu 6 ký tự' }]}
                  >
                    <Input.Password
                      prefix={<LockOutlined style={{ color: 'var(--gray-400)' }} />}
                      placeholder="Mật khẩu"
                      style={{ borderRadius: 8 }}
                    />
                  </Form.Item>

                  <Title level={5} style={{ fontWeight: 600, marginTop: 8, marginBottom: 12, fontSize: 14 }}>
                    Địa chỉ mặc định
                  </Title>
                  <Row gutter={12}>
                    <Col span={24}>
                      <Form.Item
                        name="street"
                        rules={[{ required: true, message: 'Vui lòng nhập số nhà, tên đường' }]}
                      >
                        <Input
                          prefix={<EnvironmentOutlined style={{ color: 'var(--gray-400)' }} />}
                          placeholder="Số nhà, tên đường"
                          style={{ borderRadius: 8 }}
                        />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item name="ward">
                        <Input placeholder="Phường / Xã" style={{ borderRadius: 8 }} />
                      </Form.Item>
                    </Col>
                    <Col span={12}>
                      <Form.Item
                        name="city"
                        rules={[{ required: true, message: 'Vui lòng nhập tỉnh / thành phố' }]}
                      >
                        <Input placeholder="Tỉnh / Thành phố" style={{ borderRadius: 8 }} />
                      </Form.Item>
                    </Col>
                  </Row>

                  <Button
                    type="primary"
                    htmlType="submit"
                    block
                    loading={loading}
                    style={{ height: 44, borderRadius: 10, fontWeight: 600, fontSize: 15 }}
                  >
                    Đăng ký
                  </Button>
                </Form>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}
