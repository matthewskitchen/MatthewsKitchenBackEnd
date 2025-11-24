package controller;

import dbmodel.Coupon;
import services.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public List<Coupon> getAllCoupons() {
        return couponService.getAllCoupons();
    }

    @GetMapping("/active")
    public List<Coupon> getActiveCoupons() {
        return couponService.getActiveCoupons();
    }

    @PostMapping("/admin")
    public Coupon createCoupon(@RequestBody Coupon coupon) {
        return couponService.createCoupon(coupon);
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Integer id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        String userEmail = (String) payload.get("userEmail");
        double amount = Double.parseDouble(payload.get("amount").toString());

        String result = couponService.validateCoupon(code, userEmail, amount);
        
        if ("VALID".equals(result)) {
            double discount = couponService.calculateDiscount(code, amount);
            return ResponseEntity.ok(Map.of(
                "status", "VALID",
                "discount", discount
            ));
        } else {
            return ResponseEntity.badRequest().body(Map.of("status", "INVALID", "message", result));
        }
    }
}
