import { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Row, Col, Typography } from 'antd';
import ShopSidebar from './ShopSidebar';
import ShopHeader from './ShopHeader';
import ProductList from './ProductList';
import { productService, categoryService } from '../../services/productService';
import { cartService } from '../../services/orderService';
import { useAuth } from '../../context/useAuth';
import { useCart } from '../../context/useCart';
import { message } from 'antd';
import {
  parseRam, parseStorage, parseBattery, categorizeScreen,
  RAM_OPTIONS, STORAGE_OPTIONS, BATTERY_OPTIONS,
} from './filterUtils';

const { Title } = Typography;

export default function ShopPage() {
  const [searchParams] = useSearchParams();
  const { user } = useAuth();
  const { refreshCartCount } = useCart();

  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categories, setCategories] = useState([]);

  const [selectedCategory, setSelectedCategory] = useState(null);
  const [priceRange, setPriceRange] = useState([0, 100000000]);
  const [searchTerm, setSearchTerm] = useState(searchParams.get('q') || '');
  const [sortBy, setSortBy] = useState('default');

  const [selectedRam, setSelectedRam] = useState([]);
  const [selectedStorage, setSelectedStorage] = useState([]);
  const [selectedBattery, setSelectedBattery] = useState([]);
  const [selectedScreen, setSelectedScreen] = useState([]);

  const resetFilters = () => {
    setSelectedCategory(null);
    setPriceRange([0, 100000000]);
    setSearchTerm(searchParams.get('q') || '');
    setSortBy('default');
    setSelectedRam([]);
    setSelectedStorage([]);
    setSelectedBattery([]);
    setSelectedScreen([]);
  };

  useEffect(() => {
    let ignore = false;
    Promise.all([
      productService.getAll(),
      categoryService.getAll(),
    ])
      .then(([prodRes, catRes]) => {
        if (!ignore) {
          setProducts(prodRes.data);
          setCategories(catRes.data);
        }
      })
      .finally(() => { if (!ignore) setLoading(false); });
    return () => { ignore = true; };
  }, []);

  const getPrice = (p) => (p.salePrice != null && p.salePrice > 0) ? p.salePrice : p.price;

  const filteredProducts = useMemo(() => {
    let result = [...products];

    if (selectedCategory) {
      const catIds = [selectedCategory];
      const children = categories.filter((c) => String(c.parentId) === selectedCategory);
      children.forEach((c) => catIds.push(String(c.id)));
      result = result.filter((p) => catIds.includes(String(p.categoryId)));
    }

    result = result.filter((p) => {
      const effPrice = getPrice(p);
      return effPrice >= priceRange[0] && effPrice <= priceRange[1];
    });

    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      result = result.filter(
        (p) =>
          p.name?.toLowerCase().includes(term) ||
          p.description?.toLowerCase().includes(term),
      );
    }

    if (selectedRam.length > 0) {
      result = result.filter((p) => {
        const v = parseRam(p.ram);
        return v !== null && selectedRam.some((key) =>
          RAM_OPTIONS.find((o) => o.value === key)?.match(v)
        );
      });
    }

    if (selectedStorage.length > 0) {
      result = result.filter((p) => {
        const v = parseStorage(p.storage);
        return v !== null && selectedStorage.some((key) =>
          STORAGE_OPTIONS.find((o) => o.value === key)?.match(v)
        );
      });
    }

    if (selectedBattery.length > 0) {
      result = result.filter((p) => {
        const v = parseBattery(p.battery);
        return v !== null && selectedBattery.some((key) =>
          BATTERY_OPTIONS.find((o) => o.value === key)?.match(v)
        );
      });
    }

    if (selectedScreen.length > 0) {
      result = result.filter((p) => {
        const cat = categorizeScreen(p.screenResolution);
        return cat !== null && selectedScreen.includes(cat);
      });
    }

    switch (sortBy) {
      case 'price-asc':
        result.sort((a, b) => getPrice(a) - getPrice(b));
        break;
      case 'price-desc':
        result.sort((a, b) => getPrice(b) - getPrice(a));
        break;
      case 'name-asc':
        result.sort((a, b) => a.name?.localeCompare(b.name));
        break;
      case 'name-desc':
        result.sort((a, b) => b.name?.localeCompare(a.name));
        break;
      default:
        break;
    }

    return result;
  }, [products, selectedCategory, priceRange, searchTerm, sortBy, selectedRam, selectedStorage, selectedBattery, selectedScreen, categories]);

  const addToCart = async (product) => {
    if (!user) { message.info('Vui lòng đăng nhập để mua hàng'); return; }
    try {
      await cartService.addItem(user.userId, {
        productId: product.id,
        productName: product.name,
        productImg: product.img,
        quantity: 1,
        unitPrice: getPrice(product),
      });
      message.success('Đã thêm vào giỏ hàng');
      refreshCartCount();
    } catch {
      message.error('Lỗi khi thêm vào giỏ hàng');
    }
  };

  return (
    <div style={{ maxWidth: 1200, margin: '0 auto', padding: '32px 20px' }}>
      <Title level={2} style={{ fontWeight: 700, marginBottom: 8, fontSize: 28 }}>
        Cửa hàng
      </Title>
      <p style={{ color: 'var(--gray-500)', marginBottom: 24 }}>
        Khám phá bộ sưu tập điện thoại mới nhất
      </p>

      <Row gutter={[32, 24]}>
        <Col xs={24} sm={24} md={6}>
          <div
            style={{
              background: '#fff',
              borderRadius: 12,
              padding: '0 16px',
              border: '1px solid var(--gray-100)',
              position: 'sticky',
              top: 88,
            }}
          >
            <ShopSidebar
              categories={categories}
              selectedCategory={selectedCategory}
              onCategoryChange={setSelectedCategory}
              priceRange={priceRange}
              onPriceChange={setPriceRange}
              selectedRam={selectedRam}
              onRamChange={setSelectedRam}
              selectedStorage={selectedStorage}
              onStorageChange={setSelectedStorage}
              selectedBattery={selectedBattery}
              onBatteryChange={setSelectedBattery}
              selectedScreen={selectedScreen}
              onScreenChange={setSelectedScreen}
              onReset={resetFilters}
            />
          </div>
        </Col>
        <Col xs={24} sm={24} md={18}>
          <ShopHeader
            total={filteredProducts.length}
            searchTerm={searchTerm}
            onSearchChange={setSearchTerm}
            sortBy={sortBy}
            onSortChange={setSortBy}
          />
          <ProductList products={filteredProducts} loading={loading} onAddToCart={addToCart} />
        </Col>
      </Row>
    </div>
  );
}
