import { useState, useMemo } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { Layout, Menu, Button, theme, Dropdown, Avatar } from 'antd';
import {
  DashboardOutlined,
  ShoppingCartOutlined,
  AppstoreOutlined,
  UnorderedListOutlined,
  FileTextOutlined,
  FileAddOutlined,
  PictureOutlined,
  TeamOutlined,
  UserOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  InboxOutlined,
  HomeOutlined,
  TagOutlined,
  StarOutlined,
} from '@ant-design/icons';
import { useAuth } from '../context/useAuth';

const { Header, Sider, Content } = Layout;

const allMenuItems = [
  { key: '/', icon: <DashboardOutlined />, label: 'Bảng điều khiển' },
  { key: '/products', icon: <ShoppingCartOutlined />, label: 'Sản phẩm' },
  { key: '/categories', icon: <AppstoreOutlined />, label: 'Danh mục sản phẩm' },
  { key: '/banners', icon: <PictureOutlined />, label: 'Banner' },
  { key: '/post-categories', icon: <FileAddOutlined />, label: 'Danh mục bài viết' },
  { key: '/orders', icon: <UnorderedListOutlined />, label: 'Đơn hàng' },
  { key: '/posts', icon: <FileTextOutlined />, label: 'Bài viết' },
  { key: '/users', icon: <TeamOutlined />, label: 'Người dùng', managerOnly: true },
  { key: '/customers', icon: <UserOutlined />, label: 'Khách hàng' },
  { key: '/inventory', icon: <InboxOutlined />, label: 'Tồn kho' },
  { key: '/warehouses', icon: <HomeOutlined />, label: 'Kho hàng' },
  { key: '/discounts', icon: <TagOutlined />, label: 'Mã giảm giá' },
  { key: '/reviews', icon: <StarOutlined />, label: 'Đánh giá' },
];

export default function AdminLayout() {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { token: { colorBgContainer, borderRadiusLG } } = theme.useToken();

  const menuItems = useMemo(() =>
    allMenuItems
      .filter(item => {
        if (item.adminOnly) return user?.role === 'ADMIN';
        if (item.managerOnly) return user?.role === 'MANAGER' || user?.role === 'ADMIN';
        return true;
      })
      .map(item => {
        const { adminOnly, managerOnly, ...rest } = item;
        void adminOnly; void managerOnly;
        return rest;
      }),
    [user]
  );

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const userMenu = {
    items: [
      { key: 'profile', icon: <UserOutlined />, label: user?.username },
      { type: 'divider' },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
    ],
    onClick: ({ key }) => {
      if (key === 'logout') handleLogout();
    },
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider trigger={null} collapsible collapsed={collapsed} width={240}>
        <div style={{
          height: 32, margin: 16, display: 'flex', alignItems: 'center',
          justifyContent: 'center', color: '#fff', fontWeight: 'bold', fontSize: collapsed ? 14 : 18,
        }}>
          {collapsed ? 'AD' : 'Bảng quản trị'}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname.startsWith('/orders') ? '/orders' : location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header style={{
          padding: '0 16px', background: colorBgContainer,
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        }}>
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
          />
          <Dropdown menu={userMenu} placement="bottomRight">
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} />
              <span>{user?.username}</span>
            </div>
          </Dropdown>
        </Header>
        <Content style={{ margin: 24 }}>
          <div style={{ padding: 24, minHeight: 360, background: colorBgContainer, borderRadius: borderRadiusLG }}>
            <Outlet />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
