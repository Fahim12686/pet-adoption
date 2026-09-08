package com.seu.petadoptionservice.repository;

import com.seu.petadoptionservice.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByStatus(String status);
    List<Pet> findByStatusOrderByIdAsc(String status);
    List<Pet> findBySpeciesContainingIgnoreCase(String species);
    List<Pet> findAllByOrderByIdAsc();

    @Query("SELECT p FROM Pet p WHERE p.status = :status " +
           "AND (CAST(:search AS string) IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) OR LOWER(p.breed) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
           "AND (CAST(:species AS string) IS NULL OR p.species = CAST(:species AS string)) " +
           "AND (CAST(:gender AS string) IS NULL OR p.gender = CAST(:gender AS string)) " +
           "AND (:maxFee IS NULL OR p.adoptionFee <= :maxFee) " +
           "ORDER BY p.id ASC")
    List<Pet> searchAvailablePets(@Param("status") String status,
                                   @Param("search") String search,
                                   @Param("species") String species,
                                   @Param("gender") String gender,
                                   @Param("maxFee") BigDecimal maxFee);
}