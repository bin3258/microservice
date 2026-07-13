import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postCategoryAPI } from '../api/axios';

export default function PostCategories() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['postCategories'],
    queryFn: () => postCategoryAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (values) => postCategoryAPI.create(values),
    onSuccess: () => {
      message.success('Đã thêm danh mục');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['postCategories'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, values }) => postCategoryAPI.update(id, values),
    onSuccess: () => {
      message.success('Đã cập nhật danh mục');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['postCategories'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => postCategoryAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa danh mục');
      queryClient.invalidateQueries({ queryKey: ['postCategories'] });
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
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
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
        <h2>Danh mục bài viết</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm danh mục</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />
      <Modal
        title={editing ? 'Sửa danh mục bài viết' : 'Thêm danh mục bài viết'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
