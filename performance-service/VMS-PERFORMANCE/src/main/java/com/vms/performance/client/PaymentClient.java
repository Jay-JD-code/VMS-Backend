package com.vms.performance.client;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.vms.performance.dto.PaymentResponse;
@FeignClient(name = "VMS-PAYMENTS", url = "${vms.payments.url:http:
public interface PaymentClient {
    @GetMapping("/api/payments/vendor/{vendorId}")
    List<PaymentResponse> getPayments(@PathVariable String vendorId);
}