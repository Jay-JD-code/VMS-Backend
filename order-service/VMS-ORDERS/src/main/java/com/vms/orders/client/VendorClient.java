package com.vms.orders.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.vms.orders.dto.VendorResponse;

@FeignClient(name = "VMS-VENDOR", url = "${vms.vendor.url:http://localhost:8082}")
public interface VendorClient {

    @GetMapping("/api/vendors/{id}")
    VendorResponse getVendor(@PathVariable("id") String id);
}