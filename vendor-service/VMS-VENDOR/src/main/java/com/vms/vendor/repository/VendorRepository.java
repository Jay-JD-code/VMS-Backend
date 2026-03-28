package com.vms.vendor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.vms.vendor.entity.VendorEntity;

@Repository
public interface VendorRepository extends JpaRepository<VendorEntity, UUID> {

	Optional<VendorEntity> findByEmail(String email);
	Optional<VendorEntity> findFirstByEmailIgnoreCase(String email);
}
