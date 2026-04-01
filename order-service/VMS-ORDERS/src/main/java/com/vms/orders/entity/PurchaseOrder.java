package com.vms.orders.entity;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.*;
import lombok.Data;
@Entity
@Table(name="purchase_orders")
@Data
public class PurchaseOrder {
    @Id
    @GeneratedValue
    private Long id;
    private String vendorId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;
    private Double totalAmount;
}