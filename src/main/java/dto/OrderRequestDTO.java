package dto;

import java.util.List;
public class OrderRequestDTO {

    private String userEmail;
    private String address;
    private String paymentMode; 
    private double totalAmount;  
    private double discount;
    private double gst;
    private double deliveryFee;
    private double finalAmount; 
    private String name;

    private List<OrderItemDTO> items;

    public static class OrderItemDTO {
        private String foodName;   
        private int quantity;
        private double priceAtOrder;

        public String getFoodName() { return foodName; }
        public void setFoodName(String foodName) { this.foodName = foodName; }

        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }

        public double getPriceAtOrder() { return priceAtOrder; }
        public void setPriceAtOrder(double priceAtOrder) { this.priceAtOrder = priceAtOrder; }
    }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getGst() { return gst; }
    public void setGst(double gst) { this.gst = gst; }

    public double getDeliveryFee() { return deliveryFee; }
    public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }

    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }

    public List<OrderItemDTO> getItems() { return items; }
    public void setItems(List<OrderItemDTO> items) { this.items = items; }
}
