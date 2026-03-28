package com.vms.orders.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vms.orders.dto.OrderRequest;
import com.vms.orders.dto.OrderResponse;
import com.vms.orders.dto.PageResponse;
import com.vms.orders.entity.OrderStatus;
import com.vms.orders.entity.PurchaseOrder;
import com.vms.orders.service.OrderService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderDetails(id));
    }
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping("/vendor/{vendorId}")
    public PageResponse<OrderResponse> getOrders(
            @PathVariable String vendorId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {

        Page<PurchaseOrder> orderPage =
                orderService.getVendorOrders(vendorId, status, page, size);

        // 🔥 MAP ENTITY → DTO
        List<OrderResponse> content = orderPage.getContent().stream().map(order -> {

            OrderResponse res = new OrderResponse();
            res.setId(order.getId());
            res.setVendorId(order.getVendorId());
            res.setStatus(order.getStatus().name());
            res.setCreatedAt(order.getCreatedAt());
            res.setTotalAmount(order.getTotalAmount());

            return res;

        }).toList();

        // 🔥 BUILD PageResponse
        PageResponse<OrderResponse> response = new PageResponse<>();
        response.setContent(content);
        response.setTotalPages(orderPage.getTotalPages());
        response.setTotalElements(orderPage.getTotalElements());

        return response;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status
    ) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }
    
    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }
}
