import { useState, useEffect, useCallback } from 'react';
import { cartService } from '../services/orderService';
import { useAuth } from './useAuth';
import { CartContext } from './CartContext';

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cartCount, setCartCount] = useState(0);

  const [fetching, setFetching] = useState(false);

  const refreshCartCount = useCallback(() => {
    if (!user || fetching) return Promise.resolve();
    setFetching(true);
    return cartService.getCart(user.userId)
      .then(res => {
        const items = res.data?.items || res.data || [];
        setCartCount(items.reduce((sum, item) => sum + (item.quantity || 1), 0));
      })
      .catch(() => setCartCount(0))
      .finally(() => setFetching(false));
  }, [user, fetching]);

  useEffect(() => {
    if (!user) return;
    let ignore = false;
    cartService.getCart(user.userId)
      .then(res => {
        if (ignore) return;
        const items = res.data?.items || res.data || [];
        setCartCount(items.reduce((sum, item) => sum + (item.quantity || 1), 0));
      })
      .catch(() => { if (!ignore) setCartCount(0); });
    return () => { ignore = true; };
  }, [user]);

  return (
    <CartContext.Provider value={{ cartCount, refreshCartCount }}>
      {children}
    </CartContext.Provider>
  );
}
