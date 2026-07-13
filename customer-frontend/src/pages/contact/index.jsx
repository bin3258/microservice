import { Row, Col, Typography, Card, Space, Form, Input, Button, message } from 'antd';
import { PhoneOutlined, MailOutlined, EnvironmentOutlined, ClockCircleOutlined, FacebookOutlined, InstagramOutlined, YoutubeOutlined } from '@ant-design/icons';

const { Title, Text, Paragraph } = Typography;

export default function ContactPage() {
  const [form] = Form.useForm();

  const handleSubmit = () => {
    message.success('Cảm ơn bạn! Chúng tôi sẽ phản hồi sớm nhất.');
    form.resetFields();
  };

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '48px 20px' }}>
      <div style={{ textAlign: 'center', marginBottom: 48 }}>
        <Title level={2} style={{ fontWeight: 700, margin: 0 }}>Liên hệ</Title>
        <Paragraph style={{ color: 'var(--gray-500)', marginTop: 8, fontSize: 15 }}>
          Chúng tôi luôn sẵn sàng hỗ trợ bạn
        </Paragraph>
      </div>

      <Row gutter={[40, 32]}>
        <Col xs={24} md={14}>
          <Card style={{ borderRadius: 12, border: '1px solid var(--gray-200)' }}>
            <Title level={4} style={{ marginBottom: 8 }}>Gửi tin nhắn cho chúng tôi</Title>
            <Text style={{ color: 'var(--gray-500)', display: 'block', marginBottom: 24 }}>
              Điền form bên dưới, chúng tôi sẽ phản hồi trong vòng 24h.
            </Text>
            <Form form={form} layout="vertical" onFinish={handleSubmit}>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="name" label="Họ tên" rules={[{ required: true, message: 'Vui lòng nhập họ tên' }]}>
                    <Input placeholder="Nguyễn Văn A" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Email không hợp lệ' }]}>
                    <Input placeholder="email@example.com" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="subject" label="Chủ đề">
                <Input placeholder="Hỗ trợ sản phẩm" />
              </Form.Item>
              <Form.Item name="message" label="Nội dung" rules={[{ required: true, message: 'Vui lòng nhập nội dung' }]}>
                <Input.TextArea rows={5} placeholder="Nhập nội dung..." />
              </Form.Item>
              <Button type="primary" htmlType="submit" size="large" style={{ borderRadius: 8 }}>
                Gửi tin nhắn
              </Button>
            </Form>
          </Card>
        </Col>

        <Col xs={24} md={10}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Card style={{ borderRadius: 12, border: '1px solid var(--gray-200)' }}>
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                {[
                  { icon: <EnvironmentOutlined style={{ fontSize: 20, color: 'var(--primary)' }} />, label: 'Địa chỉ', value: '123 Nguyễn Huệ, Quận 1, TP.HCM' },
                  { icon: <PhoneOutlined style={{ fontSize: 20, color: 'var(--primary)' }} />, label: 'Hotline', value: '1900 1234' },
                  { icon: <MailOutlined style={{ fontSize: 20, color: 'var(--primary)' }} />, label: 'Email', value: 'support@phonestore.vn' },
                  { icon: <ClockCircleOutlined style={{ fontSize: 20, color: 'var(--primary)' }} />, label: 'Giờ làm việc', value: 'T2 - CN: 8:00 - 21:00' },
                ].map((item) => (
                  <div key={item.label}>
                    <Space align="start" size={12}>
                      {item.icon}
                      <div>
                        <Text style={{ fontWeight: 600, fontSize: 13, color: 'var(--gray-500)' }}>{item.label}</Text>
                        <div style={{ fontSize: 14, fontWeight: 500 }}>{item.value}</div>
                      </div>
                    </Space>
                  </div>
                ))}
              </Space>
            </Card>

            <Card style={{ borderRadius: 12, border: '1px solid var(--gray-200)' }}>
              <Text style={{ fontWeight: 600, display: 'block', marginBottom: 16 }}>Kết nối với chúng tôi</Text>
              <Space size={12}>
                {[
                  { icon: <FacebookOutlined />, color: '#1877F2' },
                  { icon: <InstagramOutlined />, color: '#E4405F' },
                  { icon: <YoutubeOutlined />, color: '#FF0000' },
                ].map((s, i) => (
                  <div key={i} style={{ width: 40, height: 40, borderRadius: 10, background: 'var(--gray-100)', display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer', fontSize: 18, color: s.color }}>
                    {s.icon}
                  </div>
                ))}
              </Space>
            </Card>
          </Space>
        </Col>
      </Row>
    </div>
  );
}
