import { useState, useEffect, useCallback } from 'react';
import { Table, Typography, Rate, Modal, Input, message, Button, Tag, Space, Image } from 'antd';
import { reviewAPI } from '../api/axios';

const { Title, Text } = Typography;

const getImageSrc = (v) => {
  if (!v) return 'https://placehold.co/500x500?text=No+Image';
  if (v.startsWith('http://') || v.startsWith('https://') || v.startsWith('/')) return v;
  return `/uploads/${v}`;
};

export default function Reviews() {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [replyModalOpen, setReplyModalOpen] = useState(false);
  const [replyTarget, setReplyTarget] = useState(null);
  const [replyText, setReplyText] = useState('');
  const [replySubmitting, setReplySubmitting] = useState(false);

  const fetchReviews = useCallback(() => {
    setLoading(true);
    reviewAPI.getAll()
      .then(res => setReviews(res.data || []))
      .catch(() => message.error('Không thể tải đánh giá'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchReviews(); }, [fetchReviews]);

  const openReply = (record) => {
    setReplyTarget(record);
    setReplyText(record.adminReply || '');
    setReplyModalOpen(true);
  };

  const handleReply = async () => {
    if (!replyTarget) return;
    setReplySubmitting(true);
    try {
      await reviewAPI.reply(replyTarget.id, { adminReply: replyText });
      message.success('Đã phản hồi đánh giá');
      setReplyModalOpen(false);
      fetchReviews();
    } catch (err) {
      message.error(err.response?.data?.message || 'Phản hồi thất bại');
    } finally {
      setReplySubmitting(false);
    }
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', key: 'id', width: 60 },
    { title: 'Đơn hàng', dataIndex: 'orderId', key: 'orderId', width: 80 },
    { title: 'Sản phẩm', dataIndex: 'productName', key: 'productName', ellipsis: true },
    { title: 'Khách hàng', dataIndex: 'name', key: 'name', width: 150 },
    {
      title: 'Đánh giá', dataIndex: 'rating', key: 'rating', width: 150,
      render: (val) => <Rate disabled value={val} style={{ fontSize: 12 }} />,
    },
    {
      title: 'Mô tả', dataIndex: 'description', key: 'description', ellipsis: true,
      render: (val) => val || <Text type="secondary">—</Text>,
    },
    {
      title: 'Hình ảnh', dataIndex: 'images', key: 'images', width: 120,
      render: (imgs) => imgs?.length > 0 ? (
        <Space>
          {imgs.slice(0, 2).map((img, i) => (
            <Image key={i} src={getImageSrc(img)} style={{ width: 40, height: 40, objectFit: 'cover', borderRadius: 4 }} preview={{ mask: null }} />
          ))}
          {imgs.length > 2 && <Text type="secondary">+{imgs.length - 2}</Text>}
        </Space>
      ) : <Text type="secondary">—</Text>,
    },
    {
      title: 'Phản hồi', dataIndex: 'adminReply', key: 'adminReply', width: 100,
      render: (val) => val ? <Tag color="blue">Đã phản hồi</Tag> : <Tag>Chưa</Tag>,
    },
    {
      title: 'Ngày', dataIndex: 'createdAt', key: 'createdAt', width: 100,
      render: (val) => val ? new Date(val).toLocaleDateString('vi-VN') : '',
    },
    {
      title: 'Hành động', key: 'action', width: 100, fixed: 'right',
      render: (_, record) => (
        <Button type="link" size="small" onClick={() => openReply(record)}>
          {record.adminReply ? 'Sửa phản hồi' : 'Phản hồi'}
        </Button>
      ),
    },
  ];

  return (
    <div>
      <Title level={3}>Quản lý đánh giá</Title>
      <Table
        columns={columns}
        dataSource={reviews}
        rowKey="id"
        loading={loading}
        scroll={{ x: 1000 }}
        pagination={{ pageSize: 20 }}
      />
      <Modal
        title="Phản hồi đánh giá"
        open={replyModalOpen}
        onOk={handleReply}
        onCancel={() => setReplyModalOpen(false)}
        confirmLoading={replySubmitting}
        okText={replyTarget?.adminReply ? 'Cập nhật' : 'Gửi phản hồi'}
      >
        <div style={{ marginBottom: 16 }}>
          <Text strong>Sản phẩm: </Text><Text>{replyTarget?.productName}</Text><br />
          <Text strong>Khách hàng: </Text><Text>{replyTarget?.name}</Text><br />
          <Text strong>Đánh giá: </Text><Rate disabled value={replyTarget?.rating} style={{ fontSize: 12 }} />
          {replyTarget?.description && <><br /><Text strong>Mô tả: </Text><Text>{replyTarget.description}</Text></>}
        </div>
        <Input.TextArea
          rows={4}
          placeholder="Nhập phản hồi của shop..."
          value={replyText}
          onChange={(e) => setReplyText(e.target.value)}
        />
      </Modal>
    </div>
  );
}
