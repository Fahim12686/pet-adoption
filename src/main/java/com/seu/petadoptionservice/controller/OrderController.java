package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.Order;
import com.seu.petadoptionservice.entity.User;
import com.seu.petadoptionservice.repository.UserRepository;
import com.seu.petadoptionservice.service.CartService;
import com.seu.petadoptionservice.service.OrderService;
import com.seu.petadoptionservice.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final StoreService storeService;
    private final UserRepository userRepository;

    @GetMapping("/checkout")
    public String checkout(Model model, HttpSession session) {
        Map<Long, Integer> cart = cartService.getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/store";
        }
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", cartService.getCartTotal(session));
        model.addAttribute("cartItemCount", cartService.getCartItemCount(session));

        List<StoreController.CartLine> cartLines = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            storeService.findById(entry.getKey()).ifPresent(item -> {
                int quantity = entry.getValue();
                BigDecimal lineTotal = item.getPrice().multiply(new BigDecimal(quantity));
                cartLines.add(new StoreController.CartLine(item.getId(), item, quantity, lineTotal));
            });
        }
        model.addAttribute("cartLines", cartLines);

        return "checkout";
    }

    @PostMapping("/checkout")
    public String processCheckout(@AuthenticationPrincipal UserDetails userDetails, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            Map<Long, Integer> cart = cartService.getCart(session);
            if (cart.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Your cart is empty!");
                return "redirect:/store";
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Order order = orderService.createOrder(user.getId(), cart);
            cartService.clearCart(session);

            return "redirect:/orders/confirmation/" + order.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/orders/checkout";
        }
    }

    @GetMapping("/confirmation/{orderId}")
    public String orderConfirmation(@PathVariable Long orderId, Model model) {
        Order order = orderService.findOrderById(orderId);
        model.addAttribute("order", order);
        return "order-confirmation";
    }

    @PostMapping("/payment/complete/{orderId}")
    @ResponseBody
    public String completePayment(@PathVariable Long orderId) {
        orderService.markOrderAsPaid(orderId);
        return "success";
    }

    @PostMapping("/quick-checkout")
    @ResponseBody
    public Map<String, Object> quickCheckout(@AuthenticationPrincipal UserDetails userDetails, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Map<Long, Integer> cart = cartService.getCart(session);
            if (cart.isEmpty()) {
                response.put("success", false);
                response.put("message", "Your cart is empty!");
                return response;
            }

            User user = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Order order = orderService.createOrder(user.getId(), cart);
            orderService.markOrderAsPaid(order.getId());
            cartService.clearCart(session);

            response.put("success", true);
            response.put("message", "Order processed successfully");
            response.put("orderId", order.getId());
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return response;
    }
}
