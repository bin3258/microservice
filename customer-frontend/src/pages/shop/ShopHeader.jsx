import { Input, Select, Space, Typography } from 'antd';
import { SearchOutlined, SortAscendingOutlined } from '@ant-design/icons';

const { Text } = Typography;

export default function ShopHeader({ total, searchTerm, onSearchChange, sortBy, onSortChange }) {
  return (
    <div
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        flexWrap: 'wrap',
        gap: 16,
        marginBottom: 24,
        padding: '20px 24px',
        background: '#fff',
        borderRadius: 12,
        border: '1px solid var(--gray-100)',
      }}
    >
      <div>
        <Text style={{ fontSize: 14, color: 'var(--gray-500)' }}>
          Hiển thị{' '}
          <Text strong style={{ color: 'var(--gray-800)' }}>
            {total}
          </Text>{' '}
          sản phẩm
        </Text>
      </div>

      <Space size={12}>
        <Input
          placeholder="Tìm sản phẩm..."
          prefix={<SearchOutlined style={{ color: 'var(--gray-400)' }} />}
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
          style={{ width: 240 }}
          allowClear
        />
        <Select
          value={sortBy}
          onChange={onSortChange}
          style={{ width: 160 }}
          prefix={<SortAscendingOutlined />}
          options={[
            { value: 'default', label: 'Mặc định' },
            { value: 'price-asc', label: 'Giá: Thấp đến Cao' },
            { value: 'price-desc', label: 'Giá: Cao đến Thấp' },
            { value: 'name-asc', label: 'Tên: A-Z' },
            { value: 'name-desc', label: 'Tên: Z-A' },
          ]}
        />
      </Space>
    </div>
  );
}
