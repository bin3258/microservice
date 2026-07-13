import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { userAPI, authAPI } from '../api/axios';

const roleColors = { ADMIN: 'red', MANAGER: 'blue' };
const roles = ['ADMIN', 'MANAGER'];
const roleLabels = { ADMIN: 'Quản trị viên', MANAGER: 'Quản lý' };

export default function Users() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: () => userAPI.getAll().then(res => res.data.filter(u => u.role === 'ADMIN' || u.role === 'MANAGER')),
  });

  const createMutation = useMutation({
    mutationFn: (values) => authAPI.register({ username: values.email, password: values.password, email: values.email, name: values.name, phone: values.phone, role: values.role }),
    onSuccess: () => {
      message.success('Đã thêm người dùng');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Có lỗi xảy ra');
    },
  });

  const updateMutation = useMutation({
    mutationFn: async ({ id, values, editing }) => {
      await userAPI.update(id, { name: values.name, email: values.email, phone: values.phone, role: editing.role });
      if (values.password) {
        await authAPI.changePassword(editing.authUserId || id, { password: values.password });
      }
    },
    onSuccess: () => {
      message.success('Đã cập nhật người dùng');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
    onError: (err) => {
      message.error(err.response?.data?.message || err.message || 'Có lỗi xảy ra');
    },
  });

  const deleteMutation = useMutation({
    mutationFn: async (id) => {
      await userAPI.delete(id);
      await authAPI.deleteUser(id).catch(() => {});
    },
    onSuccess: () => {
      message.success('Đã xóa người dùng');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const roleMutation = useMutation({
    mutationFn: async ({ id, role }) => {
      await userAPI.updateRole(id, role);
      await authAPI.changeRole(id, role).catch(() => {});
    },
    onSuccess: () => {
      message.success('Đã cập nhật vai trò');
      queryClient.invalidateQueries({ queryKey: ['users'] });
    },
  });

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({ name: record.name, email: record.email, phone: record.phone });
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
      message.error(err.response?.data?.message || err.message || 'Có lỗi xảy ra');
    }
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const handleRoleChange = (id, role) => {
    roleMutation.mutate({ id, role });
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Email', dataIndex: 'email', key: 'email' },
    { title: 'Điện thoại', dataIndex: 'phone', key: 'phone' },
    {
      title: 'Vai trò', dataIndex: 'role', key: 'role',
      render: (r, record) => (
        <Select
          value={r}
          onChange={(val) => handleRoleChange(record.id, val)}
          size="small"
          style={{ width: 130 }}
        >
          {roles.map((role) => (
            <Select.Option key={role} value={role}>
              <Tag color={roleColors[role]}>{roleLabels[role]}</Tag>
            </Select.Option>
          ))}
        </Select>
      ),
    },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => openEdit(r)} />
          <Popconfirm title="Xóa?" onConfirm={() => handleDelete(r.id)}>
            <Button danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
        <h2>Người dùng</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm người dùng</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />
      <Modal
        title={editing ? 'Sửa người dùng' : 'Thêm người dùng'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="email" label="Email" rules={[{ required: true, type: 'email' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="phone" label="Điện thoại" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          {!editing && (
            <>
              <Form.Item name="password" label="Mật khẩu" rules={[{ required: true, min: 6 }]}>
                <Input.Password />
              </Form.Item>
              <Form.Item name="role" label="Vai trò" rules={[{ required: true }]}>
                <Select>
                  {roles.map((role) => (
                    <Select.Option key={role} value={role}>{roleLabels[role]}</Select.Option>
                  ))}
                </Select>
              </Form.Item>
            </>
          )}
          {editing && (
            <Form.Item name="password" label="Mật khẩu mới (để trống nếu không đổi)">
              <Input.Password />
            </Form.Item>
          )}
        </Form>
      </Modal>
    </>
  );
}
