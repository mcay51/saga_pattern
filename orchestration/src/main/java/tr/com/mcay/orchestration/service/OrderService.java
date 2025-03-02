package tr.com.mcay.orchestration.service;

import tr.com.mcay.orchestration.dto.OrderRequest;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.entity.OrderStatus;

import java.util.List;

/**
 * Sipariş işlemleri için servis arayüzü
 */
public interface OrderService {

    /**
     * Yeni bir sipariş oluşturur
     * @param orderRequest Sipariş isteği
     * @return Oluşturulan sipariş
     */
    OrderResponse createOrder(OrderRequest orderRequest);

    /**
     * Sipariş numarasına göre siparişi getirir
     * @param orderNumber Sipariş numarası
     * @return Sipariş
     */
    OrderResponse getOrderByOrderNumber(String orderNumber);

    /**
     * Müşteri ID'sine göre siparişleri getirir
     * @param customerId Müşteri ID
     * @return Siparişler listesi
     */
    List<OrderResponse> getOrdersByCustomerId(Long customerId);

    /**
     * Sipariş durumunu günceller
     * @param orderNumber Sipariş numarası
     * @param status Yeni durum
     * @return Güncellenmiş sipariş
     */
    OrderResponse updateOrderStatus(String orderNumber, OrderStatus status);
} 