package com.vms.payments.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.vms.payments.client.OrderClient;
import com.vms.payments.client.VendorClient;
import com.vms.payments.dto.OrderResponse;
import com.vms.payments.dto.PaymentRequest;
import com.vms.payments.dto.PaymentResponse;
import com.vms.payments.dto.VendorResponse;
import com.vms.payments.entity.OrderStatus;
import com.vms.payments.entity.PaymentEntity;
import com.vms.payments.entity.PaymentStatus;
import com.vms.payments.repository.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderClient orderClient;
    private final VendorClient vendorClient;

    public PaymentResponse createPayment(PaymentRequest request) {

        OrderResponse order = orderClient.getOrder(request.getOrderId());

        if (!order.getStatus().equals("DELIVERED")) {
            throw new RuntimeException("Item not yet delivered");
        }

        paymentRepository.findByOrderId(request.getOrderId())
                .ifPresent(p -> {
                    throw new RuntimeException("Payment already exists");
                });

        PaymentEntity payment = new PaymentEntity();

        payment.setOrderId(request.getOrderId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setVendorId(order.getVendorId());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setCreatedAt(LocalDateTime.now());

        PaymentEntity saved = paymentRepository.save(payment);

        String vendorName = "—";
        try {
            VendorResponse vendor = vendorClient.getVendor(order.getVendorId());
            if (vendor != null && vendor.getCompanyName() != null) {
                vendorName = vendor.getCompanyName();
            }
        } catch (Exception e) {
        }

        PaymentResponse response = new PaymentResponse();
        response.setId(saved.getId());
        response.setOrderId(saved.getOrderId());
        response.setVendorId(saved.getVendorId());
        response.setAmount(saved.getAmount());
        response.setMethod(saved.getMethod());
        response.setStatus(saved.getStatus().name());
        response.setCreatedAt(saved.getCreatedAt());
        response.setVendorName(vendorName);

        return response;
    }

    public PaymentEntity updateStatus(Long id, PaymentStatus status) {

        PaymentEntity payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setStatus(status);

        PaymentEntity updatedPayment = paymentRepository.save(payment);

        if (status == PaymentStatus.COMPLETED) {
            orderClient.updateOrderStatus(
                    payment.getOrderId(),
                    OrderStatus.COMPLETED
            );
        }

        return updatedPayment;
    }

    public List<PaymentResponse> getVendorPayments(String vendorId) {

        List<PaymentEntity> payments = paymentRepository.findByVendorId(vendorId);

        return payments.stream().map(payment -> {

            String vendorName = "—";
            try {
                VendorResponse vendor = vendorClient.getVendor(payment.getVendorId());
                if (vendor != null && vendor.getCompanyName() != null) {
                    vendorName = vendor.getCompanyName();
                }
            } catch (Exception e) {
            }

            PaymentResponse response = new PaymentResponse();
            response.setId(payment.getId());
            response.setOrderId(payment.getOrderId());
            response.setVendorId(payment.getVendorId());
            response.setAmount(payment.getAmount());
            response.setMethod(payment.getMethod());
            response.setStatus(payment.getStatus().name());
            response.setCreatedAt(payment.getCreatedAt());
            response.setVendorName(vendorName);

            return response;

        }).toList();
    }

    public List<PaymentResponse> getAllPayments() {

        List<PaymentEntity> payments = paymentRepository.findAll();

        return payments.stream().map(payment -> {

            String vendorName = "—";
            try {
                VendorResponse vendor = vendorClient.getVendor(payment.getVendorId());
                if (vendor != null && vendor.getCompanyName() != null) {
                    vendorName = vendor.getCompanyName();
                }
            } catch (Exception e) {
            }

            PaymentResponse response = new PaymentResponse();
            response.setId(payment.getId());
            response.setOrderId(payment.getOrderId());
            response.setVendorId(payment.getVendorId());
            response.setAmount(payment.getAmount());
            response.setMethod(payment.getMethod());
            response.setStatus(payment.getStatus().name());
            response.setCreatedAt(payment.getCreatedAt());
            response.setVendorName(vendorName);

            return response;

        }).toList();
    }
}