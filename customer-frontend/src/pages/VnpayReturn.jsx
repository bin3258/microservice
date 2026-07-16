import { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Typography, Spin, Result, Button } from 'antd';
import api from '../api/axiosClient';

const { Text } = Typography;

export default function VnpayReturnPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const [status, setStatus] = useState('loading');
  const [orderId, setOrderId] = useState(null);

  useEffect(() => {
    const params = {};
    searchParams.forEach((val, key) => { params[key] = val; });

    const txnRef = params.vnp_TxnRef;
    const orderIdFromRef = txnRef ? txnRef.split('_')[0] : null;
    setOrderId(orderIdFromRef);

    api.get('/payments/vnpay/return', { params })
      .then((res) => {
        const s = res.data.status === 'COMPLETED' ? 'success' : 'fail';
        setStatus(s);
      })
      .catch(() => {
        setStatus('fail');
      });
  }, [searchParams]);

  useEffect(() => {
    if (status !== 'loading' && orderId) {
      const timer = setTimeout(() => navigate(`/orders/${orderId}`), 5000);
      return () => clearTimeout(timer);
    }
  }, [status, orderId, navigate]);

  if (status === 'loading') {
    return (
      <div style={{ textAlign: 'center', padding: 100 }}>
        <Spin size="large" />
        <br /><br />
        <Text>Đang xác nhận thanh toán...</Text>
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 500, margin: '60px auto', padding: 20 }}>
      {status === 'success' ? (
        <Result
          status="success"
          title="Thanh toán thành công!"
          subTitle={`Đơn hàng #${orderId} đã được thanh toán qua VNPay.`}
          extra={[
            <Button type="primary" key="detail" onClick={() => navigate(`/orders/${orderId}`)}>
              Xem chi tiết đơn hàng
            </Button>,
            <Button key="shop" onClick={() => navigate('/shop')}>
              Tiếp tục mua sắm
            </Button>,
          ]}
        />
      ) : (
        <Result
          status="error"
          title="Thanh toán thất bại"
          subTitle="Giao dịch không thành công. Vui lòng thử lại."
          extra={[
            <Button type="primary" key="orders" onClick={() => navigate('/orders')}>
              Xem đơn hàng
            </Button>,
            <Button key="retry" onClick={() => navigate('/checkout')}>
              Thanh toán lại
            </Button>,
          ]}
        />
      )}
      <div style={{ textAlign: 'center', marginTop: 16 }}>
        <Text style={{ color: 'var(--gray-400)', fontSize: 13 }}>
          Tự động chuyển về chi tiết đơn hàng sau 5 giây...
        </Text>
      </div>
    </div>
  );
}
