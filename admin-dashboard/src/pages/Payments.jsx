import { Table, Tag, Select, message } from 'antd';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentAPI } from '../api/axios';

const statuses = [
  'PROCESSING',
  'CONFIRMED',
  'SHIPPED',
  'DELIVERED',
  'CANCELLED',
  'REFUNDED',
  'FAILED',
];

const statusLabels = {
  PROCESSING: 'Đang xử lý',
  CONFIRMED: 'Đã xác nhận',
  SHIPPED: 'Đã gửi hàng',
  DELIVERED: 'Đã giao hàng',
  CANCELLED: 'Đã hủy',
  REFUNDED: 'Đã hoàn tiền',
  FAILED: 'Thất bại',
};

const statusColors = {
  PROCESSING: 'orange',
  CONFIRMED: 'blue',
  SHIPPED: 'purple',
  DELIVERED: 'green',
  CANCELLED: 'red',
  REFUNDED: 'cyan',
  FAILED: 'red',
};

export default function Payments() {
  const queryClient = useQueryClient();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['payments'],
    queryFn: () => paymentAPI.getAll().then(res => res.data),
  });

  const statusMutation = useMutation({
    mutationFn: ({ id, status }) => paymentAPI.updateStatus(id, { status }),
    onSuccess: () => {
      message.success('Đã cập nhật trạng thái');
      queryClient.invalidateQueries({ queryKey: ['payments'] });
    },
    onError: (err) => {
      const msg = err.response?.data?.message || err.message || 'Lỗi';
      message.error(msg);
    },
  });

  const handleStatusChange = (id, newStatus) => {
    statusMutation.mutate({ id, status: newStatus });
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Đơn hàng', dataIndex: 'orderId', key: 'orderId' },
    { title: 'Số tiền', dataIndex: 'amount', key: 'amount', render: (v) => (v || 0).toLocaleString('vi-VN') + '₫' },
    {
      title: 'Trạng thái', dataIndex: 'status', key: 'status',
      render: (s) => <Tag color={statusColors[s] || 'default'}>{statusLabels[s] || s}</Tag>,
    },
    {
      title: 'Cập nhật', key: 'action',
      render: (_, r) => (
        <Select
          size="small"
          style={{ width: 150 }}
          value={r.status}
          onChange={(v) => handleStatusChange(r.id, v)}
          options={statuses.map((s) => ({ label: statusLabels[s], value: s }))}
        />
      ),
    },
    { title: 'Mã GD', dataIndex: 'transactionId', key: 'transactionId' },
    { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt' },
  ];

  return (
    <>
      <h2 style={{ marginBottom: 16 }}>Thanh toán</h2>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />
    </>
  );
}
