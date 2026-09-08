package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.StoreItem;
import com.seu.petadoptionservice.service.CartService;
import com.seu.petadoptionservice.service.StoreService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

@Controller
@RequestMapping("/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;
    private final CartService cartService;

    @GetMapping
    public String browseStore(Model model, HttpSession session) {
        model.addAttribute("items", storeService.getActiveItems());
        model.addAttribute("cartItemCount", cartService.getCartItemCount(session));
        return "store";
    }

    @PostMapping("/cart/add/{itemId}")
    public String addToCart(@PathVariable Long itemId, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.addToCart(session, itemId, 1);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/store";
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        Map<Long, Integer> cart = cartService.getCart(session);
        model.addAttribute("cart", cart);
        model.addAttribute("cartItemCount", cartService.getCartItemCount(session));
        model.addAttribute("cartTotal", cartService.getCartTotal(session));

        List<StoreItem> allItems = storeService.getAllItems();
        Map<Long, StoreItem> itemsById = new LinkedHashMap<>();
        for (StoreItem item : allItems) {
            itemsById.put(item.getId(), item);
        }
        model.addAttribute("items", itemsById);

        List<CartLine> cartLines = new java.util.ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            StoreItem item = itemsById.get(entry.getKey());
            if (item != null) {
                int quantity = entry.getValue();
                java.math.BigDecimal lineTotal = item.getPrice().multiply(new java.math.BigDecimal(quantity));
                cartLines.add(new CartLine(item.getId(), item, quantity, lineTotal));
            }
        }
        model.addAttribute("cartLines", cartLines);

        return "cart";
    }

    /** Simple view-model row for the cart page: avoids ambiguous SpEL BigDecimal.valueOf overload resolution in Thymeleaf. */
    public static class CartLine {
        private final Long itemId;
        private final StoreItem item;
        private final int quantity;
        private final java.math.BigDecimal lineTotal;

        public CartLine(Long itemId, StoreItem item, int quantity, java.math.BigDecimal lineTotal) {
            this.itemId = itemId;
            this.item = item;
            this.quantity = quantity;
            this.lineTotal = lineTotal;
        }

        public Long getItemId() { return itemId; }
        public StoreItem getItem() { return item; }
        public int getQuantity() { return quantity; }
        public java.math.BigDecimal getLineTotal() { return lineTotal; }
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam Long itemId, @RequestParam Integer quantity, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItem(session, itemId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/store/cart";
    }

    @PostMapping("/cart/remove/{itemId}")
    public String removeFromCart(@PathVariable Long itemId, HttpSession session, RedirectAttributes redirectAttributes) {
        cartService.removeFromCart(session, itemId);
        redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
        return "redirect:/store/cart";
    }
}
