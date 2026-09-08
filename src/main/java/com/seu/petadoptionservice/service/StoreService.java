package com.seu.petadoptionservice.service;

import com.seu.petadoptionservice.entity.StoreItem;
import com.seu.petadoptionservice.repository.StoreItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreItemRepository storeItemRepository;

    public List<StoreItem> getAllItems() {
        return storeItemRepository.findAllByOrderByIdAsc();
    }

    public List<StoreItem> getActiveItems() {
        return storeItemRepository.findByActiveTrueOrderByIdAsc();
    }

    /** Throws if not found - use only where the id is already known-good. */
    public StoreItem getItemById(Long id) {
        return storeItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Store item not found"));
    }

    /** Safe lookup for anywhere the id could be invalid/stale. */
    public Optional<StoreItem> findById(Long id) {
        return storeItemRepository.findById(id);
    }

    public void saveItem(StoreItem item) {
        storeItemRepository.save(item);
    }

    public void deleteItem(Long id) {
        storeItemRepository.deleteById(id);
    }
}
