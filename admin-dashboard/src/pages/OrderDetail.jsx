import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import {
  Descriptions, Button, Space, Divider, Typography, Select, message,
  Modal, Input, InputNumber, Tag, Spin, Table
} from 'antd';
import {
  ArrowLeftOutlined, EnvironmentOutlined, FileTextOutlined, EditOutlined,
  DeleteOutlined, PlusOutlined, MinusCircleOutlined
} from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderAPI, productAPI } from '../api/axios';

const { Text } = Typography;

const STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING', label: 'Đang giao hàng' },
  { value: 'DELIVERED', label: 'Đã giao hàng' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

const STATUS_MAP = {
  PENDING: { color: 'default', text: 'Chờ xác nhận' },
  CONFIRMED: { color: 'blue', text: 'Đã xác nhận' },
  SHIPPING: { color: 'cyan', text: 'Đang giao hàng' },
  DELIVERED: { color: 'success', text: 'Đã giao hàng' },
  CANCELLED: { color: 'error', text: 'Đã hủy' },
};

export default function OrderDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editAddress, setEditAddress] = useState('');
  const [editNote, setEditNote] = useState('');
  const [editItems, setEditItems] = useState([]);
  const [savingEdit, setSavingEdit] = useState(false);

  const { data: order, isLoading } = useQuery({
    queryKey: ['order', id],
    queryFn: () => orderAPI.getById(id).then(res => res.data),
    onError: () => {
      message.error('Không tìm thấy đơn hàng');
      navigate('/orders');
    },
  });

  const { data: allProducts = [] } = useQuery({
    queryKey: ['products'],
    queryFn: () => productAPI.getAll().then(res => Array.isArray(res.data) ? res.data : []),
  });

  const statusMutation = useMutation({
    mutationFn: (newStatus) => {
      return orderAPI.updateStatus(order.orderId, { status: newStatus });
    },
    onSuccess: () => {
      message.success('Đã cập nhật trạng thái đơn hàng');
      queryClient.invalidateQueries({ queryKey: ['order', id] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Lỗi');
    },
  });

  const updateMutation = useMutation({
    mutationFn: (payload) => orderAPI.update(order.orderId, payload),
    onSuccess: () => {
      message.success('Đã cập nhật đơn hàng');
      setEditModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['order', id] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || 'Cập nhật thất bại');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => orderAPI.delete(order.orderId),
    onSuccess: () => {
      message.success('Đã xóa đơn hàng');
      navigate('/orders');
    },
    onError: (err) => {
      message.error(err.response?.data?.message || 'Xóa thất bại');
    },
  });

  const statusInfo = STATUS_MAP[order?.status] || { color: 'default', text: order?.status };

  const handleStatusChange = (newStatus) => {
    statusMutation.mutate(newStatus);
  };

  const openEditModal = () => {
    setEditAddress(order.shippingAddress || '');
    setEditNote(order.note || '');
    setEditItems((order.items || []).map((item) => ({
      productId: item.product?.id || item.productId,
      productName: item.product?.name || item.productName,
      quantity: item.quantity,
    })));
    setEditModalOpen(true);
  };

  const handleSaveEdit = async () => {
    setSavingEdit(true);
    try {
      const payload = { shippingAddress: editAddress, note: editNote };
      if (editItems.length > 0) {
        payload.items = editItems.map((item) => ({ productId: item.productId, quantity: item.quantity }));
      }
      updateMutation.mutate(payload);
    } catch {
      message.error('Cập nhật thất bại');
    } finally {
      setSavingEdit(false);
    }
  };

  const handleDeleteOrder = () => {
    Modal.confirm({
      title: 'Xóa đơn hàng',
      content: 'Bạn có chắc muốn xóa đơn hàng này?',
      okText: 'Xóa',
      okType: 'danger',
      cancelText: 'Hủy',
      onOk: () => deleteMutation.mutate(),
    });
  };

  if (isLoading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;
  if (!order) return null;

  const imgSrc = (img) => {
    if (!img) return 'https://placehold.co/48x48';
    if (img.startsWith('http://') || img.startsWith('https://') || img.startsWith('/')) return img;
    return `/uploads/${img}`;
  };

  const formatPrice = (v) => (v || 0).toLocaleString('vi-VN') + '₫';

  const itemColumns = [
    {
      title: 'Sản phẩm', key: 'product', width: 300,
      render: (_, item) => (
        <Space>
          <img src={imgSrc(item.product?.img)} alt={item.product?.name}
            style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 6 }} />
          <Text strong>{item.product?.name}</Text>
        </Space>
      ),
    },
    { title: 'Đơn giá', dataIndex: 'unitPrice', key: 'unitPrice', render: formatPrice, width: 120 },
    { title: 'Số lượng', dataIndex: 'quantity', key: 'quantity', width: 80 },
    {
      title: 'Kho hàng', key: 'warehouse', width: 180,
      render: (_, item) => item.warehouseName || <Text type="secondary">—</Text>,
    },
    { title: 'Thành tiền', dataIndex: 'lineTotal', key: 'lineTotal', render: (v) => <Text strong>{formatPrice(v)}</Text>, width: 120 },
  ];

  return (
    <div>
      <Space style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/orders')}>Quay lại</Button>
      </Space>

      <Descriptions
        title={`Đơn hàng #${order.orderId}`}
        column={{ xs: 1, sm: 2, md: 3 }}
        bordered
        size="small"
        style={{ marginBottom: 24 }}
      >
        <Descriptions.Item label="Khách hàng">{order.user?.name}</Descriptions.Item>
        <Descriptions.Item label="Email">{order.user?.email}</Descriptions.Item>
        <Descriptions.Item label="Điện thoại">{order.user?.phone}</Descriptions.Item>
        <Descriptions.Item label="Số lượng">{order.totalQuantity} sản phẩm</Descriptions.Item>
        <Descriptions.Item label="Tổng tiền"><Text strong>{formatPrice(order.totalPrice)}</Text></Descriptions.Item>
        <Descriptions.Item label="Phí ship">{order.shippingFee > 0 ? formatPrice(order.shippingFee) : <Text type="secondary">—</Text>}</Descriptions.Item>
        <Descriptions.Item label="Trạng thái">
          <Tag color={statusInfo.color}>{statusInfo.text}</Tag>
        </Descriptions.Item>
      </Descriptions>

      <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
        <Descriptions.Item label={<><EnvironmentOutlined /> Địa chỉ giao hàng</>}>
          {order.shippingAddress || <Text type="secondary">—</Text>}
        </Descriptions.Item>
        <Descriptions.Item label={<><FileTextOutlined /> Ghi chú</>}>
          <Text style={{ fontStyle: 'italic' }}>{order.note || <Text type="secondary">—</Text>}</Text>
        </Descriptions.Item>
      </Descriptions>

      <Space style={{ marginBottom: 16 }}>
        <Text strong>Cập nhật trạng thái: </Text>
        <Select
          value={order.status}
          style={{ width: 180 }}
          onChange={handleStatusChange}
          options={STATUS_OPTIONS}
        />
      </Space>

      {order.status === 'PENDING' && (
        <Space style={{ marginBottom: 16 }}>
          <Button icon={<EditOutlined />} onClick={openEditModal}>Sửa đơn hàng</Button>
          <Button danger icon={<DeleteOutlined />} onClick={handleDeleteOrder}>Xóa đơn hàng</Button>
        </Space>
      )}

      <Divider />
      <h3>Chi tiết đơn hàng ({order.items?.length || 0} sản phẩm)</h3>
      <Table
        rowKey={(_, idx) => idx}
        columns={itemColumns}
        dataSource={order.items || []}
        pagination={false}
        bordered
        size="small"
        style={{ marginTop: 8 }}
      />

      {/* Edit order modal */}
      <Modal
        title={`Sửa đơn hàng #${order.orderId}`}
        open={editModalOpen}
        onOk={handleSaveEdit}
        onCancel={() => setEditModalOpen(false)}
        confirmLoading={savingEdit}
        okText="Lưu"
        width={600}
      >
        <Space direction="vertical" style={{ width: '100%' }} size={12}>
          <div>
            <Text strong>Địa chỉ giao hàng</Text>
            <Input.TextArea value={editAddress} onChange={(e) => setEditAddress(e.target.value)} rows={2} style={{ marginTop: 4 }} />
          </div>
          <div>
            <Text strong>Ghi chú</Text>
            <Input.TextArea value={editNote} onChange={(e) => setEditNote(e.target.value)} rows={2} style={{ marginTop: 4 }} />
          </div>
          <Divider />
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Text strong>Sản phẩm ({editItems.length})</Text>
            <Button size="small" icon={<PlusOutlined />} onClick={() => {
              const product = allProducts.find((p) => !editItems.find((ei) => ei.productId === p.id));
              if (product) {
                setEditItems((prev) => [...prev, { productId: product.id, productName: product.name, quantity: 1 }]);
              } else {
                message.info('Tất cả sản phẩm đã có trong đơn hàng');
              }
            }}>Thêm sản phẩm</Button>
          </div>
          {editItems.map((item, idx) => (
            <div key={idx} style={{ display: 'flex', gap: 8, alignItems: 'center', padding: '8px 12px', background: '#fafafa', borderRadius: 6 }}>
              <Select
                showSearch style={{ flex: 1 }} size="small"
                value={item.productId}
                onChange={(val) => {
                  const prod = allProducts.find((p) => p.id === val);
                  setEditItems((prev) => prev.map((ei, i) => i === idx ? { ...ei, productId: val, productName: prod?.name || '' } : ei));
                }}
                filterOption={(input, option) => (option?.label || '').toLowerCase().includes(input.toLowerCase())}
                options={allProducts.filter((p) => p.id === item.productId || !editItems.find((ei) => ei.productId === p.id)).map((p) => ({ label: p.name, value: p.id }))}
              />
              <InputNumber size="small" min={1} max={999} value={item.quantity}
                onChange={(val) => setEditItems((prev) => prev.map((ei, i) => i === idx ? { ...ei, quantity: val || 1 } : ei))}
                style={{ width: 70 }} />
              <Button type="text" danger icon={<MinusCircleOutlined />} onClick={() => setEditItems((prev) => prev.filter((_, i) => i !== idx))} />
            </div>
          ))}
        </Space>
      </Modal>
    </div>
  );
}
