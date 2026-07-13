import { useState, useMemo } from 'react';
import { Layout, Input, Badge, Button, Space, Dropdown, Avatar, Drawer, AutoComplete } from 'antd';
import {
  ShoppingCartOutlined, UserOutlined, LogoutOutlined,
  MenuOutlined, SearchOutlined, HistoryOutlined, CloseCircleOutlined,
} from '@ant-design/icons';
import { useNavigate, Link, useLocation } from 'react-router-dom';
import { useAuth } from '../context/useAuth';
import { useCart } from '../context/useCart';

const { Header: AntHeader } = Layout;

export default function Header() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuth();
  const { cartCount } = useCart();
  const isShopPage = location.pathname === '/shop';
  const [search, setSearch] = useState(new URLSearchParams(location.search).get('q') || '');
  const [searchFocused, setSearchFocused] = useState(false);
  const [searchHistory, setSearchHistory] = useState(() => {
    try { return JSON.parse(localStorage.getItem('searchHistory') || '[]'); } catch { return []; }
  });
  const [mobileMenu, setMobileMenu] = useState(false);

  const saveSearch = (term) => {
    const trimmed = term.trim();
    if (!trimmed) return;
    const updated = [trimmed, ...searchHistory.filter(t => t !== trimmed)].slice(0, 8);
    setSearchHistory(updated);
    localStorage.setItem('searchHistory', JSON.stringify(updated));
  };

  const clearHistory = () => {
    setSearchHistory([]);
    localStorage.removeItem('searchHistory');
  };

  const handleSearch = (v) => {
    if (!v.trim()) return;
    saveSearch(v);
    navigate(`/shop?q=${encodeURIComponent(v.trim())}`);
  };

  const historyOptions = useMemo(() => {
    if (searchHistory.length === 0) return [];
    const filtered = search
      ? searchHistory.filter(t => t.toLowerCase().includes(search.toLowerCase()))
      : searchHistory;
    return [
      ...filtered.map(t => ({ value: t, label: <Space><HistoryOutlined style={{ color: 'var(--gray-400)' }} />{t}</Space> })),
      { value: '__clear__', label: <Button type="link" size="small" icon={<CloseCircleOutlined />} onClick={clearHistory} style={{ padding: 0 }}>Xóa lịch sử tìm kiếm</Button>, disabled: true },
    ];
  }, [searchHistory, search]);

  const userMenu = {
    items: [
      { key: 'profile', icon: <UserOutlined />, label: 'Tài khoản' },
      { key: 'orders', icon: <MenuOutlined />, label: 'Đơn hàng' },
      { type: 'divider' },
      { key: 'logout', icon: <LogoutOutlined />, label: 'Đăng xuất', danger: true },
    ],
    onClick: ({ key }) => {
      if (key === 'logout') { logout(); navigate('/'); }
      else if (key === 'profile') navigate('/profile');
      else if (key === 'orders') navigate('/orders');
    },
  };

  const navItems = [
    { label: 'Trang chủ', path: '/' },
    { label: 'Cửa hàng', path: '/shop' },
    { label: 'Tin tức', path: '/blog' },
    { label: 'Liên hệ', path: '/contact' },
  ];

  return (
    <AntHeader
      style={{
        background: 'rgba(255,255,255,0.95)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '0 32px',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        height: 72,
        borderBottom: '1px solid rgba(0,0,0,0.05)',
      }}
    >
      <Link
        to="/"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          textDecoration: 'none',
        }}
      >
        <div
          style={{
            width: 36,
            height: 36,
            borderRadius: 10,
            background: 'linear-gradient(135deg, var(--primary), var(--accent))',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontWeight: 800,
            fontSize: 16,
          }}
        >
          P
        </div>
        <span
          style={{
            fontSize: 20,
            fontWeight: 700,
            background: 'linear-gradient(135deg, var(--primary), var(--accent))',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
            letterSpacing: '-0.5px',
          }}
        >
          PhoneStore
        </span>
      </Link>

      <nav
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 20,
        }}
        className="desktop-nav"
      >
        {navItems.map((item) => (
          <Link
            key={item.path}
            to={item.path}
            style={{
              color: 'var(--gray-600)',
              fontWeight: 500,
              fontSize: 14,
              textDecoration: 'none',
              whiteSpace: 'nowrap',
              transition: 'color 0.2s',
              position: 'relative',
            }}
            onMouseEnter={(e) => {
              e.target.style.color = 'var(--primary)';
            }}
            onMouseLeave={(e) => {
              e.target.style.color = 'var(--gray-600)';
            }}
          >
            {item.label}
          </Link>
        ))}
      </nav>

      <Space size="small" align="center">
        <div className="desktop-search" style={{ display: isShopPage ? 'none' : '' }}>
          <AutoComplete
            value={search}
            onChange={setSearch}
            options={historyOptions}
            onSelect={(v) => { if (v !== '__clear__') handleSearch(v); }}
            onFocus={() => setSearchFocused(true)}
            onBlur={() => setTimeout(() => setSearchFocused(false), 200)}
            className="search-autocomplete"
            open={searchFocused && historyOptions.length > 0}
          >
       <Input.Search
          placeholder="Tìm..."
          onSearch={handleSearch}
          prefix={<SearchOutlined style={{ color: 'var(--gray-400)' }} />}
          size="middle"
          style={{ width: 230, height: 32, borderRadius: 8, background: 'var(--gray-100)', marginTop: 15 }}
        />
          </AutoComplete>
        </div>

        <Badge count={cartCount} showZero={false} size="small">
          <Button
            type="text"
            icon={<ShoppingCartOutlined style={{ fontSize: 20 }} />}
            onClick={() => navigate('/cart')}
            style={{ color: 'var(--gray-600)' }}
          />
        </Badge>

        {user ? (
          <Dropdown menu={userMenu} placement="bottomRight">
            <Space style={{ cursor: 'pointer' }} size={8}>
              <Avatar
                icon={<UserOutlined />}
                style={{ background: 'linear-gradient(135deg, var(--primary), var(--accent))' }}
              />
              <span className="desktop-user" style={{ fontWeight: 500, color: 'var(--gray-700)' }}>
                {user.username}
              </span>
            </Space>
          </Dropdown>
        ) : (
          <Button
            type="primary"
            onClick={() => navigate('/login')}
            style={{ borderRadius: 8, height: 26 }}
          >
            Đăng nhập
          </Button>
        )}

        <Button
          type="text"
          icon={<MenuOutlined style={{ fontSize: 20 }} />}
          onClick={() => setMobileMenu(true)}
          className="mobile-menu-btn"
          style={{ display: 'none', color: 'var(--gray-600)' }}
        />
      </Space>

      <Drawer
        title="Menu"
        placement="right"
        onClose={() => setMobileMenu(false)}
        open={mobileMenu}
        width={280}
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {navItems.map((item) => (
            <Link
              key={item.path}
              to={item.path}
              onClick={() => setMobileMenu(false)}
              style={{
                color: 'var(--gray-700)',
                fontWeight: 500,
                fontSize: 16,
                textDecoration: 'none',
                padding: '8px 0',
                borderBottom: '1px solid var(--gray-100)',
              }}
            >
              {item.label}
            </Link>
          ))}
          {!isShopPage && (
            <Input.Search
              placeholder="Tìm kiếm..."
              onSearch={(v) => { setSearch(v); handleSearch(v); }}
              style={{ marginTop: 8 }}
            />
          )}
        </div>
      </Drawer>

      <style>{`
        .search-autocomplete { width: 300px; }
        @media (max-width: 900px) {
          .desktop-nav, .desktop-search, .desktop-user { display: none !important; }
          .mobile-menu-btn { display: inline-flex !important; }
          .ant-layout-header { padding: 0 16px !important; }
        }
        @media (min-width: 901px) and (max-width: 1100px) {
          .desktop-nav { gap: 14px !important; }
          .search-autocomplete { width: 200px !important; }
          .ant-layout-header { padding: 0 20px !important; }
        }
      `}</style>
    </AntHeader>
  );
}
