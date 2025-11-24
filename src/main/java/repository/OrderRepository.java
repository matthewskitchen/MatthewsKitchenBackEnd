package repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import dbmodel.Order;
import dbmodel.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByUserEmailOrderByOrderedAtDesc(String userEmail);
    List<Order> findByStatusOrderByOrderedAtAsc(OrderStatus status);
    Optional<Order> findByOrderId(String orderId);
	List<Order> findByUserEmail(String email);
	
}
