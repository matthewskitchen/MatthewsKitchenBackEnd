package repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import dbmodel.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder_OrderId(String orderId);
}
