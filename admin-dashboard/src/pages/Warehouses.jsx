import { useState, useEffect } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { warehouseAPI } from '../api/axios';

export default function Warehouses() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    if (modalOpen) {
      if (editing) {
        form.setFieldsValue(editing);
      } else {
        form.resetFields();
      }
    }
  }, [modalOpen]);

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['warehouses'],
    queryFn: () => warehouseAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (values) => warehouseAPI.create(values),
    onSuccess: () => {
      message.success('Đã tạo kho mới');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }) => warehouseAPI.update(id, values),
    onSuccess: () => {
      message.success('Đã cập nhật kho');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => warehouseAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa kho');
      queryClient.invalidateQueries({ queryKey: ['warehouses'] });
    },
  });

  const openCreate = () => {
    setEditing(null);
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    setModalOpen(true);
  };

  const handleOk = async () => {
    const values = await form.validateFields();
    if (editing) {
      updateMutation.mutate({ id: editing.id, values });
    } else {
      createMutation.mutate(values);
    }
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tên kho', dataIndex: 'name', key: 'name' },
    { title: 'Địa chỉ', dataIndex: 'address', key: 'address', render: (v) => v || '—' },
    { title: 'Vĩ độ', dataIndex: 'latitude', key: 'latitude', render: (v) => v ?? '—', width: 100 },
    { title: 'Kinh độ', dataIndex: 'longitude', key: 'longitude', render: (v) => v ?? '—', width: 100 },
    {
      title: 'Thao tác', key: 'actions', width: 120,
      render: (_, r) => (
        <Space>
          <Button icon={<EditOutlined />} size="small" onClick={() => openEdit(r)} />
          <Popconfirm title="Xóa kho?" description="Sản phẩm trong kho sẽ không bị xóa." onConfirm={() => handleDelete(r.id)} okText="Xóa" cancelText="Hủy">
            <Button danger icon={<DeleteOutlined />} size="small" />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Kho hàng</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm kho</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />
      <Modal
        title={editing ? 'Sửa kho' : 'Thêm kho'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên kho" rules={[{ required: true, message: 'Vui lòng nhập tên kho' }]}>
            <Input placeholder="VD: Kho Hà Nội" />
          </Form.Item>
          <Form.Item name="address" label="Địa chỉ">
            <Input placeholder="Địa chỉ kho (không bắt buộc)" />
          </Form.Item>
          <Form.Item name="latitude" label="Vĩ độ">
            <InputNumber style={{ width: '100%' }} placeholder="VD: 10.78" />
          </Form.Item>
          <Form.Item name="longitude" label="Kinh độ">
            <InputNumber style={{ width: '100%' }} placeholder="VD: 106.70" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
