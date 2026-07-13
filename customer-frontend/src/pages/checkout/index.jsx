import { useState, useEffect, useMemo } from 'react';
import { Form, Input, Button, Typography, Row, Col, Divider, message, Select, Space, Card, Radio, Spin, Table } from 'antd';
import { UserOutlined, MailOutlined, PhoneOutlined, EnvironmentOutlined, ShoppingCartOutlined, ArrowLeftOutlined, PlusOutlined, CreditCardOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { orderService, paymentService } from '../../services/orderService';
import { customerService, addressService } from '../../services/orderService';
import { cartService } from '../../services/orderService';
import { useAuth } from '../../context/useAuth';
import { useCart } from '../../context/useCart';
import api from '../../api/axiosClient';
import { haversine, geocode, calcShippingFee } from '../../utils/geo';

const { Title, Text } = Typography;

export default function CheckoutPage() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { refreshCartCount } = useCart();
  const [form] = Form.useForm();
  const [customer, setCustomer] = useState(null);
  const [addresses, setAddresses] = useState([]);
  const [selectedAddr, setSelectedAddr] = useState(null);
  const [showNewForm, setShowNewForm] = useState(false);
  const [newAddrForm] = Form.useForm();
  const [addingAddr, setAddingAddr] = useState(false);
  const [pageLoading, setPageLoading] = useState(true);
  const [cartItems, setCartItems] = useState([]);
  const [cartLoading, setCartLoading] = useState(true);
  const [processing, setProcessing] = useState(false);

  const [warehouses, setWarehouses] = useState([]);
  const [inventory, setInventory] = useState([]);
  const [warehouseSelections, setWarehouseSelections] = useState({});
  const [customerLat, setCustomerLat] = useState(null);
  const [customerLng, setCustomerLng] = useState(null);
  const [shippingFee, setShippingFee] = useState(0);

  useEffect(() => {
    if (!user) { navigate('/login'); return; }
    let ignore = false;
    (async () => {
      try {
        const cusRes = await customerService.getByAuthId(user.userId);
        if (ignore) return;
        const cus = cusRes.data;
        setCustomer(cus);

        const [addrRes, cartRes, whRes, invRes] = await Promise.all([
          addressService.getByCustomer(cus.id),
          cartService.getCart(user.userId).catch(() => ({ data: { items: [] } })),
          api.get('/warehouses').catch(() => ({ data: [] })),
          api.get('/inventory').catch(() => ({ data: [] })),
        ]);
        if (ignore) return;
        setAddresses(addrRes.data);
        setWarehouses(Array.isArray(whRes.data) ? whRes.data : []);
        setInventory(Array.isArray(invRes.data) ? invRes.data : []);

        const defaultAddr = addrRes.data.find((a) => a.isDefault) || addrRes.data[0] || null;
        setSelectedAddr(defaultAddr);

        const items = Array.isArray(cartRes.data) ? cartRes.data : (cartRes.data?.items || []);
        setCartItems(items);
      } catch {
        message.error('Không thể tải thông tin');
      } finally {
        if (!ignore) { setPageLoading(false); setCartLoading(false); }
      }
    })();
    return () => { ignore = true; };
  }, [user, navigate]);

  const inventoryByProduct = useMemo(() => {
    const map = {};
    inventory.forEach((inv) => {
      if (!map[inv.productId]) map[inv.productId] = [];
      map[inv.productId].push(inv);
    });
    return map;
  }, [inventory]);

  useEffect(() => {
    if (!selectedAddr || !addresses.length) return;
    const addr = addresses.find((a) => a.id === selectedAddr);
    if (!addr) return;
    (async () => {
      let lat = addr.latitude;
      let lng = addr.longitude;
      if (lat == null || lng == null) {
        const fullAddr = `${addr.street}${addr.ward ? `, ${addr.ward}` : ''}, ${addr.city}`;
        const coords = await geocode(fullAddr);
        if (coords) { lat = coords.lat; lng = coords.lng; }
      }
      setCustomerLat(lat);
      setCustomerLng(lng);

      if (lat != null && lng != null && warehouses.length > 0) {
        const selections = {};
        let maxKm = 0;
        cartItems.forEach((item) => {
          const productId = item.productId || item.product?.id;
          const invRecords = inventoryByProduct[productId] || [];
          let best = null, bestKm = Infinity;
          invRecords.forEach((inv) => {
            if (inv.availableQuantity < item.quantity) return;
            const wh = warehouses.find((w) => w.id === inv.warehouseId);
            if (!wh || wh.latitude == null || wh.longitude == null) return;
            const km = haversine(lat, lng, wh.latitude, wh.longitude);
            if (km < bestKm) { bestKm = km; best = wh; }
          });
          if (best) {
            selections[productId] = best.id;
            if (bestKm > maxKm) maxKm = bestKm;
          }
        });
        setWarehouseSelections(selections);
        setShippingFee(maxKm > 0 ? calcShippingFee(maxKm) : 0);
      }
    })();
  }, [selectedAddr, addresses, warehouses, cartItems, inventoryByProduct]);

  const handleAddNewAddress = async () => {
    if (!customer) return;
    try {
      const values = await newAddrForm.validateFields();
      setAddingAddr(true);
      const fullAddr = `${values.street}${values.ward ? `, ${values.ward}` : ''}, ${values.city}`;
      const coords = await geocode(fullAddr);
      const payload = {
        customerId: customer.id,
        street: values.street,
        ward: values.ward || '',
        city: values.city,
        default: addresses.length === 0,
        latitude: coords?.lat || null,
        longitude: coords?.lng || null,
      };
      const res = await addressService.create(payload);
      setAddresses((prev) => [...prev, res.data]);
      setSelectedAddr(res.data.id);
      setShowNewForm(false);
      newAddrForm.resetFields();
      message.success('Đã thêm địa chỉ mới');
    } catch {
      // ignore
    } finally {
      setAddingAddr(false);
    }
  };

  const totalAmount = useMemo(() => cartItems.reduce(
    (sum, item) => sum + (item.unitPrice || item.product?.price || 0) * (item.quantity || 1), 0
  ), [cartItems]);

  const grandTotal = totalAmount + shippingFee;

  const handleSelectWarehouse = (productId, warehouseId) => {
    setWarehouseSelections((prev) => {
      const next = { ...prev, [productId]: warehouseId };
      let maxKm = 0;
      if (customerLat != null && customerLng != null) {
        cartItems.forEach((item) => {
          const pid = item.productId || item.product?.id;
          const wid = next[pid];
          if (!wid) return;
          const wh = warehouses.find((w) => w.id === wid);
          if (!wh || wh.latitude == null || wh.longitude == null) return;
          const km = haversine(customerLat, customerLng, wh.latitude, wh.longitude);
          if (km > maxKm) maxKm = km;
        });
      }
      setShippingFee(maxKm > 0 ? calcShippingFee(maxKm) : 0);
      return next;
    });
  };

  const handleSubmit = async (values) => {
    if (!selectedAddr) {
      message.warning('Vui lòng chọn địa chỉ giao hàng');
      return;
    }
    if (cartItems.length === 0) {
      message.warning('Giỏ hàng trống');
      return;
    }
    const addr = addresses.find((a) => a.id === selectedAddr);
    if (!addr) return;

    setProcessing(true);
    try {
      const orderPayload = {
        userId: user.userId,
        userName: values.fullName,
        userEmail: values.email,
        userPhone: values.phone,
        shippingAddress: `${addr.street}${addr.ward ? `, ${addr.ward}` : ''}, ${addr.city}`,
        note: values.note || '',
        shippingFee,
        city: addr.city,
        customerLat,
        customerLng,
        items: cartItems.map((item) => {
          const productId = item.productId || item.product?.id;
          const warehouseId = warehouseSelections[productId];
          const wh = warehouses.find((w) => w.id === warehouseId);
          return {
            productId,
            quantity: item.quantity,
            warehouseId: warehouseId || null,
            warehouseName: wh?.name || null,
          };
        }),
      };

      const orderRes = await orderService.create(orderPayload);
      const order = orderRes.data;
      const orderId = order.orderId;

      await cartService.clearCart(user.userId);
      refreshCartCount();

      try {
        await paymentService.create({ orderId, amount: totalAmount });
      } catch { /* ignore */ }

      message.success('Đặt hàng thành công!');
      navigate(`/orders/${orderId}`);
    } catch (err) {
      message.error(err.response?.data?.message || 'Đặt hàng thất bại');
    } finally {
      setProcessing(false);
    }
  };

  if (pageLoading) return <div style={{ textAlign: 'center', padding: 80 }}><Spin size="large" /></div>;

  const itemColumns = [
    {
      title: 'Sản phẩm', dataIndex: 'productName', key: 'product',
      render: (name, record) => (
        <Space>
          <img src={(() => {
            const img = record.productImg || record.product?.img;
            if (!img) return 'https://placehold.co/48x48';
            if (img.startsWith('http://') || img.startsWith('https://') || img.startsWith('/') || img.startsWith('uploads/')) return img;
            return `/uploads/${img}`;
          })()} alt={name} style={{ width: 48, height: 48, objectFit: 'cover', borderRadius: 8 }} />
          <Text strong style={{ fontSize: 13 }}>{name || record.product?.name}</Text>
        </Space>
      ),
    },
    { title: 'Đơn giá', dataIndex: 'unitPrice', key: 'price', width: 100, align: 'right',
      render: (price, record) => `${(price || record.product?.price || 0).toLocaleString('vi-VN')}₫` },
    { title: 'SL', dataIndex: 'quantity', key: 'qty', width: 50, align: 'center' },
    { title: 'Thành tiền', key: 'subtotal', width: 100, align: 'right',
      render: (_, record) => (
        <Text strong style={{ color: 'var(--primary)' }}>
          {((record.unitPrice || record.product?.price || 0) * (record.quantity || 1)).toLocaleString('vi-VN')}₫
        </Text>
      ),
    },
    {
      title: 'Chọn kho', key: 'warehouse', width: 220,
      render: (_, record) => {
        const productId = record.productId || record.product?.id;
        const invRecords = inventoryByProduct[productId] || [];
        const options = invRecords
          .filter((inv) => inv.availableQuantity >= record.quantity)
          .map((inv) => {
            const wh = warehouses.find((w) => w.id === inv.warehouseId);
            let label = wh?.name || `Kho #${inv.warehouseId}`;
            if (wh && customerLat != null && customerLng != null && wh.latitude != null && wh.longitude != null) {
              const km = haversine(customerLat, customerLng, wh.latitude, wh.longitude);
              label += ` (${km.toFixed(1)}km)`;
            }
            return { value: inv.warehouseId, label, disabled: inv.availableQuantity < record.quantity };
          });
        return (
          <Select
            size="small"
            placeholder="Chọn kho"
            style={{ width: 200 }}
            value={warehouseSelections[productId] || undefined}
            onChange={(v) => handleSelectWarehouse(productId, v)}
            options={options}
          />
        );
      },
    },
  ];

  return (
    <div style={{ maxWidth: 1100, margin: '0 auto', padding: '32px 20px' }}>
      <Button type="link" icon={<ArrowLeftOutlined />} onClick={() => navigate('/cart')} style={{ padding: 0, marginBottom: 16, fontWeight: 500 }}>
        Quay lại giỏ hàng
      </Button>

      <Title level={2} style={{ fontWeight: 700, marginBottom: 24, fontSize: 24 }}><CreditCardOutlined /> Thanh toán</Title>

      <Row gutter={[32, 24]}>
        <Col xs={24} md={16}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 32, border: '1px solid var(--gray-100)', marginBottom: 24 }}>
            <Title level={5} style={{ fontWeight: 600, marginBottom: 16 }}>Sản phẩm trong đơn hàng</Title>
            <Table columns={itemColumns} dataSource={cartItems} rowKey={(r) => r.id || r.productId || r.product?.id || Math.random()} pagination={false} loading={cartLoading} size="small" />
          </div>

          <div style={{ background: '#fff', borderRadius: 12, padding: 32, border: '1px solid var(--gray-100)' }}>
            <Form form={form} layout="vertical" onFinish={handleSubmit} size="large" requiredMark={false}
              initialValues={{ paymentMethod: 'cod', fullName: customer?.fullName || '', phone: customer?.phone || '', email: customer?.email || '' }}
            >
              <Title level={5} style={{ fontWeight: 600, marginBottom: 20 }}>Thông tin khách hàng</Title>
              <Row gutter={16}>
                <Col span={12}>
                  <Form.Item name="fullName" label="Họ và tên" rules={[{ required: true, message: 'Vui lòng nhập họ tên' }]}>
                    <Input prefix={<UserOutlined />} placeholder="Nguyễn Văn A" />
                  </Form.Item>
                </Col>
                <Col span={12}>
                  <Form.Item name="phone" label="Số điện thoại" rules={[{ required: true, message: 'Vui lòng nhập số điện thoại' }]}>
                    <Input prefix={<PhoneOutlined />} placeholder="0987654321" />
                  </Form.Item>
                </Col>
              </Row>
              <Form.Item name="email" label="Email" rules={[{ required: true, message: 'Vui lòng nhập email' }, { type: 'email', message: 'Email không hợp lệ' }]}>
                <Input prefix={<MailOutlined />} placeholder="email@example.com" />
              </Form.Item>

              <Divider />
              <Title level={5} style={{ fontWeight: 600, marginBottom: 20 }}><EnvironmentOutlined /> Địa chỉ giao hàng</Title>

              {addresses.length > 0 && (
                <Radio.Group value={selectedAddr} onChange={(e) => { setSelectedAddr(e.target.value); setShowNewForm(false); }} style={{ width: '100%' }}>
                  <Space direction="vertical" size={8} style={{ width: '100%' }}>
                    {addresses.map((addr) => (
                      <Card key={addr.id} size="small" style={{ borderRadius: 8, border: selectedAddr === addr.id ? '2px solid var(--primary)' : '1px solid var(--gray-200)', cursor: 'pointer' }}
                        onClick={() => { setSelectedAddr(addr.id); setShowNewForm(false); }}>
                        <Radio value={addr.id}>
                          <Text strong>{addr.street}{addr.ward ? `, ${addr.ward}` : ''}, {addr.city}</Text>
                          {addr.isDefault && <Text style={{ color: 'var(--primary)', marginLeft: 8, fontSize: 12 }}>(Mặc định)</Text>}
                        </Radio>
                      </Card>
                    ))}
                  </Space>
                </Radio.Group>
              )}

              {showNewForm ? (
                <Card size="small" style={{ marginTop: 12, borderRadius: 8, background: 'var(--gray-50)' }}>
                  <Form form={newAddrForm} layout="vertical">
                    <Form.Item name="street" label="Số nhà, tên đường" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
                      <Input placeholder="123 Nguyễn Huệ" />
                    </Form.Item>
                    <Form.Item name="ward" label="Phường / Xã">
                      <Input placeholder="Phường Bến Nghé" />
                    </Form.Item>
                    <Form.Item name="city" label="Tỉnh / Thành phố" rules={[{ required: true, message: 'Vui lòng nhập' }]}>
                      <Input placeholder="TP. Hồ Chí Minh" />
                    </Form.Item>
                    <Space>
                      <Button type="primary" onClick={handleAddNewAddress} loading={addingAddr}>Lưu & chọn</Button>
                      <Button onClick={() => setShowNewForm(false)}>Hủy</Button>
                    </Space>
                  </Form>
                </Card>
              ) : (
                <Button type="dashed" icon={<PlusOutlined />} onClick={() => setShowNewForm(true)} style={{ marginTop: 12 }}>
                  Thêm địa chỉ mới
                </Button>
              )}

              <Divider />
              <Form.Item name="note" label="Ghi chú">
                <Input.TextArea placeholder="Ghi chú cho đơn hàng..." rows={2} />
              </Form.Item>

              <Divider />
              <Title level={5} style={{ fontWeight: 600, marginBottom: 20 }}>Phương thức thanh toán</Title>
              <Form.Item name="paymentMethod">
                <Select options={[
                  { value: 'cod', label: 'Thanh toán khi nhận hàng (COD)' },
                  { value: 'bank', label: 'Chuyển khoản ngân hàng' },
                  { value: 'card', label: 'Thẻ tín dụng / Ghi nợ' },
                ]} />
              </Form.Item>

              <Button type="primary" htmlType="submit" block size="large" loading={processing} icon={<ShoppingCartOutlined />}
                style={{ height: 48, fontWeight: 600, fontSize: 16, borderRadius: 10, marginTop: 8 }} disabled={cartItems.length === 0}>
                {processing ? 'Đang xử lý...' : `Đặt hàng (${grandTotal.toLocaleString('vi-VN')}₫)`}
              </Button>
            </Form>
          </div>
        </Col>

        <Col xs={24} md={8}>
          <div style={{ background: '#fff', borderRadius: 12, padding: 24, border: '1px solid var(--gray-100)', position: 'sticky', top: 88 }}>
            <Title level={5} style={{ fontWeight: 600, marginBottom: 16 }}>Thông tin đơn hàng</Title>
            <Space direction="vertical" size={12} style={{ width: '100%' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <Text style={{ color: 'var(--gray-500)' }}>Số lượng</Text>
                <Text strong>{cartItems.reduce((s, i) => s + (i.quantity || 1), 0)} sản phẩm</Text>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <Text style={{ color: 'var(--gray-500)' }}>Tạm tính</Text>
                <Text strong>{totalAmount.toLocaleString('vi-VN')}₫</Text>
              </div>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                <Text style={{ color: 'var(--gray-500)' }}>Phí vận chuyển</Text>
                <Text strong style={{ color: 'var(--primary)' }}>{shippingFee > 0 ? `${shippingFee.toLocaleString('vi-VN')}₫` : '—'}</Text>
              </div>
            </Space>
            <Divider style={{ margin: '16px 0' }} />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Text strong style={{ fontSize: 15 }}>Tổng cộng</Text>
              <Title level={4} style={{ color: 'var(--primary)', margin: 0, fontSize: 22 }}>
                {grandTotal.toLocaleString('vi-VN')}₫
              </Title>
            </div>
            <div style={{ marginTop: 16, padding: 12, background: 'var(--gray-50)', borderRadius: 8, fontSize: 12, color: 'var(--gray-500)', lineHeight: 1.6 }}>
              Bạn chỉ cần thanh toán khi nhận được hàng. Đơn hàng sẽ được giao trong vòng 3-5 ngày làm việc.
            </div>
          </div>
        </Col>
      </Row>
    </div>
  );
}
