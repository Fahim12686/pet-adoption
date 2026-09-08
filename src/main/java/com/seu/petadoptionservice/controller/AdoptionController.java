package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.AdoptionApplication;
import com.seu.petadoptionservice.entity.Pet;
import com.seu.petadoptionservice.entity.User;
import com.seu.petadoptionservice.repository.AdoptionApplicationRepository;
import com.seu.petadoptionservice.repository.UserRepository;
import com.seu.petadoptionservice.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AdoptionController {

    private final AdoptionApplicationRepository applicationRepository;
    private final PetService petService;
    private final UserRepository userRepository;

    @PostMapping("/adopt/{petId}")
    public String applyToAdopt(@PathVariable Long petId, @RequestParam String reason,
                                Principal principal, RedirectAttributes redirectAttributes) {
        Optional<Pet> petOpt = petService.findById(petId);
        if (petOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("dashboardError", "That pet is no longer available.");
            return "redirect:/pets";
        }
        Pet pet = petOpt.get();

        if (reason == null || reason.isBlank()) {
            redirectAttributes.addFlashAttribute("petDetailError", "Please tell us why you'd be a good fit before applying.");
            return "redirect:/pets/" + petId;
        }

        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("dashboardError", "We couldn't verify your account. Please log in again.");
            return "redirect:/signin";
        }
        User user = userOpt.get();

        if (!"AVAILABLE".equals(pet.getStatus())) {
            redirectAttributes.addFlashAttribute("petDetailError", "This pet already has a pending or completed application.");
            return "redirect:/pets/" + petId;
        }

        List<AdoptionApplication> existing = applicationRepository.findByUserId(user.getId());
        boolean alreadyApplied = existing.stream()
                .anyMatch(a -> a.getPet() != null && a.getPet().getId().equals(petId)
                        && !"REJECTED".equals(a.getStatus()));
        if (alreadyApplied) {
            redirectAttributes.addFlashAttribute("dashboardError", "You've already applied to adopt this pet.");
            return "redirect:/dashboard";
        }

        AdoptionApplication app = AdoptionApplication.builder()
                .user(user)
                .pet(pet)
                .reason(reason)
                .status("PENDING")
                .build();

        applicationRepository.save(app);

        pet.setStatus("PENDING");
        petService.savePet(pet);

        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Optional<User> userOpt = userRepository.findByEmail(principal.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/signin";
        }
        User user = userOpt.get();
        model.addAttribute("user", user);
        model.addAttribute("applications", applicationRepository.findByUserId(user.getId()));
        return "dashboard";
    }
}