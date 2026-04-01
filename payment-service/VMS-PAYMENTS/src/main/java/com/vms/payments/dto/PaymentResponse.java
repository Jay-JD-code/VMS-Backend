package com.vms.payments.dto;
import java.time.LocalDateTime;
import lombok.Data;
@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String vendorId;
    private String vendorName; 
    private Double amount;
    private String method;
    private String status;
    private LocalDateTime createdAt;
}