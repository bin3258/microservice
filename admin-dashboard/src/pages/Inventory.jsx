import { useState, useMemo } from 'react';
import { Table, Tag, Button, Modal, Form, InputNumber, Space, message, Select, Popconfirm } from 'antd';
import { EditOutlined, PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { inventoryAPI, productAPI, categoryAPI, warehouseAPI } from '../api/axios';

export default function Inventory() {
  const queryClient = useQueryClient();
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form] = Form.useForm();
  const [categoryFilter, setCategoryFilter] = useState(null);
  const [warehouseFilter, setWarehouseFilter] = useState(null);
  const [addModalOpen, setAddModalOpen] = useState(false);
  const [addForm] = Form.useForm();

  const { data: inventoryData = [], isLoading } = useQuery({
    queryKey: ['inventory'],
    queryFn: () => inventoryAPI.getAll().then(res => res.data),
  });

  const { data: products = [] } = useQuery({
    queryKey: ['products'],
    queryFn: () => productAPI.getAll().then(res => res.data),
  });

  const { data: trashProducts = [] } = useQuery({
    queryKey: ['trash'],
    queryFn: () => productAPI.getTrash().then(res => res.data),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryAPI.getAll().then(res => res.data),
  });

  const { data: warehouses = [] } = useQuery({
    queryKey: ['warehouses'],
    queryFn: () => warehouseAPI.getAll().then(res => res.data),
  });

  const allProducts = useMemo(() => [...products, ...trashProducts], [products, trashProducts]);

  const productMap = useMemo(() => {
    const map = {};
    allProducts.forEach((p) => { map[p.id] = p; });
    return map;
  }, [allProducts]);

  const categoryMap = useMemo(() => {
    const map = {};
    categories.forEach((c) => { map[c.id] = c; });
    return map;
  }, [categories]);

  const warehouseMap = useMemo(() => {
    const map = {};
    warehouses.forEach((w) => { map[w.id] = w; });
    return map;
  }, [warehouses]);

  const editMutation = useMutation({
    mutationFn: ({ productId, values }) => inventoryAPI.update(productId, values),
    onSuccess: () => {
      message.success('Đã cập nhật tồn kho');
      setEditModalOpen(false);
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
    },
  });

  const addMutation = useMutation({
    mutationFn: ({ productId, values }) => inventoryAPI.update(productId, values),
    onSuccess: () => {
      message.success('Đã thêm vào tồn kho');
      setAddModalOpen(false);
      addForm.resetFields();
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: ({ productId, warehouseId }) => inventoryAPI.deleteByWarehouse(productId, warehouseId),
    onSuccess: () => {
      message.success('Đã xóa khỏi tồn kho');
      queryClient.invalidateQueries({ queryKey: ['inventory'] });
    },
  });

  const openEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({ quantity: record.quantity });
    setEditModalOpen(true);
  };

  const handleEditOk = async () => {
    const values = await form.validateFields();
    editMutation.mutate({ productId: editing.productId, values: { quantity: values.quantity, warehouseId: editing.warehouseId } });
  };

  const handleAddProduct = async () => {
    const values = await addForm.validateFields();
    const exists = inventoryData.some(
      (d) => d.productId === values.productId && d.warehouseId === values.warehouseId
    );
    if (exists) {
      message.warning('Sản phẩm đã có trong kho này');
      return;
    }
    addMutation.mutate({ productId: values.productId, values: { quantity: values.quantity || 0, warehouseId: values.warehouseId } });
  };

  const handleDelete = (record) => {
    deleteMutation.mutate({ productId: record.productId, warehouseId: record.warehouseId });
  };

  const filteredData = useMemo(() => {
    let result = inventoryData;
    if (categoryFilter) {
      const childIds = categories.filter(c => c.parentId === categoryFilter).map(c => c.id);
      childIds.push(categoryFilter);
      result = result.filter((item) => {
        const prod = productMap[item.productId];
        return prod && childIds.includes(prod.categoryId);
      });
    }
    if (warehouseFilter) {
      result = result.filter((item) => item.warehouseId === warehouseFilter);
    }
    return result;
  }, [inventoryData, categoryFilter, warehouseFilter, productMap]);

  const columns = [
    { title: 'Tên sản phẩm', key: 'productName',
      render: (_, r) => productMap[r.productId]?.name || `ID: ${r.productId}`,
    },
    { title: 'Danh mục', key: 'categoryName',
      render: (_, r) => {
        const catId = productMap[r.productId]?.categoryId;
        return catId ? categoryMap[catId]?.name || '' : '';
      },
    },
    { title: 'Kho hàng', key: 'warehouseName',
      render: (_, r) => warehouseMap[r.warehouseId]?.name || `Kho #${r.warehouseId}`,
    },
    { title: 'Số lượng', dataIndex: 'quantity', key: 'quantity' },
    { title: 'Đã đặt', dataIndex: 'reservedQuantity', key: 'reservedQuantity' },
    {
      title: 'Có sẵn', key: 'available',
      render: (_, r) => {
        const avail = r.quantity - r.reservedQuantity;
        const color = avail > 10 ? 'green' : avail > 0 ? 'orange' : 'red';
        return <Tag color={color}>{avail}</Tag>;
      },
    },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button icon={<EditOutlined />} onClick={() => openEdit(r)} />
          <Popconfirm
            title="Xóa khỏi tồn kho?"
            description={`Xóa "${productMap[r.productId]?.name}" khỏi "${warehouseMap[r.warehouseId]?.name}"?`}
            onConfirm={() => handleDelete(r)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Tồn kho</h2>
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
          <Select
            allowClear
            placeholder="Lọc theo kho"
            style={{ width: 200 }}
            value={warehouseFilter}
            onChange={setWarehouseFilter}
            options={warehouses.map((w) => ({ label: w.name, value: w.id }))}
          />
          <Button type="primary" icon={<PlusOutlined />} onClick={() => { addForm.resetFields(); setAddModalOpen(true); }}>
            Thêm sản phẩm
          </Button>
        </Space>
      </div>
      <Table rowKey={(r) => `${r.productId}-${r.warehouseId}`} columns={columns} dataSource={filteredData} loading={isLoading} />
      <Modal
        title="Cập nhật tồn kho"
        open={editModalOpen}
        onOk={handleEditOk}
        onCancel={() => setEditModalOpen(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item label="Kho hàng">
            <InputNumber value={editing?.warehouseId} disabled style={{ width: '100%' }} />
            <div style={{ fontSize: 12, color: 'var(--gray-400)' }}>
              {editing ? warehouseMap[editing.warehouseId]?.name : ''}
            </div>
          </Form.Item>
          <Form.Item name="quantity" label="Số lượng" rules={[{ required: true }]}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="Thêm sản phẩm vào tồn kho"
        open={addModalOpen}
        onOk={handleAddProduct}
        onCancel={() => setAddModalOpen(false)}
      >
        <Form form={addForm} layout="vertical">
          <Form.Item name="productId" label="Sản phẩm" rules={[{ required: true, message: 'Chọn sản phẩm' }]}>
            <Select
              showSearch
              placeholder="Chọn sản phẩm"
              filterOption={(input, option) => option.label.toLowerCase().includes(input.toLowerCase())}
              options={allProducts.filter((p) => !p.deleted).map((p) => ({ label: p.name, value: p.id }))}
            />
          </Form.Item>
          <Form.Item name="warehouseId" label="Kho hàng" rules={[{ required: true, message: 'Chọn kho' }]}>
            <Select
              placeholder="Chọn kho"
              options={warehouses.map((w) => ({ label: w.name, value: w.id }))}
            />
          </Form.Item>
          <Form.Item name="quantity" label="Số lượng" initialValue={0}>
            <InputNumber min={0} style={{ width: '100%' }} />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}
