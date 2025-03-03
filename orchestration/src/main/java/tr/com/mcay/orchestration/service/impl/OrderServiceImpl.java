package tr.com.mcay.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tr.com.mcay.orchestration.dto.OrderRequest;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.entity.Order;
import tr.com.mcay.orchestration.entity.OrderItem;
import tr.com.mcay.orchestration.entity.OrderStatus;
import tr.com.mcay.orchestration.exception.ResourceNotFoundException;
import tr.com.mcay.orchestration.repository.OrderItemRepository;
import tr.com.mcay.orchestration.repository.OrderRepository;
import tr.com.mcay.orchestration.service.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Sipariş işlemleri için servis implementasyonu
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Toplam tutarı hesapla
        BigDecimal totalAmount = orderRequest.getItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sipariş oluştur
        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .customerId(orderRequest.getCustomerId())
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Sipariş kalemlerini oluştur
        List<OrderItem> orderItems = orderRequest.getItems().stream()
                .map(item -> {
                    BigDecimal itemTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return OrderItem.builder()
                            .order(savedOrder)
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .totalPrice(itemTotal)
                            .build();
                })
                .collect(Collectors.toList());

        List<OrderItem> savedItems = orderItemRepository.saveAll(orderItems);

        // Yanıt oluştur
        return mapToOrderResponse(savedOrder, savedItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı: " + orderNumber));

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        return mapToOrderResponse(order, orderItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);

        return orders.stream()
                .map(order -> {
                    List<OrderItem> orderItems = orderItemRepository.findByOrder(order);
                    return mapToOrderResponse(order, orderItems);
                })
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderNumber, OrderStatus status) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Sipariş bulunamadı: " + orderNumber));

        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrder(updatedOrder);

        return mapToOrderResponse(updatedOrder, orderItems);
    }

    /**
     * Sipariş numarası oluşturur
     * @return Sipariş numarası
     */
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Order ve OrderItem nesnelerini OrderResponse nesnesine dönüştürür
     * @param order Sipariş
     * @param orderItems Sipariş kalemleri
     * @return OrderResponse nesnesi
     */
    private OrderResponse mapToOrderResponse(Order order, List<OrderItem> orderItems) {
        List<OrderResponse.OrderItemResponse> itemResponses = orderItems.stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .totalPrice(item.getTotalPrice())
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .customerId(order.getCustomerId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
} 