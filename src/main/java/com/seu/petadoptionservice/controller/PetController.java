package com.seu.petadoptionservice.controller;

import com.seu.petadoptionservice.entity.Pet;
import com.seu.petadoptionservice.service.CloudinaryImageStorageService;
import com.seu.petadoptionservice.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final CloudinaryImageStorageService imageStorageService;

    @GetMapping
    public String listPets(@RequestParam(required = false) String search,
                            @RequestParam(required = false) String species,
                            @RequestParam(required = false) String gender,
                            @RequestParam(required = false) String maxFee,
                            Model model) {
        java.math.BigDecimal maxFeeValue = null;
        if (maxFee != null && !maxFee.isBlank()) {
            try {
                maxFeeValue = new java.math.BigDecimal(maxFee.trim());
            } catch (NumberFormatException ignored) {
                // leave maxFeeValue as null if the input isn't a valid number
            }
        }
        model.addAttribute("pets", petService.searchAvailablePets(search, species, gender, maxFeeValue));
        model.addAttribute("availableSpecies", petService.getAvailableSpecies());
        model.addAttribute("search", search);
        model.addAttribute("species", species);
        model.addAttribute("gender", gender);
        model.addAttribute("maxFee", maxFee);
        return "pet-list";
    }

    @GetMapping("/{id}")
    public String petDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pet> pet = petService.findById(id);
        if (pet.isEmpty()) {
            redirectAttributes.addFlashAttribute("petListError", "That pet couldn't be found — it may have been removed.");
            return "redirect:/pets";
        }
        model.addAttribute("pet", pet.get());
        return "pet-detail";
    }

    @GetMapping("/new")
    public String newPetForm(Model model) {
        model.addAttribute("pet", new Pet());
        return "pet-form";
    }

    @PostMapping("/save")
    public String savePet(@ModelAttribute Pet pet,
                           @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (pet.getStatus() == null) pet.setStatus("AVAILABLE");

        // If admin is editing and left the image blank, keep the existing image
        // rather than wiping it out.
        if (pet.getId() != null && (pet.getImageUrl() == null || pet.getImageUrl().isBlank())
                && (imageFile == null || imageFile.isEmpty())) {
            Optional<Pet> existing = petService.findById(pet.getId());
            existing.ifPresent(value -> pet.setImageUrl(value.getImageUrl()));
        }

        // An uploaded file takes priority over a manually typed URL.
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                pet.setImageUrl(imageStorageService.upload(imageFile, "pets"));
            } catch (Exception e) {
                model.addAttribute("pet", pet);
                model.addAttribute("imageError", e.getMessage());
                return "pet-form";
            }
        }

        petService.savePet(pet);
        return "redirect:/admin/pets";
    }

    @GetMapping("/edit/{id}")
    public String editPetForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Pet> pet = petService.findById(id);
        if (pet.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminPetsError", "That pet couldn't be found — it may already have been deleted.");
            return "redirect:/admin/pets";
        }
        model.addAttribute("pet", pet.get());
        return "pet-form";
    }

    @GetMapping("/delete/{id}")
    public String deletePet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (petService.findById(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("adminPetsError", "That pet was already deleted.");
            return "redirect:/admin/pets";
        }
        petService.deletePet(id);
        return "redirect:/admin/pets";
    }
}