package com.vms.dashboard.service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.vms.dashboard.client.OrderClient;
import com.vms.dashboard.client.PaymentClient;
import com.vms.dashboard.client.PerformanceClient;
import com.vms.dashboard.client.VendorClient;
import com.vms.dashboard.dto.DashboardResponse;
import com.vms.dashboard.dto.KPIStats;
import com.vms.dashboard.dto.OrdersTrendDTO;
import com.vms.dashboard.dto.VendorStatusDTO;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final VendorClient vendorClient;
    private final OrderClient orderClient;
    private final PaymentClient paymentClient;
    private final PerformanceClient performanceClient;
    public DashboardResponse getDashboard() {
        List<Map<String, Object>> vendors = vendorClient.getVendors();
        List<Map<String, Object>> orders = orderClient.getOrders();
        List<Map<String, Object>> payments = paymentClient.getPayments();
        List<Map<String, Object>> performance = performanceClient.getPerformance();
        DashboardResponse response = new DashboardResponse();
        KPIStats stats = new KPIStats();
        stats.setVendors(vendors.size());
        stats.setOrders(orders.size());
        stats.setPayments(payments.size());
        stats.setPerformance(calculatePerformance(performance));
        response.setStats(stats);
        Map<String, Long> statusMap = vendors.stream()
            .collect(Collectors.groupingBy(
                v -> (String) v.get("status"),
                Collectors.counting()
            ));
        response.setVendorStatus(
            statusMap.entrySet().stream()
                .map(e -> new VendorStatusDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList())
        );
        Map<String, Long> ordersByMonth = orders.stream()
            .collect(Collectors.groupingBy(
                o -> extractMonth((String) o.get("createdAt")),
                Collectors.counting()
            ));
        response.setOrdersTrend(
            ordersByMonth.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new OrdersTrendDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList())
        );
        Map<String, Long> paymentStatus = payments.stream()
            .collect(Collectors.groupingBy(
                p -> (String) p.get("status"),
                Collectors.counting()
            ));
        response.setPaymentStatus(
            paymentStatus.entrySet().stream()
                .map(e -> new VendorStatusDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList())
        );
        return response;
    }
    private double calculatePerformance(List<Map<String, Object>> performance) {
        return performance.stream()
            .mapToDouble(p -> {
                Object val = p.get("score");
                if (val == null) return 0.0;
                return Double.parseDouble(val.toString());
            })
            .average()
            .orElse(0.0);
    }
    private String extractMonth(String date) {
        return date.substring(0, 7);
    }
}