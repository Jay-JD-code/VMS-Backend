package com.vms.performance.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vms.performance.entity.VendorPerformance;
public interface VendorPerformanceRepository extends JpaRepository<VendorPerformance, String> {
}
