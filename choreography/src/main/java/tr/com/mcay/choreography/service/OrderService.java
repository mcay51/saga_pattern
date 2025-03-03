package tr.com.mcay.choreography.service;

import tr.com.mcay.choreography.dto.OrderRequest;
import tr.com.mcay.choreography.dto.OrderResponse;
import tr.com.mcay.choreography.dto.StockCheckResponse;
import tr.com.mcay.choreography.dto.PaymentResponse;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);
    OrderResponse getOrder(Long id);
    OrderResponse updateOrderStatus(Long id, String status);
    OrderResponse processStockResult(StockCheckResponse stockCheckResponse);
    OrderResponse processPaymentResult(PaymentResponse paymentResponse);
} 