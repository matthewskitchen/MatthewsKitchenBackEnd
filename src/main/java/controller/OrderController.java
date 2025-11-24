package controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import dbmodel.Order;
import dto.OrderRequestDTO;
import dto.OrderResponseDTO;
import services.OrderService;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") 
public class OrderController {
    
	private final OrderService orderService;
    public OrderController(OrderService orderService) 
    { this.orderService = orderService;}

    @PostMapping("/place")
    public ResponseEntity<?> placeOrder(@RequestBody OrderRequestDTO request) {
            String orderId = orderService.placeOrder(request);
            return ResponseEntity.ok().body("Order placed successfully! Your Order ID is: " + orderId);
    }
    
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders() {
            return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
      }
    
    @PutMapping("/admin/status/{orderId}")
    public ResponseEntity<?> updateOrderStatus(@PathVariable String orderId,@RequestParam String status) {
            String msg = orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(msg);
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<?> getOrdersByUserEmail(@PathVariable String email) {
        List<OrderResponseDTO> response = orderService.getOrdersByUserEmail(email);
        return ResponseEntity.ok(response);
    }
    

}