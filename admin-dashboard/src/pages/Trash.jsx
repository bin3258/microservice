import { Table, Button, Space, message, Popconfirm } from 'antd';
import { RollbackOutlined, DeleteOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { productAPI, categoryAPI } from '../api/axios';

export default function Trash() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const getImageSrc = (v) => {
    if (!v) return null;
    if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
    return `/uploads/${v}`;
  };

  const { data: products = [], isLoading: productsLoading } = useQuery({
    queryKey: ['trash'],
    queryFn: () => productAPI.getTrash().then(res => res.data),
  });

  const { data: categories = [] } = useQuery({
    queryKey: ['categories'],
    queryFn: () => categoryAPI.getAll().then(res => res.data),
  });

  const restoreMutation = useMutation({
    mutationFn: (id) => productAPI.restore(id),
    onSuccess: () => {
      message.success('Đã khôi phục sản phẩm');
      queryClient.invalidateQueries({ queryKey: ['trash'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  const hardDeleteMutation = useMutation({
    mutationFn: (id) => productAPI.hardDelete(id),
    onSuccess: () => {
      message.success('Đã xóa vĩnh viễn');
      queryClient.invalidateQueries({ queryKey: ['trash'] });
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });

  const handleRestore = (id) => {
    restoreMutation.mutate(id);
  };

  const handleHardDelete = (id) => {
    hardDeleteMutation.mutate(id);
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Tên', dataIndex: 'name', key: 'name' },
    { title: 'Giá', dataIndex: 'price', key: 'price', render: (v) => (v || 0).toLocaleString('vi-VN') + '₫' },
    {
      title: 'Hình ảnh', dataIndex: 'img', key: 'img',
      render: (v) => v ? <img src={getImageSrc(v)} alt="" style={{ height: 40 }} /> : '-',
    },
    {
      title: 'Danh mục', key: 'categoryName',
      render: (_, r) => {
        const cat = categories.find(c => c.id === r.categoryId);
        return cat ? cat.name : r.categoryId;
      },
    },
    {
      title: 'Thao tác', key: 'actions',
      render: (_, r) => (
        <Space>
          <Button type="primary" icon={<RollbackOutlined />} onClick={() => handleRestore(r.id)}>
            Khôi phục
          </Button>
          <Popconfirm title="Xóa vĩnh viễn?" onConfirm={() => handleHardDelete(r.id)}>
            <Button danger icon={<DeleteOutlined />}>Xóa hẳn</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0 }}>Thùng rác</h2>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/products')}>
          Quay về
        </Button>
      </div>
      <Table rowKey="id" columns={columns} dataSource={products} loading={productsLoading} />
    </>
  );
}
