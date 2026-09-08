package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.AdoptionApplication;
import com.seu.petadoptionservice.repository.AdoptionApplicationRepository;
import com.seu.petadoptionservice.service.OrderService;
import com.seu.petadoptionservice.service.PetService;
import com.seu.petadoptionservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final PetService petService;
    private final UserService userService;
    private final AdoptionApplicationRepository applicationRepository;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/sales")
    public String manageSales(Model model) {
        model.addAttribute("soldItems", orderService.getAllSoldItems());
        return "admin-sales";
    }

    @GetMapping("/pets")
    public String managePets(Model model) {
        model.addAttribute("pets", petService.getAllPets());
        return "admin-pets";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @GetMapping("/applications")
    public String manageApplications(Model model) {
        model.addAttribute("applications", applicationRepository.findAll());
        return "admin-applications";
    }

    @PostMapping("/applications/{id}/approve")
    public String approveApplication(@PathVariable Long id) {
        AdoptionApplication app = applicationRepository.findById(id).orElseThrow();
        app.setStatus("APPROVED");
        applicationRepository.save(app);
        return "redirect:/admin/applications";
    }

    @PostMapping("/applications/{id}/reject")
    public String rejectApplication(@PathVariable Long id) {
        AdoptionApplication app = applicationRepository.findById(id).orElseThrow();
        app.setStatus("REJECTED");
        applicationRepository.save(app);

        // Revert pet status
        app.getPet().setStatus("AVAILABLE");
        petService.savePet(app.getPet());

        return "redirect:/admin/applications";
    }

    @PostMapping("/users/{id}/suspend")
    public String suspendUser(@PathVariable Long id) {
        userService.updateUserStatus(id, "SUSPENDED");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/unsuspend")
    public String unsuspendUser(@PathVariable Long id) {
        userService.updateUserStatus(id, "ACTIVE");
        return "redirect:/admin/users";
    }
}