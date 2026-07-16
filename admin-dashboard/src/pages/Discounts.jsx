import { useState, useMemo } from 'react';
import { Table, Button, Modal, Form, Input, Select, InputNumber, DatePicker, Switch, Space, message, Popconfirm, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UserOutlined, SearchOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { discountAPI, userAPI } from '../api/axios';
import dayjs from 'dayjs';

export default function Discounts() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [userModalOpen, setUserModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [selectedDiscount, setSelectedDiscount] = useState(null);
  const [selectedUserIds, setSelectedUserIds] = useState([]);
  const [searchEmail, setSearchEmail] = useState('');
  const [searchPhone, setSearchPhone] = useState('');
  const [form] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['discounts'],
    queryFn: () => discountAPI.getAll().then(res => res.data),
  });

  const { data: users = [] } = useQuery({
    queryKey: ['users', 'CUSTOMER'],
    queryFn: () => userAPI.getByRole('CUSTOMER').then(res => res.data),
  });

  const filteredUsers = useMemo(() => {
    return users.filter(u => {
      const matchEmail = !searchEmail || (u.email || '').toLowerCase().includes(searchEmail.toLowerCase());
      const matchPhone = !searchPhone || (u.phone || '').toLowerCase().includes(searchPhone.toLowerCase());
      return matchEmail && matchPhone;
    });
  }, [users, searchEmail, searchPhone]);

  const createMutation = useMutation({
    mutationFn: (payload) => discountAPI.create(payload),
    onSuccess: () => {
      message.success('Đã thêm mã giảm giá');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
    onError: (err) => message.error(err.response?.data?.message || err.message),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => discountAPI.update(id, payload),
    onSuccess: () => {
      message.success('Đã cập nhật mã giảm giá');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
    onError: (err) => message.error(err.response?.data?.message || err.message),
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => discountAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa mã giảm giá');
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
  });

  const assignUsersMutation = useMutation({
    mutationFn: ({ id, userIds }) => discountAPI.setUsers(id, userIds),
    onSuccess: () => {
      message.success('Đã cập nhật người dùng được áp dụng');
      setUserModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['discounts'] });
    },
    onError: (err) => message.error(err.response?.data?.message || err.message),
  });

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    form.setFieldsValue({ type: 'PRODUCT', isActive: true, minOrderValue: 0 });
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      ...record,
      startDate: record.startDate ? dayjs(record.startDate, 'YYYY-MM-DD HH:mm:ss') : null,
      endDate: record.endDate ? dayjs(record.endDate, 'YYYY-MM-DD HH:mm:ss') : null,
    });
    setModalOpen(true);
  };

  const openUserModal = async (record) => {
    setSelectedDiscount(record);
    try {
      const res = await discountAPI.getUsers(record.id);
      const userIds = res.data.map(u => u.userId);
      setSelectedUserIds(userIds);
    } catch {
      setSelectedUserIds([]);
    }
    setUserModalOpen(true);
  };

  const handleOk = async () => {
    const values = await form.validateFields();
    const payload = {
      code: values.code,
      type: values.type,
      discountValue: values.discountValue,
      minOrderValue: values.minOrderValue || 0,
      usageLimit: values.usageLimit || null,
      startDate: values.startDate ? values.startDate.format('YYYY-MM-DD HH:mm:ss') : null,
      endDate: values.endDate ? values.endDate.format('YYYY-MM-DD HH:mm:ss') : null,
      isActive: values.isActive,
      description: values.description || '',
    };
    if (editing) {
      updateMutation.mutate({ id: editing.id, payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  const handleAssignUsers = async () => {
    assignUsersMutation.mutate({ id: selectedDiscount.id, userIds: selectedUserIds });
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Mã', dataIndex: 'code', key: 'code', width: 120 },
    {
      title: 'Loại', dataIndex: 'type', key: 'type', width: 100,
      render: (v) => v === 'SHIPPING' ? <Tag color="blue">Giảm ship</Tag> : <Tag color="green">Giảm SP</Tag>,
    },
    {
      title: 'Giá trị', dataIndex: 'discountValue', key: 'discountValue', width: 100,
      render: (v) => v?.toLocaleString('vi-VN') + '₫',
    },
    {
      title: 'Đơn tối thiểu', dataIndex: 'minOrderValue', key: 'minOrderValue', width: 100,
      render: (v) => v > 0 ? v.toLocaleString('vi-VN') + '₫' : '—',
    },
    {
      title: 'Đã dùng/Lượt', key: 'usage', width: 100,
      render: (_, r) => r.usageLimit ? `${r.usedCount || 0}/${r.usageLimit}` : `${r.usedCount || 0}/∞`,
    },
    {
      title: 'Áp dụng', key: 'assignedToAll', width: 100,
      render: (_, r) => r.assignedToAll ? <Tag color="green">Tất cả</Tag> : <Tag color="orange">Cá nhân</Tag>,
    },
    {
      title: 'Kích hoạt', dataIndex: 'isActive', key: 'isActive', width: 80,
      render: (v) => v ? <Tag color="green">Có</Tag> : <Tag color="red">Không</Tag>,
    },
    {
      title: 'Mô tả', dataIndex: 'description', key: 'description',
      ellipsis: true,
    },
    {
      title: 'Thao tác', key: 'actions', width: 180,
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<UserOutlined />} onClick={() => openUserModal(r)}>Gán user</Button>
          <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)} />
          <Popconfirm title="Xóa?" onConfirm={() => handleDelete(r.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Mã giảm giá</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm mã</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} scroll={{ x: 1000 }} />

      <Modal
        title={editing ? 'Sửa mã giảm giá' : 'Thêm mã giảm giá'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="code" label="Mã giảm giá" rules={[{ required: true, message: 'Vui lòng nhập mã' }]}>
            <Input placeholder="VD: GIAM20" style={{ textTransform: 'uppercase' }} />
          </Form.Item>
          <Form.Item name="type" label="Loại" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="PRODUCT">Giảm giá sản phẩm</Select.Option>
              <Select.Option value="SHIPPING">Giảm giá ship</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item name="discountValue" label="Giá trị giảm (₫)" rules={[{ required: true, message: 'Vui lòng nhập số tiền giảm' }]}>
            <InputNumber min={1000} step={1000} style={{ width: '100%' }} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
          <Form.Item name="minOrderValue" label="Giá trị đơn hàng tối thiểu (₫)">
            <InputNumber min={0} step={10000} style={{ width: '100%' }} formatter={(v) => `${v}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')} />
          </Form.Item>
          <Form.Item name="usageLimit" label="Giới hạn lượt sử dụng (để trống nếu không giới hạn)">
            <InputNumber min={1} style={{ width: '100%' }} />
          </Form.Item>
          <Space style={{ width: '100%' }}>
            <Form.Item name="startDate" label="Ngày bắt đầu">
              <DatePicker showTime format="YYYY-MM-DD HH:mm:ss" />
            </Form.Item>
            <Form.Item name="endDate" label="Ngày kết thúc">
              <DatePicker showTime format="YYYY-MM-DD HH:mm:ss" />
            </Form.Item>
          </Space>
          <Form.Item name="isActive" label="Kích hoạt" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={2} />
          </Form.Item>
        </Form>
      </Modal>

      <Modal
        title={`Gán người dùng cho mã "${selectedDiscount?.code}"`}
        open={userModalOpen}
        onOk={handleAssignUsers}
        onCancel={() => setUserModalOpen(false)}
        width={500}
      >
        <p style={{ marginBottom: 12, color: '#888' }}>Chọn người dùng được phép sử dụng mã này. Nếu không chọn ai, mã áp dụng cho tất cả.</p>
        <Space direction="vertical" style={{ width: '100%' }} size={8}>
          <Space.Compact style={{ width: '100%' }}>
            <Input prefix={<SearchOutlined />} placeholder="Tìm email..." value={searchEmail} onChange={e => setSearchEmail(e.target.value)} allowClear />
            <Input placeholder="Tìm SDT..." value={searchPhone} onChange={e => setSearchPhone(e.target.value)} allowClear />
          </Space.Compact>
          <Select
            mode="multiple"
            style={{ width: '100%' }}
            placeholder="Chọn người dùng..."
            value={selectedUserIds}
            onChange={setSelectedUserIds}
          >
            {filteredUsers.map(u => (
              <Select.Option key={u.id} value={u.id}>{u.name}{u.phone ? ` - ${u.phone}` : ''} ({u.email})</Select.Option>
            ))}
          </Select>
        </Space>
      </Modal>
    </>
  );
}
