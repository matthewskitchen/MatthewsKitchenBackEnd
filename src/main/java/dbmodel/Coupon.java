package dbmodel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(unique = true)
    private String code;
    
    @Column(name = "discount_percentage")
    private double discountPercentage;
    
    @Column(name = "max_discount_amount")
    private double maxDiscountAmount;
    
    @Column(name = "active")
    @JsonProperty("active")
    private boolean active;
    
    @Column(name = "first_time_user_only")
    @JsonProperty("firstTimeUserOnly")
    private boolean firstTimeUserOnly;

    public Coupon() {}

    public Coupon(String code, double discountPercentage, double maxDiscountAmount, boolean active, boolean firstTimeUserOnly) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.maxDiscountAmount = maxDiscountAmount;
        this.active = active;
        this.firstTimeUserOnly = firstTimeUserOnly;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    
    public double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public boolean isFirstTimeUserOnly() { return firstTimeUserOnly; }
    public void setFirstTimeUserOnly(boolean firstTimeUserOnly) { this.firstTimeUserOnly = firstTimeUserOnly; }
}
