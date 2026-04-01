package com.vms.orders.service;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.vms.orders.client.VendorClient;
import com.vms.orders.dto.OrderItemResponse;
import com.vms.orders.dto.OrderRequest;
import com.vms.orders.dto.OrderResponse;
import com.vms.orders.dto.PageResponse;
import com.vms.orders.dto.VendorResponse;
import com.vms.orders.entity.OrderItem;
import com.vms.orders.entity.OrderStatus;
import com.vms.orders.entity.PurchaseOrder;
import com.vms.orders.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final VendorClient vendorClient;
    public PurchaseOrder createOrder(OrderRequest request) {
        PurchaseOrder order = new PurchaseOrder();
        order.setVendorId(request.getVendorId());
        order.setStatus(OrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        List<OrderItem> items = request.getItems().stream().map(item -> {
            OrderItem newItem = new OrderItem();
            newItem.setOrder(order);
            newItem.setProductName(item.getProductName());
            newItem.setQuantity(item.getQuantity());
            newItem.setPrice(item.getPrice());
            return newItem;
        }).toList();
        double total = items.stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();
        order.setItems(items);
        order.setTotalAmount(total);
        return orderRepository.save(order);
    }
    public OrderResponse getOrderDetails(Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setVendorId(order.getVendorId());
        response.setStatus(order.getStatus().name());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        String vendorName = "—";
        try {
            VendorResponse vendor = vendorClient.getVendor(order.getVendorId());
            if (vendor != null && vendor.getCompanyName() != null) {
                vendorName = vendor.getCompanyName();
            }
        } catch (Exception e) {
        }
        response.setVendorName(vendorName);
        List<OrderItemResponse> itemResponses = order.getItems().stream().map(item -> {
            OrderItemResponse dto = new OrderItemResponse();
            dto.setProductName(item.getProductName());
            dto.setQuantity(item.getQuantity());
            dto.setPrice(item.getPrice());
            return dto;
        }).toList();
        response.setItems(itemResponses);
        response.setTotalAmount(order.getTotalAmount());
        return response;
    }
    public Page<PurchaseOrder> getVendorOrders(
            String vendorId,
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        if (status != null) {
            return orderRepository.findByVendorIdAndStatus(vendorId, status, pageable);
        }
        return orderRepository.findByVendorId(vendorId, pageable);
    }
    private boolean isValidTransition(OrderStatus current, OrderStatus next) {
        return switch (current) {
            case CREATED -> next == OrderStatus.APPROVED || next == OrderStatus.CANCELLED;
            case APPROVED -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.DELIVERED;
            case DELIVERED -> next == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
    public PurchaseOrder updateStatus(Long orderId, OrderStatus status) {
        PurchaseOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!isValidTransition(order.getStatus(), status)) {
            throw new RuntimeException("Invalid status transition");
        }
        order.setStatus(status);
        order.setUpdatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }
    public List<OrderResponse> getAllOrders() {
        List<PurchaseOrder> orders = orderRepository.findAll();
        return orders.stream().map(order -> {
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setVendorId(order.getVendorId());
            response.setStatus(order.getStatus().name());
            response.setCreatedAt(order.getCreatedAt());
            response.setUpdatedAt(order.getUpdatedAt());
            String vendorName = "—";
            try {
                VendorResponse vendor = vendorClient.getVendor(order.getVendorId());
                if (vendor != null && vendor.getCompanyName() != null) {
                    vendorName = vendor.getCompanyName();
                }
            } catch (Exception e) {
            }
            response.setVendorName(vendorName);
            List<OrderItemResponse> items = order.getItems().stream().map(item -> {
                OrderItemResponse dto = new OrderItemResponse();
                dto.setProductName(item.getProductName());
                dto.setQuantity(item.getQuantity());
                dto.setPrice(item.getPrice());
                return dto;
            }).toList();
            response.setItems(items);
            double total = items.stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();
            response.setTotalAmount(total);
            return response;
        }).toList();
    }
    public PageResponse<OrderResponse> getVendorOrdersResponse(
            String vendorId,
            OrderStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending()
        );
        Page<PurchaseOrder> orderPage;
        if (status != null) {
            orderPage = orderRepository.findByVendorIdAndStatus(vendorId, status, pageable);
        } else {
            orderPage = orderRepository.findByVendorId(vendorId, pageable);
        }
        List<OrderResponse> content = orderPage.getContent().stream().map(order -> {
            OrderResponse response = new OrderResponse();
            response.setId(order.getId());
            response.setVendorId(order.getVendorId());
            response.setStatus(order.getStatus().name());
            response.setCreatedAt(order.getCreatedAt());
            response.setTotalAmount(order.getTotalAmount());
            String vendorName = "—";
            try {
                VendorResponse vendor = vendorClient.getVendor(order.getVendorId());
                if (vendor != null && vendor.getCompanyName() != null) {
                    vendorName = vendor.getCompanyName();
                }
            } catch (Exception e) {
            }
            response.setVendorName(vendorName);
            return response;
        }).toList();
        PageResponse<OrderResponse> res = new PageResponse<>();
        res.setContent(content);
        res.setTotalPages(orderPage.getTotalPages());
        res.setTotalElements(orderPage.getTotalElements());
        return res;
    }
}