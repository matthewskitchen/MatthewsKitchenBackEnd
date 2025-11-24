package services;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import dbmodel.FoodItem;
import dbmodel.Order;
import dbmodel.OrderItem;
import dbmodel.OrderStatus;
import dbmodel.PaymentMode;
import dto.OrderRequestDTO;
import dto.OrderResponseDTO;
import repository.FoodItemRepository;
import repository.OrderItemRepository;
import repository.OrderRepository;

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final EmailService emailService;
    private final FoodItemRepository foodItemRepo;

    public OrderService(OrderRepository orderRepo,
                        OrderItemRepository orderItemRepo,
                        EmailService emailService,
                        FoodItemRepository foodItemRepo) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.emailService = emailService;
        this.foodItemRepo = foodItemRepo;
    }

    private String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public String placeOrder(OrderRequestDTO request) {
        String orderId = generateOrderId();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserEmail(request.getUserEmail());
        order.setAddress(request.getAddress());
        order.setName(request.getName());
        order.setTotalAmount(BigDecimal.valueOf(request.getTotalAmount()));
        order.setDiscount(BigDecimal.valueOf(request.getDiscount()));
        order.setGst(BigDecimal.valueOf(request.getGst()));
        order.setDeliveryFee(BigDecimal.valueOf(request.getDeliveryFee()));
        order.setFinalAmount(BigDecimal.valueOf(request.getFinalAmount()));
        order.setPaymentMode(PaymentMode.valueOf(request.getPaymentMode().toUpperCase()));
        order.setStatus(OrderStatus.ORDERED);

        // ✅ Set orderedAt in code so it's never null
        order.setOrderedAt(Instant.now());

        orderRepo.save(order);

        for (OrderRequestDTO.OrderItemDTO itemDTO : request.getItems()) {
            // Decrease inventory logic
            FoodItem foodItem = foodItemRepo.findByName(itemDTO.getFoodName());
            if (foodItem != null) {
                int currentInventory = foodItem.getInventory();
                if (currentInventory >= itemDTO.getQuantity()) {
                    foodItem.setInventory(currentInventory - itemDTO.getQuantity());
                    foodItemRepo.save(foodItem);
                } else {
                    throw new RuntimeException("Insufficient inventory for item: " + itemDTO.getFoodName());
                }
            }

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setFoodName(itemDTO.getFoodName()); // save name permanently
            item.setQuantity(itemDTO.getQuantity());
            item.setPriceAtOrder(BigDecimal.valueOf(itemDTO.getPriceAtOrder()));
            orderItemRepo.save(item);
        }

        return orderId;
    }

    public List<OrderResponseDTO> getAllOrdersForAdmin() {
        List<Order> orders = orderRepo.findAll();

        // ✅ Safe sorting: handles any null orderedAt (old rows) by putting them last
        orders.sort(
            Comparator.comparing(
                Order::getOrderedAt,
                Comparator.nullsLast(Comparator.naturalOrder())
            ).reversed()
        );

        return orders.stream().map(order -> {
            OrderResponseDTO response = new OrderResponseDTO(order);
            response.setOrderId(order.getOrderId());
            response.setUserEmail(order.getUserEmail());
            response.setAddress(order.getAddress());
            response.setName(order.getName());
            response.setStatus(order.getStatus().name());
            response.setPaymentMode(order.getPaymentMode().name());
            response.setOrderedAt(order.getOrderedAt());
            response.setFinalAmount(order.getFinalAmount().doubleValue());

            List<OrderResponseDTO.OrderItemDTO> itemDTOs = order.getItems().stream()
                .map(item -> new OrderResponseDTO.OrderItemDTO(
                    item.getFoodName(),
                    item.getQuantity(),
                    item.getPriceAtOrder().doubleValue()
                ))
                .collect(Collectors.toList());

            response.setItems(itemDTOs);
            return response;
        }).collect(Collectors.toList());
    }

    public String updateOrderStatus(String orderId, String status) {
        Order order = orderRepo.findByOrderId(orderId).orElse(null);
        if (order == null)
            return "Order not found with ID: " + orderId;

        if (order.getStatus().equals(OrderStatus.DELIVERED)) {
            return "Order already delivered. Cannot update further.";
        }

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            orderRepo.save(order);

            // SEND INVOICE ONLY WHEN ORDER GETS DELIVERED
            if (newStatus == OrderStatus.DELIVERED) {
                // Convert to DTO
                OrderResponseDTO dto = new OrderResponseDTO(order);
                dto.setName(order.getName());

                // Send invoice mail
                emailService.sendOrderInvoice(dto);
            }

            return "Order Status Updated to " + newStatus;

        } catch (IllegalArgumentException e) {
            return "Invalid status. Allowed values: ORDERED, PREPARING, OUT_FOR_DELIVERY, DELIVERED";
        }
    }

    public List<OrderResponseDTO> getOrdersByUserEmail(String email) {
        List<Order> orders = orderRepo.findByUserEmailOrderByOrderedAtDesc(email);
        return orders.stream()
            .map(OrderResponseDTO::new)
            .collect(Collectors.toList());
    }
}
