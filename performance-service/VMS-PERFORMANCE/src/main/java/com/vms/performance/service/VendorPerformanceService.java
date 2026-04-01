package com.vms.performance.service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.stereotype.Service;
import com.vms.performance.client.OrderClient;
import com.vms.performance.client.PaymentClient;
import com.vms.performance.client.VendorClient;
import com.vms.performance.dto.OrderResponse;
import com.vms.performance.dto.PageResponse;
import com.vms.performance.dto.PaymentResponse;
import com.vms.performance.dto.VendorResponse;
import com.vms.performance.entity.VendorPerformance;
import com.vms.performance.repository.VendorPerformanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class VendorPerformanceService {
    private final VendorPerformanceRepository repository;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final VendorClient vendorClient;
    private static final long ON_TIME_DAYS = 7;
    public VendorPerformance calculatePerformance(String vendorId) {
        PageResponse<OrderResponse> page = orderClient.getOrders(vendorId);
        List<OrderResponse> orders = page.getContent();
        List<PaymentResponse> payments = paymentClient.getPayments(vendorId);
        int totalOrders = orders.size();
        List<OrderResponse> activeOrders = orders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .toList();
        List<OrderResponse> deliveredOrders = activeOrders.stream()
                .filter(o ->
                    "DELIVERED".equals(o.getStatus()) ||
                    "COMPLETED".equals(o.getStatus()))
                .toList();
        List<OrderResponse> completedOrders = activeOrders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .toList();
        long cancelledCount = orders.stream()
                .filter(o -> "CANCELLED".equals(o.getStatus()))
                .count();
        long onTimeCount = deliveredOrders.stream()
                .filter(o -> {
                    if (o.getCreatedAt() == null) return false;
                    LocalDateTime effectiveEnd = o.getUpdatedAt() != null
                            ? o.getUpdatedAt()
                            : LocalDateTime.now();
                    long days = ChronoUnit.DAYS.between(o.getCreatedAt(), effectiveEnd);
                    return days <= ON_TIME_DAYS;
                })
                .count();
        double deliveryScore = deliveredOrders.isEmpty()
                ? 100.0  
                : (onTimeCount * 100.0 / deliveredOrders.size());
        double qualityScore = deliveredOrders.isEmpty()
                ? 100.0
                : (completedOrders.size() * 100.0 / deliveredOrders.size());
        double fulfillmentScore = totalOrders == 0
                ? 100.0
                : ((totalOrders - cancelledCount) * 100.0 / totalOrders);
        int totalPayments = payments.size();
        long completedPayments = payments.stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .count();
        long failedPayments = payments.stream()
                .filter(p -> "FAILED".equals(p.getStatus()))
                .count();
        double complianceScore = totalPayments == 0
                ? 100.0  
                : ((totalPayments - failedPayments) * 100.0 / totalPayments);
        double overallScore =
                (deliveryScore    * 0.40) +
                (qualityScore     * 0.25) +
                (fulfillmentScore * 0.20) +
                (complianceScore  * 0.15);
        double avgDeliveryTime = deliveredOrders.stream()
                .filter(o -> o.getCreatedAt() != null)
                .mapToLong(o -> {
                    LocalDateTime end = o.getUpdatedAt() != null ? o.getUpdatedAt() : LocalDateTime.now();
                    return ChronoUnit.HOURS.between(o.getCreatedAt(), end);
                })
                .average()
                .orElse(0);
        String vendorName = "Unknown Vendor";
        try {
            VendorResponse vendor = vendorClient.getVendor(vendorId);
            log.info("Vendor response for {}: {}", vendorId, vendor);
            if (vendor != null && vendor.getCompanyName() != null) {
                vendorName = vendor.getCompanyName();
            }
        } catch (Exception e) {
            log.error("Failed to fetch vendor name for vendorId={}: {}", vendorId, e.getMessage(), e);
        }
        VendorPerformance vp = repository.findById(vendorId)
                .orElse(new VendorPerformance());
        vp.setVendorId(vendorId);
        vp.setVendorName(vendorName);
        vp.setTotalOrders(totalOrders);
        vp.setCompletedOrders(completedOrders.size());
        vp.setOnTimeDeliveries((int) onTimeCount);
        vp.setAverageDeliveryTime(avgDeliveryTime);
        vp.setDeliveryScore(deliveryScore);
        vp.setQualityScore(qualityScore);
        vp.setComplianceScore(complianceScore);
        vp.setFulfillmentScore(fulfillmentScore);
        vp.setOverallScore(overallScore);
        vp.setCalculatedAt(LocalDateTime.now());
        log.info("Scores for vendor {}: delivery={}, quality={}, fulfillment={}, compliance={}, overall={}",
                vendorId,
                String.format("%.1f", deliveryScore),
                String.format("%.1f", qualityScore),
                String.format("%.1f", fulfillmentScore),
                String.format("%.1f", complianceScore),
                String.format("%.1f", overallScore));
        return repository.save(vp);
    }
    public List<VendorPerformance> calculateAllPerformance() {
        List<String> vendorIds = vendorClient.getAllVendors()
                .stream()
                .map(v -> v.getId().toString())
                .toList();
        return vendorIds.stream()
                .map(this::calculatePerformance)
                .toList();
    }
    public VendorPerformance getPerformance(String vendorId) {
        return repository.findById(vendorId)
                .orElseGet(() -> {
                    log.info("No performance record found for vendor {} — calculating now", vendorId);
                    return calculatePerformance(vendorId);
                });
    }
    public List<VendorPerformance> getAllPerformance() {
        return repository.findAll();
    }
}