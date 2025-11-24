package services;

import dbmodel.Coupon;
import dbmodel.Order;
import repository.CouponRepository;
import repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    private CouponRepository couponRepository;
    
    @Autowired
    private OrderRepository orderRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }
    
    public List<Coupon> getActiveCoupons() {
        return couponRepository.findByActiveTrue();
    }

    public Coupon createCoupon(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    public void deleteCoupon(Integer id) {
        couponRepository.deleteById(id);
    }

    public Coupon getCouponByCode(String code) {
        return couponRepository.findByCode(code).orElse(null);
    }
    
    public String validateCoupon(String code, String userEmail, double orderAmount) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isEmpty()) {
            return "Invalid coupon code";
        }
        
        Coupon coupon = couponOpt.get();
        if (!coupon.isActive()) {
            return "Coupon is expired or inactive";
        }
        
        if (coupon.isFirstTimeUserOnly()) {
            List<Order> userOrders = orderRepository.findByUserEmailOrderByOrderedAtDesc(userEmail);
            if (!userOrders.isEmpty()) {
                return "This coupon is valid only for first-time users";
            }
        }
        
        return "VALID";
    }
    
    public double calculateDiscount(String code, double orderAmount) {
        Optional<Coupon> couponOpt = couponRepository.findByCode(code);
        if (couponOpt.isPresent()) {
            Coupon coupon = couponOpt.get();
            double discount = (orderAmount * coupon.getDiscountPercentage()) / 100.0;
            if (discount > coupon.getMaxDiscountAmount()) {
                discount = coupon.getMaxDiscountAmount();
            }
            return discount;
        }
        return 0.0;
    }
}
