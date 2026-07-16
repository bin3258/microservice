import { Menu, Slider, Typography, Checkbox, Collapse, Button } from 'antd';
import { ReloadOutlined } from '@ant-design/icons';
import { RAM_OPTIONS, STORAGE_OPTIONS, BATTERY_OPTIONS, SCREEN_OPTIONS, COLOR_OPTIONS } from './filterUtils';

const { Title, Text } = Typography;

export default function ShopSidebar({
  categories, selectedCategory, onCategoryChange,
  priceRange, onPriceChange,
  selectedRam, onRamChange,
  selectedStorage, onStorageChange,
  selectedBattery, onBatteryChange,
  selectedScreen, onScreenChange,
  selectedColor, onColorChange,
  onReset,
}) {
  const range = priceRange || [0, 100000000];

  const parents = categories.filter(c => !c.parentId);
  const getChildren = (parentId) => categories.filter(c => c.parentId === parentId);

  const categoryItems = [
    { key: 'all', label: 'Tất cả sản phẩm' },
    ...parents.map((p) => {
      const children = getChildren(p.id);
      if (children.length === 0) return { key: String(p.id), label: p.name };
      return {
        key: String(p.id),
        label: p.name,
        children: [
          { key: `parent-all-${p.id}`, label: `Tất cả ${p.name}` },
          ...children.map((c) => ({ key: String(c.id), label: c.name })),
        ],
      };
    }),
  ];

  const handleCategoryClick = ({ key }) => {
    if (key === 'all') {
      onCategoryChange(null);
    } else if (key.startsWith('parent-all-')) {
      onCategoryChange(key.replace('parent-all-', ''));
    } else {
      onCategoryChange(key);
    }
  };

  const collapseItems = [
    {
      key: 'ram',
      label: 'RAM',
      children: (
        <Checkbox.Group value={selectedRam} onChange={onRamChange}>
          {RAM_OPTIONS.map((o) => (
            <div key={o.value} style={{ marginBottom: 6 }}>
              <Checkbox value={o.value}>{o.label}</Checkbox>
            </div>
          ))}
        </Checkbox.Group>
      ),
    },
    {
      key: 'storage',
      label: 'Bộ nhớ trong',
      children: (
        <Checkbox.Group value={selectedStorage} onChange={onStorageChange}>
          {STORAGE_OPTIONS.map((o) => (
            <div key={o.value} style={{ marginBottom: 6 }}>
              <Checkbox value={o.value}>{o.label}</Checkbox>
            </div>
          ))}
        </Checkbox.Group>
      ),
    },
    {
      key: 'battery',
      label: 'Pin',
      children: (
        <Checkbox.Group value={selectedBattery} onChange={onBatteryChange}>
          {BATTERY_OPTIONS.map((o) => (
            <div key={o.value} style={{ marginBottom: 6 }}>
              <Checkbox value={o.value}>{o.label}</Checkbox>
            </div>
          ))}
        </Checkbox.Group>
      ),
    },
    {
      key: 'screen',
      label: 'Màn hình',
      children: (
        <Checkbox.Group value={selectedScreen} onChange={onScreenChange}>
          {SCREEN_OPTIONS.map((o) => (
            <div key={o.value} style={{ marginBottom: 6 }}>
              <Checkbox value={o.value}>{o.label}</Checkbox>
            </div>
          ))}
        </Checkbox.Group>
      ),
    },
    {
      key: 'color',
      label: 'Màu sắc',
      children: (
        <Checkbox.Group value={selectedColor} onChange={onColorChange}>
          {COLOR_OPTIONS.map((o) => (
            <div key={o.value} style={{ marginBottom: 6 }}>
              <Checkbox value={o.value}>{o.label}</Checkbox>
            </div>
          ))}
        </Checkbox.Group>
      ),
    },
  ];

  return (
    <div style={{ padding: '24px 0' }}>
      <div style={{ marginBottom: 28 }}>
        <Title level={5} style={{ marginBottom: 12, fontWeight: 600 }}>
          Danh mục
        </Title>
        <Menu
          mode="inline"
          selectedKeys={[selectedCategory || 'all']}
          items={categoryItems}
          onClick={handleCategoryClick}
          style={{ border: 'none', background: 'transparent' }}
        />
      </div>

      <div style={{ marginBottom: 28 }}>
        <Title level={5} style={{ marginBottom: 12, fontWeight: 600 }}>
          Khoảng giá
        </Title>
        <div style={{ padding: '0 8px' }}>
          <Slider
            range
            min={0}
            max={100000000}
            step={500000}
            value={range}
            onChange={(v) => onPriceChange(v)}
            tooltip={{ formatter: (v) => (v || 0).toLocaleString('vi-VN') + '₫' }}
          />
          <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 4 }}>
            <Text style={{ color: 'var(--gray-500)', fontSize: 13 }}>{(range[0] || 0).toLocaleString('vi-VN') + '₫'}</Text>
            <Text style={{ color: 'var(--gray-500)', fontSize: 13 }}>{(range[1] || 0).toLocaleString('vi-VN') + '₫'}</Text>
          </div>
        </div>
      </div>

      <Collapse
        ghost
        expandIconPosition="end"
        items={collapseItems}
        defaultActiveKey={[]}
      />

      <Button
        icon={<ReloadOutlined />}
        block
        onClick={onReset}
        style={{ marginTop: 16, borderRadius: 8 }}
      >
        Làm mới bộ lọc
      </Button>
    </div>
  );
}
