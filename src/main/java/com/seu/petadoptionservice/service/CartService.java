package com.seu.petadoptionservice.service;

import com.seu.petadoptionservice.entity.StoreItem;
import com.seu.petadoptionservice.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String CART_SESSION_KEY = "cart";
    private final StoreItemRepository storeItemRepository;

    public Map<Long, Integer> getCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public void addToCart(HttpSession session, Long itemId, Integer quantity) {
        Map<Long, Integer> cart = getCart(session);
        StoreItem item = storeItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        if (item.getStockQty() < quantity) {
            throw new RuntimeException("Not enough stock available");
        }

        cart.merge(itemId, quantity, Integer::sum);
    }

    public void updateCartItem(HttpSession session, Long itemId, Integer quantity) {
        Map<Long, Integer> cart = getCart(session);
        if (quantity <= 0) {
            cart.remove(itemId);
        } else {
            StoreItem item = storeItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            if (item.getStockQty() < quantity) {
                throw new RuntimeException("Not enough stock available");
            }
            cart.put(itemId, quantity);
        }
    }

    public void removeFromCart(HttpSession session, Long itemId) {
        Map<Long, Integer> cart = getCart(session);
        cart.remove(itemId);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    public int getCartItemCount(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    public BigDecimal getCartTotal(HttpSession session) {
        Map<Long, Integer> cart = getCart(session);
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            StoreItem item = storeItemRepository.findById(entry.getKey())
                    .orElse(null);
            if (item != null) {
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }
}
