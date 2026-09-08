package com.seu.petadoptionservice.service;

import com.seu.petadoptionservice.entity.*;
import com.seu.petadoptionservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreItemRepository storeItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public Order createOrder(Long userId, Map<Long, Integer> cartItems) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = Order.builder()
                .user(user)
                .totalAmount(BigDecimal.ZERO)
                .status("PENDING")
                .transactionId("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .build();

        order = orderRepository.save(order);

        for (Map.Entry<Long, Integer> entry : cartItems.entrySet()) {
            StoreItem storeItem = storeItemRepository.findById(entry.getKey()).orElse(null);
            if (storeItem == null) {
                continue; // skip stale/removed cart items instead of failing the whole order
            }

            Integer quantity = entry.getValue();
            int availableQty = Math.min(quantity, storeItem.getStockQty());
            if (availableQty <= 0) {
                continue; // skip out-of-stock items instead of failing the whole order
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .storeItem(storeItem)
                    .quantity(availableQty)
                    .unitPrice(storeItem.getPrice())
                    .build();

            orderItemRepository.save(orderItem);

            BigDecimal itemTotal = storeItem.getPrice().multiply(BigDecimal.valueOf(availableQty));
            totalAmount = totalAmount.add(itemTotal);

            storeItem.setStockQty(storeItem.getStockQty() - availableQty);
            storeItemRepository.save(storeItem);
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    @Transactional
    public void markOrderAsPaid(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus("PAID");
        orderRepository.save(order);
    }

    public Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public java.util.List<OrderItem> getAllSoldItems() {
        return orderItemRepository.findAllByOrderByIdDesc();
    }
}
