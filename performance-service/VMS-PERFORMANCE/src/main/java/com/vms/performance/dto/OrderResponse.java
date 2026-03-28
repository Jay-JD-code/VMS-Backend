package com.vms.performance.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OrderResponse {
    /** Matches the 'id' field returned by VMS-ORDERS */
    private Long id;
    private String vendorId;
    private String status;
    private LocalDateTime createdAt;
    /** Timestamp of the last status change — used for accurate delivery-time measurement. */
    private LocalDateTime updatedAt;
}
