import { useState, useEffect } from 'react';
import { Typography, Card, Button, Spin, Empty, message, Tag, Table, Space, Steps, Modal, Input } from 'antd';
import { ArrowLeftOutlined, ShoppingCartOutlined, UserOutlined, PhoneOutlined, MailOutlined, CheckCircleOutlined, ClockCircleOutlined, CloseCircleOutlined, LoadingOutlined, EnvironmentOutlined, FileTextOutlined, EditOutlined, StopOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { orderService } from '../../services/orderService';

const { Title, Text } = Typography;

const STATUS_STEPS = {
  PENDING: 0,
  CONFIRMED: 1,
  SHIPPING: 2,
  DELIVERED: 3,
};

const STATUS_MAP = {
  PENDING: { color: 'default', icon: <ClockCircleOutlined />, text: 'Chờ xác nhận' },
  CONFIRMED: { color: 'blue', icon: <CheckCircleOutlined />, text: 'Đã xác nhận' },
  SHIPPING: { color: 'cyan', icon: <LoadingOutlined />, text: 'Đang giao hàng' },
  DELIVERED: { color: 'success', icon: <CheckCircleOutlined />, text: 'Đã giao hàng' },
  CANCELLED: { color: 'error', icon: <CloseCircleOutlined />, text: 'Đã hủy' },
  PROCESSING: { color: 'processing', icon: <LoadingOutlined />, text: 'Đang xử lý' },
  PAID: { color: 'blue', icon: <CheckCircleOutlined />, text: 'Đã thanh toán' },
  COMPLETED: { color: 'success', icon: <CheckCircleOutlined />, text: 'Hoàn thành' },
  FAILED: { color: 'error', icon: <CloseCircleOutlined />, text: 'Thất bại' },
};

export default function OrderDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editAddress, setEditAddress] = useState('');
  const [editNote, setEditNote] = useState('');
  const [saving, setSaving] = useState(false);

  function loadOrder() {
    orderService.getById(id)
      .then(res => setOrder(res.data))
      .catch(() => message.error('Không thể tải thông tin đơn hàng'))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    let ignore = false;
    orderService.getById(id)
      .then(res => { if (!ignore) setOrder(res.data); })
      .catch(() => { if (!ignore) message.error('Không thể tải thông tin đơn hàng'); })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, [id]);

  const handleCancel = async () => {
    Modal.confirm({
      title: 'Hủy đơn hàng',
      content: 'Bạn có chắc muốn hủy đơn hàng này?',
      onOk: async () => {
        try {
          await orderService.cancel(order.orderId);
          message.success('Đã hủy đơn hàng');
          loadOrder();
        } catch (err) {
          message.error(err.response?.data?.message || 'Hủy đơn hàng thất bại');
        }
      },
    });
  };

  const openEditModal = () => {
    setEditAddress(order.shippingAddress || '');
    setEditNote(order.note || '');
    setEditModalOpen(true);
  };

  const handleSaveEdit = async () => {
    setSaving(true);
    try {
      await orderService.update(order.orderId, {
        shippingAddress: editAddress,
        note: editNote,
      });
      message.success('Đã cập nhật đơn hàng');
      setEditModalOpen(false);
      loadOrder();
    } catch (err) {
      message.error(err.response?.data?.message || 'Cập nhật thất bại');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;
  if (!order) return <div style={{ textAlign: 'center', padding: 80 }}><Empty description="Không tìm thấy đơn hàng" /></div>;

  const statusInfo = STATUS_MAP[order.status] || { color: 'default', icon: null, text: order.status };
  const isCancelled = order.status === 'CANCELLED';
  const currentStep = STATUS_STEPS[order.status] ?? -1;

  const columns = [
    {
      title: 'Sản phẩm',
      dataIndex: 'product',
      key: 'product',
      render: (product) => (
        <Space>
          {product?.img && (
            <img
              src={
                (() => {
                  const img = product?.img;
                  if (!img) return 'https://placehold.co/48x48';
                  if (img.startsWith('http://') || img.startsWith('https://') || img.startsWith('/') || img.startsWith('uploads/')) return img;
                  return `/uploads/${img}`;
                })()
              }
              alt={product.name}
              style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 8 }}
            />
          )}
          <div>
            <Text strong>{product?.name}</Text>
            <br />
            <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Mã SP: #{product?.id}</Text>
          </div>
        </Space>
      ),
    },
    {
      title: 'Đơn giá',
      dataIndex: 'unitPrice',
      key: 'unitPrice',
      width: 120,
      align: 'right',
      render: (val) => `${val?.toLocaleString('vi-VN')}₫`,
    },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 80,
      align: 'center',
    },
    {
      title: 'Thành tiền',
      dataIndex: 'lineTotal',
      key: 'lineTotal',
      width: 120,
      align: 'right',
      render: (val) => <Text strong style={{ color: 'var(--primary)' }}>{val?.toLocaleString('vi-VN')}₫</Text>,
    },
  ];

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '32px 20px' }}>
      <Button
        type="link"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/orders')}
        style={{ padding: 0, marginBottom: 16, fontWeight: 500 }}
      >
        Quay lại đơn hàng
      </Button>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <Title level={2} style={{ fontWeight: 700, margin: 0, fontSize: 24 }}>
          <ShoppingCartOutlined /> Đơn hàng #{order.orderId}
        </Title>
        <Tag color={statusInfo.color} style={{ borderRadius: 4, padding: '4px 12px', fontSize: 13 }}>
          {statusInfo.icon} {statusInfo.text}
        </Tag>
      </div>

      {!isCancelled && currentStep >= 0 && (
        <Card style={{ borderRadius: 12, marginBottom: 20, border: '1px solid var(--gray-100)' }}>
          <Steps
            current={currentStep}
            size="small"
            items={[
              { title: 'Chờ xác nhận', icon: <ClockCircleOutlined /> },
              { title: 'Đã xác nhận', icon: <CheckCircleOutlined /> },
              { title: 'Đang giao hàng', icon: <LoadingOutlined /> },
              { title: 'Đã giao hàng', icon: <CheckCircleOutlined /> },
            ]}
          />
        </Card>
      )}

      <Card style={{ borderRadius: 12, marginBottom: 20, border: '1px solid var(--gray-100)' }}>
        <Space size={24} wrap>
          <Space>
            <UserOutlined style={{ color: 'var(--gray-400)' }} />
            <div>
              <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Khách hàng</Text>
              <br />
              <Text strong>{order.user?.name}</Text>
            </div>
          </Space>
          <Space>
            <PhoneOutlined style={{ color: 'var(--gray-400)' }} />
            <div>
              <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Số điện thoại</Text>
              <br />
              <Text strong>{order.user?.phone}</Text>
            </div>
          </Space>
          <Space>
            <MailOutlined style={{ color: 'var(--gray-400)' }} />
            <div>
              <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Email</Text>
              <br />
              <Text strong>{order.user?.email}</Text>
            </div>
          </Space>
        </Space>
      </Card>

      {order.shippingAddress && (
        <Card style={{ borderRadius: 12, marginBottom: 20, border: '1px solid var(--gray-100)' }}>
          <Space>
            <EnvironmentOutlined style={{ color: 'var(--gray-400)', fontSize: 16 }} />
            <div>
              <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Địa chỉ giao hàng</Text>
              <br />
              <Text strong>{order.shippingAddress}</Text>
            </div>
          </Space>
        </Card>
      )}

      {order.note && (
        <Card style={{ borderRadius: 12, marginBottom: 20, border: '1px solid var(--gray-100)' }}>
          <Space>
            <FileTextOutlined style={{ color: 'var(--gray-400)', fontSize: 16 }} />
            <div>
              <Text style={{ color: 'var(--gray-400)', fontSize: 12 }}>Ghi chú</Text>
              <br />
              <Text strong style={{ fontStyle: 'italic' }}>{order.note}</Text>
            </div>
          </Space>
        </Card>
      )}

      <Card
        title={<Text strong>Chi tiết đơn hàng</Text>}
        style={{ borderRadius: 12, border: '1px solid var(--gray-100)' }}
      >
        <Table
          columns={columns}
          dataSource={order.items}
          rowKey="productId"
          pagination={false}
          summary={() => (
            <Table.Summary>
              {order.shippingFee > 0 && (
                <Table.Summary.Row>
                  <Table.Summary.Cell index={0} colSpan={2}>
                    <Text>Phí vận chuyển</Text>
                  </Table.Summary.Cell>
                  <Table.Summary.Cell index={1} align="center" />
                  <Table.Summary.Cell index={2} align="right">
                    <Text>{order.shippingFee?.toLocaleString('vi-VN')}₫</Text>
                  </Table.Summary.Cell>
                </Table.Summary.Row>
              )}
              <Table.Summary.Row>
                <Table.Summary.Cell index={0} colSpan={2}>
                  <Text strong>Tổng cộng</Text>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={1} align="center">
                  <Text strong>{order.totalQuantity}</Text>
                </Table.Summary.Cell>
                <Table.Summary.Cell index={2} align="right">
                  <Title level={4} style={{ color: 'var(--primary)', margin: 0, fontSize: 20 }}>
                    {order.totalPrice?.toLocaleString('vi-VN')}₫
                  </Title>
                </Table.Summary.Cell>
              </Table.Summary.Row>
            </Table.Summary>
          )}
        />
      </Card>

      <div style={{ marginTop: 24, display: 'flex', gap: 12 }}>
        {order.status === 'PENDING' && (
          <>
            <Button icon={<EditOutlined />} onClick={openEditModal}>Sửa thông tin</Button>
            <Button danger icon={<StopOutlined />} onClick={handleCancel}>Hủy đơn hàng</Button>
          </>
        )}
      </div>

      <Modal
        title="Sửa thông tin đơn hàng"
        open={editModalOpen}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalOpen(false)}
        confirmLoading={saving}
        okText="Lưu"
      >
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <div>
            <Text strong>Địa chỉ giao hàng</Text>
            <Input.TextArea
              value={editAddress}
              onChange={(e) => setEditAddress(e.target.value)}
              rows={2}
              style={{ marginTop: 4 }}
            />
          </div>
          <div>
            <Text strong>Ghi chú</Text>
            <Input.TextArea
              value={editNote}
              onChange={(e) => setEditNote(e.target.value)}
              rows={2}
              style={{ marginTop: 4 }}
            />
          </div>
        </Space>
      </Modal>
    </div>
  );
}
