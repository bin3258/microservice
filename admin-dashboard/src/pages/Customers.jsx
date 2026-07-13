import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Space, message, Popconfirm, Checkbox, Tag } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, EnvironmentOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { customerAPI, addressAPI, authAPI } from '../api/axios';

export default function Customers() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  // Address state
  const [addrModalOpen, setAddrModalOpen] = useState(false);
  const [addrCustomer, setAddrCustomer] = useState(null);
  const [addrFormOpen, setAddrFormOpen] = useState(false);
  const [addrEditing, setAddrEditing] = useState(null);
  const [addrForm] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['customers'],
    queryFn: () => customerAPI.getAll().then(res => res.data),
  });

  const { data: addresses = [], isLoading: addrLoading } = useQuery({
    queryKey: ['addresses', addrCustomer?.id],
    queryFn: () => addressAPI.getByCustomer(addrCustomer.id).then(res => Array.isArray(res.data) ? res.data : []),
    enabled: !!addrCustomer,
  });

  const createMutation = useMutation({
    mutationFn: (values) => customerAPI.create(values),
    onSuccess: () => {
      message.success('Đã thêm khách hàng');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Lỗi');
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, values, editing }) => {
      await customerAPI.update(id, { fullName: values.fullName, phone: values.phone, email: values.email });
      if (values.password && values.password.trim().length >= 6) {
        await authAPI.changePassword(editing.authUserId, { password: values.password });
      }
    },
    onSuccess: () => {
      message.success('Đã cập nhật khách hàng');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Lỗi');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => customerAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa khách hàng');
      queryClient.invalidateQueries({ queryKey: ['customers'] });
    },
  });

  // Address mutations
  const addrCreateMutation = useMutation({
    mutationFn: (values) => addressAPI.create(values),
    onSuccess: () => {
      message.success('Đã thêm địa chỉ');
      setAddrFormOpen(false);
      queryClient.invalidateQueries({ queryKey: ['addresses', addrCustomer?.id] });
    },
  });

  const addrUpdateMutation = useMutation({
    mutationFn: ({ id, values }) => addressAPI.update(id, values),
    onSuccess: () => {
      message.success('Đã cập nhật địa chỉ');
      setAddrFormOpen(false);
      queryClient.invalidateQueries({ queryKey: ['addresses', addrCustomer?.id] });
    },
  });

  const addrDeleteMutation = useMutation({
    mutationFn: (id) => addressAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa địa chỉ');
      queryClient.invalidateQueries({ queryKey: ['addresses', addrCustomer?.id] });
    },
  });

  const addrSetDefaultMutation = useMutation({
    mutationFn: ({ id, customerId }) => addressAPI.setDefault(id, customerId),
    onSuccess: () => {
      message.success('Đã đặt làm mặc định');
      queryClient.invalidateQueries({ queryKey: ['addresses', addrCustomer?.id] });
    },
  });

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleOk = async () => {
    try {
      const values = await form.validateFields();
      if (editing) {
        updateMutation.mutate({ id: editing.id, values, editing });
      } else {
        createMutation.mutate(values);
      }
    } catch (err) {
      message.error(err.response?.data?.message || err.message || 'Lỗi');
    }
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  // Address handlers
  const openAddresses = (record) => {
    setAddrCustomer(record);
    setAddrModalOpen(true);
  };

  const openAddrCreate = () => {
    setAddrEditing(null);
    addrForm.resetFields();
    setAddrFormOpen(true);
  };

  const openAddrEdit = (record) => {
    setAddrEditing(record);
    addrForm.setFieldsValue(record);
    setAddrFormOpen(true);
  };

  const handleAddrOk = async () => {
    const values = await addrForm.validateFields();
    if (addrEditing) {
      addrUpdateMutation.mutate({ id: addrEditing.id, values: { ...values, customerId: addrCustomer.id } });
    } else {
      addrCreateMutation.mutate({ ...values, customerId: addrCustomer.id });
    }
  };

  const handleAddrDelete = (id) => {
    addrDeleteMutation.mutate(id);
  };

  const handleSetDefault = (id) => {
    addrSetDefaultMutation.mutate({ id, customerId: addrCustomer.id });
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Họ tên', dataIndex: 'fullName', key: 'fullName' },
    { title: 'Điện thoại', dataIndex: 'phone', key: 'phone' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'ID người dùng', dataIndex: 'authUserId', key: 'authUserId' },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => openEdit(r)} />
          <Button icon={<EnvironmentOutlined />} onClick={() => openAddresses(r)}>Địa chỉ</Button>
          <Popconfirm title="Xóa?" onConfirm={() => handleDelete(r.id)}>
            <Button danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const addrColumns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Đường', dataIndex: 'street', key: 'street' },
    { title: 'Phường/Xã', dataIndex: 'ward', key: 'ward' },
    { title: 'Thành phố', dataIndex: 'city', key: 'city' },
    {
      title: 'Mặc định', key: 'default',
      render: (_, r) => r['default']
        ? <Tag color="green">Mặc định</Tag>
        : <Button size="small" onClick={() => handleSetDefault(r.id)}>Đặt mặc định</Button>,
    },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openAddrEdit(r)} />
          <Popconfirm title="Xóa?" onConfirm={() => handleAddrDelete(r.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Khách hàng</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm khách hàng</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />

      {/* Customer create/edit modal */}
      <Modal
        title={editing ? 'Sửa khách hàng' : 'Thêm khách hàng'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="fullName" label="Họ tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Điện thoại" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="password" label={editing ? 'Mật khẩu mới (để trống nếu không đổi)' : 'Mật khẩu'}
            rules={editing ? [] : [{ required: true }]}
          >
            <Input.Password />
          </Form.Item>
        </Form>
      </Modal>

      {/* Address management drawer/modal */}
      <Modal
        title={`Địa chỉ - ${addrCustomer?.fullName || ''}`}
        open={addrModalOpen}
        onCancel={() => setAddrModalOpen(false)}
        footer={[
          <Button key="add" type="primary" icon={<PlusOutlined />} onClick={openAddrCreate}>Thêm địa chỉ</Button>,
        ]}
        width={800}
      >
        <Table
          rowKey="id"
          columns={addrColumns}
          dataSource={addresses}
          loading={addrLoading}
          size="small"
        />

        {/* Address create/edit form inside drawer */}
        <Modal
          title={addrEditing ? 'Sửa địa chỉ' : 'Thêm địa chỉ'}
          open={addrFormOpen}
          onOk={handleAddrOk}
          onCancel={() => setAddrFormOpen(false)}
        >
          <Form form={addrForm} layout="vertical">
            <Form.Item name="street" label="Đường" rules={[{ required: true }]}>
              <Input placeholder="Số nhà, tên đường" />
            </Form.Item>
            <Form.Item name="ward" label="Phường/Xã">
              <Input />
            </Form.Item>
            <Form.Item name="city" label="Thành phố" rules={[{ required: true }]}>
              <Input />
            </Form.Item>
            <Form.Item name="default" label="Mặc định" valuePropName="checked">
              <Checkbox>Đặt làm địa chỉ mặc định</Checkbox>
            </Form.Item>
          </Form>
        </Modal>
      </Modal>
    </>
  );
}
