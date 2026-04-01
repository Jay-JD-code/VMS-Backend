package com.vms.vendor.controller;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vms.vendor.entity.VendorItem;
import com.vms.vendor.repository.VendorItemRepository;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("api/vendors/{vendorId}/items")
@RequiredArgsConstructor
public class VendorItemController {
    private final VendorItemRepository itemRepository;
    
    @GetMapping
    public ResponseEntity<List<VendorItem>> getItems(@PathVariable String vendorId) {
        return ResponseEntity.ok(itemRepository.findByVendorId(vendorId));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<VendorItem>> getAvailableItems(@PathVariable String vendorId) {
        return ResponseEntity.ok(itemRepository.findByVendorIdAndAvailableTrue(vendorId));
    }
    
    @PostMapping
    public ResponseEntity<VendorItem> addItem(
            @PathVariable String vendorId,
            @RequestBody VendorItem item) {
        item.setId(null);            
        item.setVendorId(vendorId);
        item.setCreatedAt(LocalDateTime.now());
        if (item.getAvailable() == null) item.setAvailable(true);
        return ResponseEntity.ok(itemRepository.save(item));
    }
    
    @PutMapping("/{itemId}")
    public ResponseEntity<VendorItem> updateItem(
            @PathVariable String vendorId,
            @PathVariable UUID itemId,
            @RequestBody VendorItem update) {
        VendorItem existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existing.getVendorId().equals(vendorId)) {
            return ResponseEntity.status(403).build();
        }
        existing.setName(update.getName());
        existing.setDescription(update.getDescription());
        existing.setUnit(update.getUnit());
        existing.setUnitPrice(update.getUnitPrice());
        existing.setAvailable(update.getAvailable() != null ? update.getAvailable() : existing.getAvailable());
        return ResponseEntity.ok(itemRepository.save(existing));
    }
    
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable String vendorId,
            @PathVariable UUID itemId) {
        VendorItem existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existing.getVendorId().equals(vendorId)) {
            return ResponseEntity.status(403).build();
        }
        itemRepository.delete(existing);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{itemId}/toggle")
    public ResponseEntity<VendorItem> toggleAvailability(
            @PathVariable String vendorId,
            @PathVariable UUID itemId) {
        VendorItem existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));
        if (!existing.getVendorId().equals(vendorId)) {
            return ResponseEntity.status(403).build();
        }
        existing.setAvailable(!Boolean.TRUE.equals(existing.getAvailable()));
        return ResponseEntity.ok(itemRepository.save(existing));
    }
}
