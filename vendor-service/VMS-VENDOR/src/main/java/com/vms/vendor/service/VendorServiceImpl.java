package com.vms.vendor.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vms.vendor.client.AuthFeignClient;
import com.vms.vendor.dto.request.VendorRequest;
import com.vms.vendor.dto.response.VendorResponse;
import com.vms.vendor.entity.VendorEntity;
import com.vms.vendor.entity.VendorStatus;
import com.vms.vendor.exception.ResourceNotFoundException;
import com.vms.vendor.repository.VendorRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorServiceImpl implements VendorService {

    private final VendorRepository repository;
    private final ModelMapper mapper;
    private final AuthFeignClient authFeignClient;

    @Override
    @Transactional
    public VendorResponse createVendor(VendorRequest request) {

        // ✅ FIX: Only block if a non-rejected vendor with this email exists.
        //    REJECTED or orphaned PENDING records from failed attempts are
        //    cleaned up and replaced, not treated as duplicates.
        repository.findFirstByEmailIgnoreCase(request.getEmail()).ifPresent(existing -> {
            if (existing.getStatus() != VendorStatus.REJECTED) {
                throw new RuntimeException(
                    "A vendor with email " + request.getEmail() + " already exists.");
            }
            // Clean up the old rejected/orphaned record before creating a fresh one
            log.info("Removing previous {} vendor record for {} before re-creating",
                    existing.getStatus(), request.getEmail());
            repository.delete(existing);
            repository.flush(); // ensure delete is committed before insert
        });

        VendorEntity vendor = mapper.map(request, VendorEntity.class);
        vendor.setStatus(VendorStatus.PENDING);
        vendor.setCreatedAt(LocalDateTime.now());
        vendor = repository.save(vendor);

        // ✅ FIX: Catch auth service errors so vendor creation never fails
        //    due to a duplicate or unavailable auth service
        try {
            authFeignClient.createVendorUser(Map.of("email", request.getEmail()));
        } catch (FeignException.BadRequest e) {
            log.warn("Auth user already exists for {} — skipping: {}", request.getEmail(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to create auth user for {}: {}", request.getEmail(), e.getMessage());
        }

        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public List<VendorResponse> getAllVendors() {
        return repository.findAll()
                .stream()
                .map(vendor -> mapper.map(vendor, VendorResponse.class))
                .toList();
    }

    @Override
    public VendorResponse approveVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStatus(VendorStatus.APPROVED);
        repository.save(vendor);
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public VendorResponse rejectVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStatus(VendorStatus.REJECTED);
        repository.save(vendor);
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public VendorResponse suspendVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        vendor.setStatus(VendorStatus.SUSPENDED);
        repository.save(vendor);
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public VendorResponse getByEmail(String email) {
        VendorEntity vendor = repository.findFirstByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vendor not found for email: " + email));
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public VendorResponse getVendorById(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        return mapper.map(vendor, VendorResponse.class);
    }

    @Override
    public void deleteVendor(UUID id) {
        VendorEntity vendor = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vendor not found"));

        if (vendor.getStatus() != VendorStatus.REJECTED) {
            throw new RuntimeException(
                "Only REJECTED vendors can be deleted. Current status: " + vendor.getStatus());
        }

        repository.delete(vendor);
    }
}