package com.seu.petadoptionservice.service;

import com.seu.petadoptionservice.entity.Pet;
import com.seu.petadoptionservice.repository.PetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PetService {
    private final PetRepository petRepository;

    public List<Pet> getAllPets() {
        return petRepository.findAllByOrderByIdAsc();
    }

    public List<Pet> getAvailablePets() {
        return petRepository.findByStatusOrderByIdAsc("AVAILABLE");
    }

    public List<Pet> searchAvailablePets(String search, String species, String gender, java.math.BigDecimal maxFee) {
        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        String normalizedSpecies = (species == null || species.isBlank()) ? null : species;
        String normalizedGender = (gender == null || gender.isBlank()) ? null : gender;
        return petRepository.searchAvailablePets("AVAILABLE", normalizedSearch, normalizedSpecies, normalizedGender, maxFee);
    }

    /** Distinct species among available pets, for populating the filter dropdown. */
    public List<String> getAvailableSpecies() {
        return petRepository.findByStatusOrderByIdAsc("AVAILABLE").stream()
                .map(Pet::getSpecies)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    /**
     * Throws if not found. Use only where the id is known-good (e.g. right after
     * loading it from the same request) - for anything driven by a path variable
     * or form input, prefer {@link #findById(Long)} and handle the empty case
     * with a friendly redirect instead of letting this propagate into a 500.
     */
    public Pet getPetById(Long id) {
        return petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet not found"));
    }

    /** Safe lookup for anywhere the id could be invalid/stale (deleted, mistyped, etc). */
    public Optional<Pet> findById(Long id) {
        return petRepository.findById(id);
    }

    public void savePet(Pet pet) {
        petRepository.save(pet);
    }

    public void deletePet(Long id) {
        petRepository.deleteById(id);
    }
}