package com.vms.orders.dto;
import java.util.List;
import com.vms.orders.entity.OrderItem;
import lombok.Data;
@Data
public class OrderRequest {
	private String vendorId;
	private List<OrderItem> items;
}
