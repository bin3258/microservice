import { useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Switch, Space, message, Popconfirm, Upload } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined, ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../api/axios';

const bannerAPI = {
  getAll: () => api.get('/banners/admin'),
  create: (data) => api.post('/banners', data),
  update: (id, data) => api.put(`/banners/${id}`, data),
  delete: (id) => api.delete(`/banners/${id}`),
  reorder: (items) => api.put('/banners/reorder', { items }),
};

export default function Banners() {
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [fileList, setFileList] = useState([]);
  const [form] = Form.useForm();

  const { data: dataSource = [], isLoading } = useQuery({
    queryKey: ['banners'],
    queryFn: () => bannerAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (formData) => bannerAPI.create(formData),
    onSuccess: () => {
      message.success('Đã thêm banner');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['banners'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, formData }) => bannerAPI.update(id, formData),
    onSuccess: () => {
      message.success('Đã cập nhật banner');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['banners'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => bannerAPI.delete(id),
    onSuccess: () => {
      message.success('Đã xóa banner');
      queryClient.invalidateQueries({ queryKey: ['banners'] });
    },
  });

  const reorderMutation = useMutation({
    mutationFn: (items) => bannerAPI.reorder(items),
    onSuccess: () => {
      message.success('Đã lưu thứ tự');
      queryClient.invalidateQueries({ queryKey: ['banners'] });
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
    formData.append('title', values.title || '');
    formData.append('sortOrder', values.sortOrder ?? 0);
    formData.append('active', values.active ?? true);
    if (fileList.length > 0) {
      formData.append('imageFile', fileList[0].originFileObj || fileList[0]);
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

  const moveUp = (index) => {
    if (index === 0) return;
    const newData = [...dataSource];
    [newData[index - 1], newData[index]] = [newData[index], newData[index - 1]];
    queryClient.setQueryData(['banners'], newData);
  };

  const moveDown = (index) => {
    if (index >= dataSource.length - 1) return;
    const newData = [...dataSource];
    [newData[index], newData[index + 1]] = [newData[index + 1], newData[index]];
    queryClient.setQueryData(['banners'], newData);
  };

  const saveOrder = () => {
    const items = (queryClient.getQueryData(['banners']) || []).map((b, i) => ({ id: b.id, sortOrder: i + 1 }));
    reorderMutation.mutate(items);
  };

  const columns = [
    { title: 'Ảnh', dataIndex: 'image', key: 'image', width: 100,
      render: (v) => v ? <img src={v} alt="" style={{ height: 50, borderRadius: 4 }} /> : '-',
    },
    { title: 'Tiêu đề', dataIndex: 'title', key: 'title' },
    { title: 'Thứ tự', key: 'sortOrder', width: 120,
      render: (_, r, i) => (
        <Space>
          <span style={{ fontWeight: 600, minWidth: 20, textAlign: 'center' }}>{r.sortOrder}</span>
          <Button size="small" icon={<ArrowUpOutlined />} disabled={i === 0} onClick={() => moveUp(i)} />
          <Button size="small" icon={<ArrowDownOutlined />} disabled={i >= dataSource.length - 1} onClick={() => moveDown(i)} />
        </Space>
      ),
    },
    { title: 'Kích hoạt', dataIndex: 'active', key: 'active', width: 100,
      render: (v) => <Switch checked={v} disabled />,
    },
    {
      title: 'Thao tác', key: 'actions', width: 120,
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
        <h2>Quản lý Banner</h2>
        <Space>
          <Button onClick={saveOrder}>Lưu thứ tự</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm banner</Button>
        </Space>
      </div>
      <Table rowKey="id" columns={columns} dataSource={dataSource} loading={isLoading} pagination={false} />
      <Modal
        title={editing ? 'Sửa banner' : 'Thêm banner'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="title" label="Tiêu đề">
            <Input />
          </Form.Item>

          <Form.Item name="sortOrder" label="Thứ tự">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="active" label="Kích hoạt" valuePropName="checked">
            <Switch />
          </Form.Item>
          <Form.Item label="Hình ảnh">
            <Upload
              beforeUpload={(file) => { setFileList([file]); return false; }}
              onRemove={() => setFileList([])}
              fileList={fileList}
              maxCount={1}
              listType="picture"
            >
              {fileList.length < 1 && <Button icon={<UploadOutlined />}>Chọn ảnh</Button>}
            </Upload>
            {editing && editing.image && fileList.length === 0 && (
              <div style={{ marginTop: 8 }}>
                <span>Hiện tại: </span>
                <img src={editing.image} alt="" style={{ height: 60, borderRadius: 4 }} />
              </div>
            )}
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
