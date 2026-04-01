package com.vms.payments.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.vms.payments.dto.PaymentRequest;
import com.vms.payments.dto.PaymentResponse;
import com.vms.payments.entity.PaymentEntity;
import com.vms.payments.entity.PaymentStatus;
import com.vms.payments.service.PaymentService;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    @PostMapping
    public ResponseEntity<?> create(@RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.createPayment(request));
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam PaymentStatus status
    ) {
        return ResponseEntity.ok(paymentService.updateStatus(id, status));
    }
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<?> getVendorPayments(@PathVariable String vendorId) {
        return ResponseEntity.ok(paymentService.getVendorPayments(vendorId));
    }
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentService.getAllPayments());
    }
}
