package tr.com.mcay.choreography.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.com.mcay.choreography.dto.*;
import tr.com.mcay.choreography.entity.Order;
import tr.com.mcay.choreography.entity.OrderStatus;
import tr.com.mcay.choreography.entity.Product;
import tr.com.mcay.choreography.exception.ResourceNotFoundException;
import tr.com.mcay.choreography.repository.OrderRepository;
import tr.com.mcay.choreography.repository.ProductRepository;
import tr.com.mcay.choreography.service.OrderService;
import tr.com.mcay.choreography.service.StockService;
import tr.com.mcay.choreography.service.PaymentService;
import tr.com.mcay.choreography.service.NotificationService;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    @Override
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Ürün bilgilerini al
        Product product = productRepository.findById(orderRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", orderRequest.getProductId()));

        // Sipariş tutarını hesapla
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(orderRequest.getQuantity()));

        // Sipariş oluştur
        Order order = Order.builder()
                .customerId(orderRequest.getCustomerId())
                .productId(orderRequest.getProductId())
                .quantity(orderRequest.getQuantity())
                .totalAmount(totalAmount)
                .status(OrderStatus.CREATED)
                .build();

        Order savedOrder = orderRepository.save(order);

        // Stok kontrolü için istek gönder
        StockCheckRequest stockCheckRequest = StockCheckRequest.builder()
                .orderId(savedOrder.getId())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .build();

        StockCheckResponse stockCheckResponse = stockService.checkAndReserveStock(stockCheckRequest);

        // Müşteriye bildirim gönder
        notificationService.sendNotification(NotificationRequest.builder()
                .customerId(savedOrder.getCustomerId())
                .message("Siparişiniz oluşturuldu ve işleme alındı.")
                .type(tr.com.mcay.choreography.entity.NotificationType.ORDER_CREATED)
                .build());

        return OrderResponse.builder()
                .id(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .productId(savedOrder.getProductId())
                .quantity(savedOrder.getQuantity())
                .totalAmount(savedOrder.getTotalAmount())
                .status(savedOrder.getStatus())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .message("Sipariş oluşturuldu ve stok kontrolü yapılıyor.")
                .build();
    }

    @Override
    public OrderResponse getOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Override
    public OrderResponse updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        order.setStatus(OrderStatus.valueOf(status));
        Order updatedOrder = orderRepository.save(order);

        return OrderResponse.builder()
                .id(updatedOrder.getId())
                .customerId(updatedOrder.getCustomerId())
                .productId(updatedOrder.getProductId())
                .quantity(updatedOrder.getQuantity())
                .totalAmount(updatedOrder.getTotalAmount())
                .status(updatedOrder.getStatus())
                .createdAt(updatedOrder.getCreatedAt())
                .updatedAt(updatedOrder.getUpdatedAt())
                .build();
    }

    @Override
    public OrderResponse processStockResult(StockCheckResponse stockCheckResponse) {
        Order order = orderRepository.findById(stockCheckResponse.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", stockCheckResponse.getOrderId()));

        if (stockCheckResponse.isAvailable()) {
            // Stok mevcut, ödeme işlemine geç
            order.setStatus(OrderStatus.STOCK_CHECKED);
            Order updatedOrder = orderRepository.save(order);

            // Ödeme işlemi için istek gönder
            PaymentRequest paymentRequest = PaymentRequest.builder()
                    .orderId(updatedOrder.getId())
                    .customerId(updatedOrder.getCustomerId())
                    .amount(updatedOrder.getTotalAmount())
                    .build();

            PaymentResponse paymentResponse = paymentService.processPayment(paymentRequest);

            // Müşteriye bildirim gönder
            notificationService.sendNotification(NotificationRequest.builder()
                    .customerId(updatedOrder.getCustomerId())
                    .message("Siparişiniz için stok kontrolü başarılı, ödeme işlemi başlatıldı.")
                    .type(tr.com.mcay.choreography.entity.NotificationType.STOCK_RESERVED)
                    .build());

            return OrderResponse.builder()
                    .id(updatedOrder.getId())
                    .customerId(updatedOrder.getCustomerId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .status(updatedOrder.getStatus())
                    .createdAt(updatedOrder.getCreatedAt())
                    .updatedAt(updatedOrder.getUpdatedAt())
                    .message("Stok kontrolü başarılı, ödeme işlemi başlatıldı.")
                    .build();
        } else {
            // Stok yetersiz, siparişi iptal et
            order.setStatus(OrderStatus.STOCK_FAILED);
            Order updatedOrder = orderRepository.save(order);

            // Müşteriye bildirim gönder
            notificationService.sendNotification(NotificationRequest.builder()
                    .customerId(updatedOrder.getCustomerId())
                    .message("Siparişiniz için yeterli stok bulunamadı, sipariş iptal edildi.")
                    .type(tr.com.mcay.choreography.entity.NotificationType.STOCK_FAILED)
                    .build());

            return OrderResponse.builder()
                    .id(updatedOrder.getId())
                    .customerId(updatedOrder.getCustomerId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .status(updatedOrder.getStatus())
                    .createdAt(updatedOrder.getCreatedAt())
                    .updatedAt(updatedOrder.getUpdatedAt())
                    .message("Stok kontrolü başarısız: " + stockCheckResponse.getMessage())
                    .build();
        }
    }

    @Override
    public OrderResponse processPaymentResult(PaymentResponse paymentResponse) {
        Order order = orderRepository.findById(paymentResponse.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", paymentResponse.getOrderId()));

        if (paymentResponse.getStatus() == tr.com.mcay.choreography.entity.PaymentStatus.COMPLETED) {
            // Ödeme başarılı, siparişi tamamla
            order.setStatus(OrderStatus.PAYMENT_COMPLETED);
            Order updatedOrder = orderRepository.save(order);

            // Stok rezervasyonunu onayla
            stockService.confirmStockReservation(updatedOrder.getId());

            // Siparişi tamamla
            order.setStatus(OrderStatus.COMPLETED);
            updatedOrder = orderRepository.save(order);

            // Müşteriye bildirim gönder
            notificationService.sendNotification(NotificationRequest.builder()
                    .customerId(updatedOrder.getCustomerId())
                    .message("Siparişiniz için ödeme alındı, siparişiniz tamamlandı.")
                    .type(tr.com.mcay.choreography.entity.NotificationType.ORDER_COMPLETED)
                    .build());

            return OrderResponse.builder()
                    .id(updatedOrder.getId())
                    .customerId(updatedOrder.getCustomerId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .status(updatedOrder.getStatus())
                    .createdAt(updatedOrder.getCreatedAt())
                    .updatedAt(updatedOrder.getUpdatedAt())
                    .message("Ödeme başarılı, sipariş tamamlandı.")
                    .build();
        } else {
            // Ödeme başarısız, siparişi iptal et ve stok rezervasyonunu geri al
            order.setStatus(OrderStatus.PAYMENT_FAILED);
            Order updatedOrder = orderRepository.save(order);

            // Stok rezervasyonunu geri al
            stockService.rollbackStockReservation(updatedOrder.getId());

            // Siparişi iptal et
            order.setStatus(OrderStatus.CANCELLED);
            updatedOrder = orderRepository.save(order);

            // Müşteriye bildirim gönder
            notificationService.sendNotification(NotificationRequest.builder()
                    .customerId(updatedOrder.getCustomerId())
                    .message("Siparişiniz için ödeme alınamadı, sipariş iptal edildi.")
                    .type(tr.com.mcay.choreography.entity.NotificationType.PAYMENT_FAILED)
                    .build());

            return OrderResponse.builder()
                    .id(updatedOrder.getId())
                    .customerId(updatedOrder.getCustomerId())
                    .productId(updatedOrder.getProductId())
                    .quantity(updatedOrder.getQuantity())
                    .totalAmount(updatedOrder.getTotalAmount())
                    .status(updatedOrder.getStatus())
                    .createdAt(updatedOrder.getCreatedAt())
                    .updatedAt(updatedOrder.getUpdatedAt())
                    .message("Ödeme başarısız: " + paymentResponse.getMessage())
                    .build();
        }
    }
} 