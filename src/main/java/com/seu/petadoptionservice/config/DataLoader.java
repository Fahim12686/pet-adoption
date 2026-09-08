package com.seu.petadoptionservice.config;

import com.seu.petadoptionservice.entity.Pet;
import com.seu.petadoptionservice.entity.StoreItem;
import com.seu.petadoptionservice.entity.User;
import com.seu.petadoptionservice.repository.PetRepository;
import com.seu.petadoptionservice.repository.StoreItemRepository;
import com.seu.petadoptionservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final StoreItemRepository storeItemRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .name("System Admin")
                    .email("admin@email.com")
                    .password(passwordEncoder.encode("admin123"))
                    .roles(Set.of("ROLE_ADMIN", "ROLE_STAFF"))
                    .status("ACTIVE")
                    .build();
            userRepository.save(admin);

            User user = User.builder()
                    .name("Test User")
                    .email("user@email.com")
                    .password(passwordEncoder.encode("user123"))
                    .roles(Set.of("ROLE_USER"))
                    .status("ACTIVE")
                    .build();
            userRepository.save(user);
        } else {
            userRepository.findByEmail("admin@petcenter.com").ifPresent(oldAdmin -> {
                oldAdmin.setEmail("admin@email.com");
                userRepository.save(oldAdmin);
            });
            userRepository.findByEmail("user@test.com").ifPresent(oldUser -> {
                oldUser.setEmail("user@email.com");
                userRepository.save(oldUser);
            });
        }

        if (petRepository.count() == 0) {
            petRepository.save(Pet.builder().name("Bella").species("Dog").breed("Labrador").age(3).gender("Female").adoptionFee(new BigDecimal("150.00")).status("AVAILABLE").description("Friendly and energetic.").build());
            petRepository.save(Pet.builder().name("Luna").species("Cat").breed("Persian").age(2).gender("Female").adoptionFee(new BigDecimal("75.00")).status("AVAILABLE").description("Loves to nap.").build());
            petRepository.save(Pet.builder().name("Charlie").species("Dog").breed("Beagle").age(1).gender("Male").adoptionFee(new BigDecimal("200.00")).status("AVAILABLE").description("Curious and playful.").build());
            petRepository.save(Pet.builder().name("Milo").species("Cat").breed("Siamese").age(4).gender("Male").adoptionFee(new BigDecimal("80.00")).status("PENDING").description("Very vocal and loving.").build());
            petRepository.save(Pet.builder().name("Daisy").species("Rabbit").breed("Holland Lop").age(1).gender("Female").adoptionFee(new BigDecimal("40.00")).status("AVAILABLE").description("Sweet and shy.").build());
            petRepository.save(Pet.builder().name("Max").species("Dog").breed("German Shepherd").age(5).gender("Male").adoptionFee(new BigDecimal("100.00")).status("ADOPTED").description("Loyal and protective.").build());
        }

        if (storeItemRepository.count() == 0) {
            storeItemRepository.save(StoreItem.builder().name("Premium Dry Dog Food (5kg)").description("Balanced nutrition for adult dogs, chicken & rice formula.").price(new BigDecimal("24.99")).category("FOOD").stockQty(50).build());
            storeItemRepository.save(StoreItem.builder().name("Grain-Free Cat Food (3kg)").description("Salmon-based recipe, gentle on sensitive stomachs.").price(new BigDecimal("19.99")).category("FOOD").stockQty(40).build());
            storeItemRepository.save(StoreItem.builder().name("Rope Chew Toy").description("Durable cotton rope for tug-of-war and teeth cleaning.").price(new BigDecimal("6.50")).category("TOYS").stockQty(75).build());
            storeItemRepository.save(StoreItem.builder().name("Feather Wand Cat Toy").description("Interactive teaser wand to keep cats active and engaged.").price(new BigDecimal("8.00")).category("TOYS").stockQty(60).build());
            storeItemRepository.save(StoreItem.builder().name("Adjustable Nylon Leash").description("Comfortable padded handle, fits small to large dogs.").price(new BigDecimal("14.99")).category("ACCESSORIES").stockQty(30).build());
            storeItemRepository.save(StoreItem.builder().name("Reflective Pet Collar").description("Adjustable, reflective for visibility on evening walks.").price(new BigDecimal("9.99")).category("ACCESSORIES").stockQty(45).build());
        }
    }
}
