package dbmodel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @Column(name = "order_ID", length = 20)
    private String orderId;

    @Column(name = "user_email", nullable = false, length = 50)
    private String userEmail;

    @Column(name = "address", nullable = false, length = 50)
    private String address;

    @Column(nullable = true)
    private String name;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_mode", nullable = false, length = 8)
    private PaymentMode paymentMode;

    // ✅ Now Hibernate will insert/update this field
    @Column(name = "ordered_at", nullable = false)
    private Instant orderedAt;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "gst", precision = 10, scale = 2)
    private BigDecimal gst;

    @Column(name = "delivery_fee", precision = 10, scale = 2)
    private BigDecimal deliveryFee;

    @Column(name = "final_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal finalAmount;

    @OneToMany(mappedBy = "order",
               cascade = CascadeType.ALL,
               orphanRemoval = true,
               fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(String orderId,
                 String userEmail,
                 String address,
                 BigDecimal totalAmount,
                 OrderStatus status,
                 PaymentMode paymentMode,
                 Instant orderedAt,
                 BigDecimal discount,
                 BigDecimal gst,
                 BigDecimal deliveryFee,
                 BigDecimal finalAmount,
                 List<OrderItem> items) {
        this.orderId = orderId;
        this.userEmail = userEmail;
        this.address = address;
        this.totalAmount = totalAmount;
        this.status = status;
        this.paymentMode = paymentMode;
        this.orderedAt = orderedAt;
        this.discount = discount;
        this.gst = gst;
        this.deliveryFee = deliveryFee;
        this.finalAmount = finalAmount;
        this.items = items;
    }

    public String getOrderId() {
        return orderId;
    }
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }
    public void setPaymentMode(PaymentMode paymentMode) {
        this.paymentMode = paymentMode;
    }

    public Instant getOrderedAt() {
        return orderedAt;
    }
    public void setOrderedAt(Instant orderedAt) {
        this.orderedAt = orderedAt;
    }

    public BigDecimal getDiscount() {
        return discount;
    }
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getGst() {
        return gst;
    }
    public void setGst(BigDecimal gst) {
        this.gst = gst;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }
    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }
    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public List<OrderItem> getItems() {
        return items;
    }
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}
