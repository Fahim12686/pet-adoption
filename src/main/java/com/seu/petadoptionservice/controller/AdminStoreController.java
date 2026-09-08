package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.StoreItem;
import com.seu.petadoptionservice.service.CloudinaryImageStorageService;
import com.seu.petadoptionservice.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/store")
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreService storeService;
    private final CloudinaryImageStorageService imageStorageService;

    @GetMapping
    public String manageItems(Model model) {
        model.addAttribute("items", storeService.getAllItems());
        return "admin-store";
    }

    @GetMapping("/new")
    public String newItemForm(Model model) {
        StoreItem item = new StoreItem();
        item.setActive(true);
        item.setStockQty(0);
        model.addAttribute("item", item);
        return "admin-store-form";
    }

    @GetMapping("/edit/{id}")
    public String editItemForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<StoreItem> item = storeService.findById(id);
        if (item.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminStoreError", "That item couldn't be found — it may already have been deleted.");
            return "redirect:/admin/store";
        }
        model.addAttribute("item", item.get());
        return "admin-store-form";
    }

    @PostMapping("/save")
    public String saveItem(@ModelAttribute StoreItem item,
                            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                            Model model) {
        // If editing and left blank, keep the existing image rather than wiping it.
        if (item.getId() != null && (item.getImageUrl() == null || item.getImageUrl().isBlank())
                && (imageFile == null || imageFile.isEmpty())) {
            Optional<StoreItem> existing = storeService.findById(item.getId());
            existing.ifPresent(value -> item.setImageUrl(value.getImageUrl()));
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                item.setImageUrl(imageStorageService.upload(imageFile, "store"));
            } catch (Exception e) {
                model.addAttribute("item", item);
                model.addAttribute("imageError", e.getMessage());
                return "admin-store-form";
            }
        }

        storeService.saveItem(item);
        return "redirect:/admin/store";
    }

    @GetMapping("/delete/{id}")
    public String deleteItem(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (storeService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("adminStoreError", "That item was already deleted.");
            return "redirect:/admin/store";
        }
        storeService.deleteItem(id);
        return "redirect:/admin/store";
    }
}
