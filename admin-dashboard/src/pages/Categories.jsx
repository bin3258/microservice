import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Space, message, Popconfirm } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FolderOutlined, FolderOpenOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { categoryAPI } from '../api/axios';

export default function Categories() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [categoryType, setCategoryType] = useState('parent');
  const [form] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (payload) => categoryAPI.create(payload),
    onSuccess: () => {
      message.success('Đã thêm danh mục');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, payload }) => categoryAPI.update(id, payload),
    onSuccess: () => {
      message.success('Đã cập nhật danh mục');
      setModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => categoryAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa danh mục');
      queryClient.invalidateQueries({ queryKey: ['categories'] });
    },
  });

  const openCreate = () => {
    setEditing(null);
    setCategoryType('parent');
    form.resetFields();
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    setCategoryType(record.parentId ? 'child' : 'parent');
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleOk = async () => {
    const values = await form.validateFields();
    const payload = {
      name: values.name,
      description: values.description || '',
      parentId: categoryType === 'child' ? (values.parentId || null) : null,
    };
    if (editing) {
      updateMutation.mutate({ id: editing.id, payload });
    } else {
      createMutation.mutate(payload);
    }
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const parents = dataSource.filter(c => !c.parentId);
  const getChildren = (parentId) => dataSource.filter(c => c.parentId === parentId);

  const expandedRowRender = (parent) => {
    const children = getChildren(parent.id);
    if (children.length === 0) return null;
    return (
      <div>
        {children.map((child, i) => (
          <div key={child.id} style={{
            display: 'flex', alignItems: 'center', minHeight: 54, padding: '4px 0',
            borderBottom: i < children.length - 1 ? '1px solid #f0f0f0' : 'none',
          }}>
            <div style={{ width: 60, flexShrink: 0 }} />
            <div style={{ flex: 1, paddingLeft: 20 }}>{child.name}</div>
            <div style={{ flex: 1 }}>{child.description || '-'}</div>
            <div style={{ width: 140, flexShrink: 0, textAlign: 'center' }}>
              <Space>
                <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(child)} />
                <Popconfirm title="Xóa?" onConfirm={() => handleDelete(child.id)}>
                  <Button size="small" danger icon={<DeleteOutlined />} />
                </Popconfirm>
              </Space>
            </div>
          </div>
        ))}
      </div>
    );
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tên', dataIndex: 'name', key: 'name', render: (v, r) => r.parentId ? <span style={{ paddingLeft: 24 }}>{v}</span> : <strong>{v}</strong> },
    { title: 'Mô tả', dataIndex: 'description', key: 'description' },
    {
      title: 'Thao tác', key: 'actions', width: 140,
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
        <h2>Danh mục sản phẩm</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm danh mục</Button>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={parents}
        loading={isLoading}
        expandable={{
          expandedRowRender,
          rowExpandable: (r) => getChildren(r.id).length > 0,
        }}
      />
      <Modal
        title={editing ? 'Sửa danh mục' : 'Thêm danh mục'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Space style={{ marginBottom: 20 }}>
          <Button
            icon={<FolderOutlined />}
            type={categoryType === 'parent' ? 'primary' : 'default'}
            onClick={() => setCategoryType('parent')}
          >
            Danh mục cha
          </Button>
          <Button
            icon={<FolderOpenOutlined />}
            type={categoryType === 'child' ? 'primary' : 'default'}
            onClick={() => setCategoryType('child')}
          >
            Danh mục con
          </Button>
        </Space>
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} />
          </Form.Item>
          {categoryType === 'child' && (
            <Form.Item name="parentId" label="Danh mục cha" rules={[{ required: true, message: 'Vui lòng chọn danh mục cha' }]}>
              <Select placeholder="Chọn danh mục cha">
                {dataSource.filter(c => !c.parentId && c.id !== editing?.id).map((c) => (
                  <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>
                ))}
              </Select>
            </Form.Item>
          )}
        </Form>
      </Modal>
    </>
  );
}
