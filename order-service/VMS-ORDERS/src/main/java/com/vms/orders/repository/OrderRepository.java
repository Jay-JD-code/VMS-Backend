package com.vms.orders.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.vms.orders.entity.OrderStatus;
import com.vms.orders.entity.PurchaseOrder;
public interface OrderRepository extends JpaRepository<PurchaseOrder, Long> {
	Page<PurchaseOrder> findByVendorId(String vendorId, Pageable pageable);
	  Page<PurchaseOrder> findByVendorIdAndStatus(
	            String vendorId,
	            OrderStatus status,
	            Pageable pageable
	    );
}
