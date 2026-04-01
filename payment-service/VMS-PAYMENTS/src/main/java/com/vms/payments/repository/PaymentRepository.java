package com.vms.payments.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vms.payments.entity.PaymentEntity;
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByOrderId(Long orderId);
    List<PaymentEntity> findByVendorId(String vendorId);
}
