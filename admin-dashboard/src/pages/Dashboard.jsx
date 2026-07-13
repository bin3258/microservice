import { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Spin } from 'antd';
import {
  ShoppingCartOutlined, TeamOutlined, AppstoreOutlined,
  UnorderedListOutlined, DollarOutlined, InboxOutlined,
} from '@ant-design/icons';
import { productAPI, orderAPI, userAPI, categoryAPI, customerAPI, inventoryAPI } from '../api/axios';

export default function Dashboard() {
  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({});

  useEffect(() => {
    Promise.all([
      productAPI.getAll(),
      orderAPI.getAll(),
      userAPI.getAll(),
      categoryAPI.getAll(),
      customerAPI.getAll(),
      inventoryAPI.getAll(),
    ]).then(([products, orders, users, categories, customers, inventory]) => {
      setStats({
        products: products.data.length,
        orders: orders.data.length,
        users: users.data.length,
        categories: categories.data.length,
        customers: customers.data.length,
        inventory: inventory.data.length,
        totalRevenue: Array.isArray(orders.data)
          ? orders.data.reduce((sum, o) => sum + (o.totalPrice || 0), 0)
          : 0,
      });
    }).finally(() => setLoading(false));
  }, []);

  if (loading) return <Spin size="large" style={{ display: 'block', margin: '100px auto' }} />;

  const cards = [
    { title: 'Sản phẩm', value: stats.products, icon: <ShoppingCartOutlined />, color: '#1677ff' },
    { title: 'Đơn hàng', value: stats.orders, icon: <UnorderedListOutlined />, color: '#52c41a' },
    { title: 'Người dùng', value: stats.users, icon: <TeamOutlined />, color: '#722ed1' },
    { title: 'Danh mục', value: stats.categories, icon: <AppstoreOutlined />, color: '#fa8c16' },
    { title: 'Khách hàng', value: stats.customers, icon: <TeamOutlined />, color: '#13c2c2' },
    { title: 'Tồn kho', value: stats.inventory, icon: <InboxOutlined />, color: '#eb2f96' },
  ];

  return (
    <>
      <Row gutter={[16, 16]}>
        <Col span={24}>
          <Card>
            <Statistic
              title="Tổng doanh thu"
              value={stats.totalRevenue}
              precision={2}
              prefix={<DollarOutlined />}
              suffix="USD"
            />
          </Card>
        </Col>
        {cards.map((c) => (
          <Col xs={24} sm={12} lg={8} key={c.title}>
            <Card>
              <Statistic title={c.title} value={c.value} prefix={c.icon} valueStyle={{ color: c.color }} />
            </Card>
          </Col>
        ))}
      </Row>
    </>
  );
}
