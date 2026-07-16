import { useState, useEffect } from 'react';
import { Typography, Card, Row, Col, Button, Tag, Modal, Form, Input, Spin, Empty, message, Space, Popconfirm, Divider } from 'antd';
import { PlusOutlined, DeleteOutlined, EditOutlined, CheckCircleOutlined, HomeOutlined, UserOutlined, MailOutlined, PhoneOutlined, LockOutlined, KeyOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/useAuth';
import { customerService, addressService } from '../../services/orderService';
import api from '../../api/axiosClient';

const { Title, Text } = Typography;

export default function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const [customer, setCustomer] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);

  const [accModal, setAccModal] = useState(false);
  const [accSubmitting, setAccSubmitting] = useState(false);
  const [accForm] = Form.useForm();

  const [addrModal, setAddrModal] = useState(false);
  const [addrEditing, setAddrEditing] = useState(null);
  const [addrSubmitting, setAddrSubmitting] = useState(false);
  const [addrForm] = Form.useForm();

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    let ignore = false;
    (async () => {
      try {
        const customerRes = await customerService.getByAuthId(user.userId);
        if (ignore) return;
        const cus = customerRes.data;
        setCustomer(cus);
        const addrRes = await addressService.getByCustomer(cus.id);
        if (ignore) return;
        setAddresses(addrRes.data);
      } catch {
        message.error('Không thể tải thông tin');
      } finally {
        if (!ignore) setLoading(false);
      }
    })();
    return () => { ignore = true; };
  }, [user, navigate]);

  const handleUpdateAccount = async (values) => {
    setAccSubmitting(true);
    try {
      await customerService.update(customer.id, {
        fullName: values.fullName,
        phone: values.phone,
        email: values.email,
      });
      if (values.currentPassword && values.newPassword) {
        await api.put(`/auth/${user.userId}/password`, {
          currentPassword: values.currentPassword,
          newPassword: values.newPassword,
        });
      }
      setAccModal(false);
      message.success('Cập nhật thành công. Vui lòng đăng nhập lại.');
      setTimeout(() => {
        logout();
        navigate('/login');
      }, 500);
    } catch (err) {
      message.error(err.response?.data?.message || 'Cập nhật thất bại');
    } finally {
      setAccSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    try {
      await addressService.delete(id);
      setAddresses((prev) => prev.filter((a) => a.id !== id));
      message.success('Đã xóa địa chỉ');
    } catch {
      message.error('Xóa thất bại');
    }
  };

  const handleSetDefault = async (id) => {
    if (!customer) return;
    try {
      await addressService.setDefault(id, customer.id);
      setAddresses((prev) =>
        prev.map((a) => ({ ...a, isDefault: a.id === id })),
      );
      message.success('Đã đặt làm mặc định');
    } catch {
      message.error('Cập nhật thất bại');
    }
  };

  const openAddAddress = () => {
    setAddrEditing(null);
    addrForm.resetFields();
    setAddrModal(true);
  };

  const openEditAddress = (addr) => {
    setAddrEditing(addr);
    addrForm.setFieldsValue({ street: addr.street, ward: addr.ward, city: addr.city });
    setAddrModal(true);
  };

  const handleSaveAddress = async (values) => {
    if (!customer) return;
    setAddrSubmitting(true);
    try {
      if (addrEditing) {
        const res = await addressService.update(addrEditing.id, {
          customerId: customer.id,
          street: values.street,
          ward: values.ward || '',
          city: values.city,
          default: addrEditing.isDefault,
        });
        setAddresses((prev) => prev.map((a) => (a.id === addrEditing.id ? res.data : a)));
        message.success('Cập nhật địa chỉ thành công');
      } else {
        const res = await addressService.create({
          customerId: customer.id,
          street: values.street,
          ward: values.ward || '',
          city: values.city,
          default: addresses.length === 0,
        });
        setAddresses((prev) => [...prev, res.data]);
        message.success('Thêm địa chỉ thành công');
      }
      addrForm.resetFields();
      setAddrModal(false);
    } catch (err) {
      message.error(err.response?.data?.message || 'Lưu địa chỉ thất bại');
    } finally {
      setAddrSubmitting(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;
  if (!customer) return <div style={{ textAlign: 'center', padding: 80 }}><Empty description="Không tìm thấy thông tin" /></div>;

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 20px' }}>
      <Title level={2} style={{ fontWeight: 700, marginBottom: 24, fontSize: 24 }}>Tài khoản của tôi</Title>

      <Row gutter={[24, 24]}>
        <Col xs={24} md={8}>
          <Card
            style={{ borderRadius: 12, textAlign: 'center' }}
            actions={[
              <Button type="link" icon={<EditOutlined />} onClick={() => { accForm.setFieldsValue({ fullName: customer.fullName, phone: customer.phone, email: customer.email, password: '' }); setAccModal(true); }}>Sửa thông tin</Button>,
              <Button type="link" icon={<KeyOutlined />} onClick={() => navigate('/forgot-password')}>Đổi mật khẩu</Button>,
            ]}
          >
            <div style={{ width: 64, height: 64, borderRadius: '50%', background: 'linear-gradient(135deg, var(--primary), var(--accent))', display: 'inline-flex', alignItems: 'center', justifyContent: 'center', fontSize: 28, color: '#fff', fontWeight: 700, marginBottom: 12 }}>
              {customer.fullName?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <Title level={4} style={{ margin: 0, fontWeight: 600 }}>{customer.fullName}</Title>
            <Text style={{ color: 'var(--gray-500)', display: 'block' }}>{customer.email}</Text>
            <Text style={{ color: 'var(--gray-400)', fontSize: 13 }}>{customer.phone}</Text>
          </Card>
        </Col>

        <Col xs={24} md={16}>
          <Card
            title={<span><HomeOutlined /> Địa chỉ của tôi</span>}
            extra={
              <Button type="primary" icon={<PlusOutlined />} onClick={openAddAddress}>
                Thêm địa chỉ
              </Button>
            }
            style={{ borderRadius: 12 }}
          >
            {addresses.length === 0 ? (
              <Empty description="Chưa có địa chỉ nào" />
            ) : (
              <Space direction="vertical" size={12} style={{ width: '100%' }}>
                {addresses.map((addr) => (
                  <Card
                    key={addr.id}
                    size="small"
                    style={{
                      borderRadius: 8,
                      border: addr.isDefault ? '1px solid var(--primary)' : '1px solid var(--gray-200)',
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                      <div>
                        <Text strong style={{ fontSize: 14 }}>
                          {addr.street}
                          {addr.ward ? `, ${addr.ward}` : ''}
                          {addr.city ? `, ${addr.city}` : ''}
                        </Text>
                        {addr.isDefault && (
                          <Tag color="blue" style={{ marginLeft: 8 }}>
                            <CheckCircleOutlined /> Mặc định
                          </Tag>
                        )}
                      </div>
                      <Space>
                        <Button size="small" type="link" icon={<EditOutlined />} onClick={() => openEditAddress(addr)} />
                        {!addr.isDefault && (
                          <>
                            <Button size="small" type="link" onClick={() => handleSetDefault(addr.id)}>
                              Đặt làm mặc định
                            </Button>
                            <Popconfirm title="Xóa địa chỉ này?" onConfirm={() => handleDelete(addr.id)} okText="Xóa" cancelText="Hủy">
                              <Button size="small" danger icon={<DeleteOutlined />} />
                            </Popconfirm>
                          </>
                        )}
                      </Space>
                    </div>
                  </Card>
                ))}
              </Space>
            )}
          </Card>
        </Col>
      </Row>

      <Modal title="Sửa thông tin tài khoản" open={accModal} onCancel={() => setAccModal(false)} footer={null} destroyOnClose>
        <Form form={accForm} layout="vertical" onFinish={handleUpdateAccount}>
          <Form.Item name="fullName" label="Họ tên" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
            <Input prefix={<UserOutlined />} />
          </Form.Item>
          <Form.Item name="phone" label="Số điện thoại" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
            <Input prefix={<PhoneOutlined />} />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email', message: 'Email không hợp lệ' }]}>
            <Input prefix={<MailOutlined />} />
          </Form.Item>
          <Divider />
          <Form.Item
            name="currentPassword"
            label="Mật khẩu hiện tại"
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu hiện tại' },
              { min: 6, message: 'Tối thiểu 6 ký tự' },
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Mật khẩu hiện tại" />
          </Form.Item>
          <Form.Item
            name="newPassword"
            label="Mật khẩu mới"
            dependencies={['currentPassword']}
            rules={[
              { required: true, message: 'Vui lòng nhập mật khẩu mới' },
              { min: 6, message: 'Tối thiểu 6 ký tự' },
              ({ getFieldValue }) => ({
                validator(_, value) {
                  if (!value || getFieldValue('currentPassword') !== value) {
                    return Promise.resolve();
                  }
                  return Promise.reject(new Error('Mật khẩu mới phải khác mật khẩu hiện tại'));
                },
              }),
            ]}
          >
            <Input.Password prefix={<LockOutlined />} placeholder="Nhập mật khẩu mới" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={accSubmitting}>
            Lưu thay đổi
          </Button>
        </Form>
      </Modal>

      <Modal title={addrEditing ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới'} open={addrModal} onCancel={() => setAddrModal(false)} footer={null} destroyOnClose>
        <Form form={addrForm} layout="vertical" onFinish={handleSaveAddress}>
          <Form.Item name="street" label="Số nhà, tên đường" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
            <Input placeholder="123 Nguyễn Huệ" />
          </Form.Item>
          <Form.Item name="ward" label="Phường / Xã">
            <Input placeholder="Phường Bến Nghé" />
          </Form.Item>
          <Form.Item name="city" label="Tỉnh / Thành phố" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
            <Input placeholder="TP. Hồ Chí Minh" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={addrSubmitting}>
            {addrEditing ? 'Lưu thay đổi' : 'Thêm địa chỉ'}
          </Button>
        </Form>
      </Modal>
    </div>
  );
}
