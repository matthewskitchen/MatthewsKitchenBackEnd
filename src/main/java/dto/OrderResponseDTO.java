package dto;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import dbmodel.Order;

public class OrderResponseDTO {
    private String orderId;
    private String userEmail;
    private String address;
    private String name;
    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private String status;
    private String paymentMode;
    private Instant orderedAt;
    private double totalAmount;


	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
	}
	public void setDiscount(double discount) {
		this.discount = discount;
	}
	public void setGst(double gst) {
		this.gst = gst;
	}
	public void setDeliveryFee(double deliveryFee) {
		this.deliveryFee = deliveryFee;
	}
	private double discount;
    private double gst;
    private double deliveryFee;
    private double finalAmount;
    private List<OrderItemDTO> items;

    public static class OrderItemDTO {
        private String foodName;
        private int quantity;
        private double priceAtOrder;

        public OrderItemDTO(String foodName, int quantity, double priceAtOrder) {
            this.foodName = foodName;
            this.quantity = quantity;
            this.priceAtOrder = priceAtOrder;
        }

        // Getters
        public String getFoodName() { return foodName; }
        public int getQuantity() { return quantity; }
        public double getPriceAtOrder() { return priceAtOrder; }
    }

    public OrderResponseDTO(Order order) {
        this.orderId = order.getOrderId();
        this.userEmail = order.getUserEmail();
        this.address = order.getAddress();
        this.status = order.getStatus().name();
        this.paymentMode = order.getPaymentMode().name();
        this.orderedAt = order.getOrderedAt();
        this.finalAmount = order.getFinalAmount().doubleValue();
        
        this.totalAmount = order.getTotalAmount() != null ? order.getTotalAmount().doubleValue() : 0.0;
        this.discount = order.getDiscount() != null ? order.getDiscount().doubleValue() : 0.0;
        this.gst = order.getGst() != null ? order.getGst().doubleValue() : 0.0;
        this.deliveryFee = order.getDeliveryFee() != null ? order.getDeliveryFee().doubleValue() : 0.0;
        this.finalAmount = order.getFinalAmount() != null ? order.getFinalAmount().doubleValue() : 0.0;

        // ✅ Map order items
        this.items = order.getItems().stream()
            .map(item -> new OrderItemDTO(
                item.getFoodName(),
                item.getQuantity(),
                item.getPriceAtOrder().doubleValue()
            ))
            .collect(Collectors.toList());

        // Convert order items to DTOs
        this.items = order.getItems().stream()
            .map(item -> new OrderItemDTO(
                item.getFoodName(),
                item.getQuantity(),
                item.getPriceAtOrder().doubleValue()
            ))
            .collect(Collectors.toList());
    }

	// Getters & Setters
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public Instant getOrderedAt() { return orderedAt; }
    public void setOrderedAt(Instant orderedAt) { this.orderedAt = orderedAt; }

    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    

    public double getTotalAmount() { return totalAmount; }
    public double getDiscount() { return discount; }
    public double getGst() { return gst; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getFinalAmount() { return finalAmount; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
