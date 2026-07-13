import { useState } from 'react';
import { Table, Button, Modal, Form, Input, Select, Tag, Space, message, Popconfirm, Upload } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { postAPI, postCategoryAPI } from '../api/axios';

export default function Posts() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [fileList, setFileList] = useState([]);
  const [form] = Form.useForm();

  const getImageSrc = (v) => {
    if (!v) return null;
    if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
    return `/uploads/${v}`;
  };

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['posts'],
    queryFn: () => postAPI.getAll().then(res => res.data),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['postCategories'],
    queryFn: () => postCategoryAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (formData) => postAPI.create(formData),
    onSuccess: () => {
      message.success('Đã thêm bài viết');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, formData }) => postAPI.update(id, formData),
    onSuccess: () => {
      message.success('Đã cập nhật bài viết');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => postAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa bài viết');
      queryClient.invalidateQueries({ queryKey: ['posts'] });
    },
  });

  const openCreate = () => {
    setEditing(null);
    form.resetFields();
    setFileList([]);
    setModalOpen(true);
  };

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue(record);
    setFileList([]);
    setModalOpen(true);
  };

  const handleOk = async () => {
    const values = await form.validateFields();
    const formData = new FormData();
    formData.append('title', values.title);
    formData.append('content', values.content);
    formData.append('categoryId', values.categoryId);
    formData.append('status', values.status);
    if (values.img) {
      formData.append('img', values.img);
    }
    if (fileList.length > 0) {
      formData.append('imgFile', fileList[0].originFileObj || fileList[0]);
    }
    if (editing) {
      updateMutation.mutate({ id: editing.id, formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tiêu đề', dataIndex: 'title', key: 'title' },
    { title: 'Hình ảnh', dataIndex: 'img', key: 'img', render: (v) => v ? <img src={getImageSrc(v)} alt="" style={{ height: 40 }} /> : '-' },
    {
      title: 'Trạng thái', dataIndex: 'status', key: 'status',
      render: (s) => <Tag color={s === 'PUBLISHED' ? 'green' : 'orange'}>{s === 'PUBLISHED' ? 'Đã xuất bản' : 'Bản nháp'}</Tag>,
    },
    { title: 'Danh mục', dataIndex: 'categoryName', key: 'categoryName' },
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
        <h2>Bài viết</h2>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm bài viết</Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} />
      <Modal
        title={editing ? 'Sửa bài viết' : 'Thêm bài viết'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
        width={600}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="Tiêu đề" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="content" label="Nội dung" rules={[{ required: true }]}>
            <Input.TextArea rows={6} />
          </Form.Item>
          <Form.Item label="Hình ảnh">
            <Upload
              beforeUpload={(file) => {
                setFileList([file]);
                return false;
              }}
              onRemove={() => setFileList([])}
              fileList={fileList}
              maxCount={1}
              listType="picture"
            >
              {fileList.length < 1 && <Button icon={<UploadOutlined />}>Chọn ảnh</Button>}
            </Upload>
            {editing && editing.img && fileList.length === 0 && (
              <div style={{ marginTop: 8 }}>
                <span>Hiện tại: </span>
                <img src={getImageSrc(editing.img)} alt="" style={{ height: 60 }} />
              </div>
            )}
          </Form.Item>
          <Form.Item name="categoryId" label="Danh mục" rules={[{ required: true }]}>
            <Select>
              {categories.map((c) => (
                <Select.Option key={c.id} value={c.id}>{c.name}</Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="status" label="Trạng thái" rules={[{ required: true }]}>
            <Select>
              <Select.Option value="PUBLISHED">Đã xuất bản</Select.Option>
              <Select.Option value="DRAFT">Bản nháp</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
