package com.vms.vendor.client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;
@FeignClient(name = "VMS-AUTH", url = "http:
public interface AuthFeignClient {
    @PostMapping("/api/auth/create-vendor-user")
    void createVendorUser(@RequestBody Map<String, String> request);
}
