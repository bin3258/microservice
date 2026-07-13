import { useState } from 'react';
import { Table, Button, Modal, Form, Input, InputNumber, Select, Space, message, Popconfirm, Upload, notification } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, UploadOutlined, DeleteFilled } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productAPI, categoryAPI } from '../api/axios';

export default function Products() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [fileList, setFileList] = useState([]);
  const [categoryFilter, setCategoryFilter] = useState(null);
  const [form] = Form.useForm();

  const getImageSrc = (v) => {
    if (!v) return null;
    if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
    return `/uploads/${v}`;
  };

  const { data: allData = [], isLoading: productsLoading } = useQuery({
    queryKey: ['products'],
    queryFn: () => productAPI.getAll().then(res => res.data),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryAPI.getAll().then(res => res.data),
  });

  const createMutation = useMutation({
    mutationFn: (formData) => productAPI.create(formData),
    onSuccess: () => {
      message.success('Đã thêm sản phẩm');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, formData }) => productAPI.update(id, formData),
    onSuccess: () => {
      message.success('Đã cập nhật sản phẩm');
      setModalOpen(false);
      setFileList([]);
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: (id) => productAPI.delete(id),
    onSuccess: () => {
      message.success('Đã chuyển vào thùng rác');
      queryClient.invalidateQueries({ queryKey: ['products'] });
      queryClient.invalidateQueries({ queryKey: ['trash'] });
    },
    onError: (err) => {
      const msg = err.response?.data?.message || err.message || 'Lỗi khi xóa';
      notification.error({
        message: 'Không thể xóa sản phẩm',
        description: msg,
        duration: 5,
      });
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
    formData.append('name', values.name);
    formData.append('price', values.price);
    formData.append('categoryId', values.categoryId);
    formData.append('description', values.description || '');
    formData.append('ram', values.ram || '');
    formData.append('storage', values.storage || '');
    formData.append('screenResolution', values.screenResolution || '');
    formData.append('screenTechnology', values.screenTechnology || '');
    formData.append('battery', values.battery || '');
    formData.append('color', values.color || '');
    formData.append('salePrice', values.salePrice ?? 0);
    if (fileList.length > 0) {
      formData.append('imgFile', fileList[0].originFileObj || fileList[0]);
    }
    if (editing) {
      updateMutation.mutate({ id: editing.id, formData });
    } else {
      createMutation.mutate(formData);
    }
  };

  const filteredData = categoryFilter
    ? allData.filter(p => p.categoryId === categoryFilter)
    : allData;

  const handleDelete = (id) => {
    deleteMutation.mutate(id);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Giá', dataIndex: 'price', key: 'price', render: (v) => (v || 0).toLocaleString('vi-VN') + '₫' },
    { title: 'Hình ảnh', dataIndex: 'img', key: 'img', render: (v) => v ? <img src={getImageSrc(v)} alt="" style={{ height: 40 }} /> : '-' },
    { title: 'Danh mục', key: 'categoryName', render: (_, r) => {
        const cat = categories.find(c => c.id === r.categoryId);
        if (!cat) return r.categoryId;
        const parent = categories.find(p => p.id === cat.parentId);
        return parent ? `${parent.name} › ${cat.name}` : cat.name;
      },
    },
    { title: 'RAM', dataIndex: 'ram', key: 'ram', render: (v) => v || '-' },
    { title: 'Bộ nhớ', dataIndex: 'storage', key: 'storage', render: (v) => v || '-' },
    { title: 'Công nghệ MH', dataIndex: 'screenTechnology', key: 'screenTechnology', render: (v) => v || '-' },
    { title: 'Màu sắc', dataIndex: 'color', key: 'color', render: (v) => v || '-' },
    { title: 'Giá sale', dataIndex: 'salePrice', key: 'salePrice', render: (v) => v ? (v || 0).toLocaleString('vi-VN') + '₫' : '-' },
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
        <h2>Sản phẩm</h2>
        <Space>
          <Select
            allowClear
            showSearch
            placeholder="Lọc theo danh mục"
            style={{ width: 250 }}
            value={categoryFilter}
            onChange={setCategoryFilter}
            filterOption={(input, option) => option.label.toLowerCase().includes(input.toLowerCase())}
            options={categories.filter(c => c.parentId).map((c) => {
              const parent = categories.find(p => p.id === c.parentId);
              return { label: `[${parent?.name}] ${c.name}`, value: c.id };
            })}
          />
          <Button icon={<DeleteFilled />} onClick={() => navigate('/products/trash')}>Thùng rác</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>Thêm sản phẩm</Button>
        </Space>
      </div>
      <Table rowKey="id" columns={columns} dataSource={filteredData} loading={productsLoading} expandedRowRender={(r) => (
        <div style={{ display: 'flex', gap: 32 }}>
          {r.description && <div><strong>Mô tả:</strong> {r.description}</div>}
          {r.screenResolution && <div><strong>Độ phân giải:</strong> {r.screenResolution}</div>}
          {r.screenTechnology && <div><strong>Công nghệ MH:</strong> {r.screenTechnology}</div>}
          {r.battery && <div><strong>Pin:</strong> {r.battery}</div>}
        </div>
      )} />
      <Modal
        title={editing ? 'Sửa sản phẩm' : 'Thêm sản phẩm'}
        open={modalOpen}
        onOk={handleOk}
        onCancel={() => setModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item name="name" label="Tên" rules={[{ required: true }]}>
            <Input />
          </Form.Item>
          <Form.Item name="price" label="Giá" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
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
              {categories.filter(c => c.parentId != null).map((c) => {
                const parent = categories.find(p => p.id === c.parentId);
                return (
                  <Select.Option key={c.id} value={c.id}>
                    [{parent?.name || '?'}] {c.name}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="ram" label="RAM">
            <Input placeholder="VD: 8GB" />
          </Form.Item>
          <Form.Item name="storage" label="Bộ nhớ">
            <Input placeholder="VD: 256GB" />
          </Form.Item>
          <Form.Item name="screenResolution" label="Độ phân giải màn hình">
            <Input placeholder="VD: 2340x1080" />
          </Form.Item>
          <Form.Item name="screenTechnology" label="Công nghệ màn hình">
            <Input placeholder="VD: AMOLED, LCD, OLED, Super Retina XDR..." />
          </Form.Item>
          <Form.Item name="battery" label="Pin">
            <Input placeholder="VD: 5000mAh" />
          </Form.Item>
          <Form.Item name="color" label="Màu sắc">
            <Input placeholder="VD: Đen" />
          </Form.Item>
          <Form.Item name="salePrice" label="Giá sale (0 = không sale)">
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
