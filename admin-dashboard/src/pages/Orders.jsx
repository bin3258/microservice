import { useNavigate } from 'react-router-dom';
import { Table, Button, Select, message } from 'antd';
import { EyeOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { orderAPI } from '../api/axios';

const STATUS_OPTIONS = [
  { value: 'PENDING', label: 'Chờ xác nhận' },
  { value: 'CONFIRMED', label: 'Đã xác nhận' },
  { value: 'SHIPPING', label: 'Đang giao hàng' },
  { value: 'DELIVERED', label: 'Đã giao hàng' },
  { value: 'CANCELLED', label: 'Đã hủy' },
];

export default function Orders() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['orders'],
    queryFn: () => orderAPI.getAll().then(res => Array.isArray(res.data) ? res.data : []),
  });

  const statusMutation = useMutation({
    mutationFn: ({ orderId, newStatus }) => {
      return orderAPI.updateStatus(orderId, { status: newStatus });
    },
    onSuccess: () => {
      message.success('Đã cập nhật trạng thái đơn hàng');
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Lỗi');
    },
  });

  const showDetail = (record) => {
    navigate(`/orders/${record.orderId}`);
  };

  const handleStatusChange = (orderId, newStatus) => {
    statusMutation.mutate({ orderId, newStatus });
  };

  const columns = [
    { title: 'ID', dataIndex: 'orderId', key: 'orderId', width: 60 },
    { title: 'Người dùng', key: 'user', render: (_, r) => r.user?.name },
    { title: 'Email', key: 'email', render: (_, r) => r.user?.email },
    { title: 'Điện thoại', key: 'phone', render: (_, r) => r.user?.phone },
    { title: 'SL', dataIndex: 'totalQuantity', key: 'totalQuantity', width: 60 },
    { title: 'Tổng', dataIndex: 'totalPrice', key: 'totalPrice', render: (v) => (v || 0).toLocaleString('vi-VN') + '₫', width: 100 },
    { title: 'Phí ship', dataIndex: 'shippingFee', key: 'shippingFee', render: (v) => v > 0 ? v.toLocaleString('vi-VN') + '₫' : '—', width: 80 },
    {
      title: 'Trạng thái', key: 'status', width: 180,
      render: (_, r) => (
        <Select
          size="small"
          value={r.status}
          style={{ width: 150 }}
          onChange={(v) => handleStatusChange(r.orderId, v)}
          options={STATUS_OPTIONS}
        />
      ),
    },
    {
      title: 'Thao tác', key: 'actions', width: 120,
      render: (_, r) => (
        <Button icon={<EyeOutlined />} onClick={() => showDetail(r)} size="small">Xem</Button>
      ),
    },
  ];

  return (
    <>
      <h2 style={{ marginBottom: 16 }}>Đơn hàng</h2>
      <Table rowKey="orderId" columns={columns} dataSource={dataSource} loading={isLoading} />
    </>
  );
}
