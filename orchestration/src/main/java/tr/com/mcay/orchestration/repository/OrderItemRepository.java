package tr.com.mcay.orchestration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.mcay.orchestration.entity.Order;
import tr.com.mcay.orchestration.entity.OrderItem;

import java.util.List;

/**
 * Sipariş kalemleri için repository sınıfı
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Siparişe ait tüm kalemleri bulur
     * @param order Sipariş
     * @return Sipariş kalemleri listesi
     */
    List<OrderItem> findByOrder(Order order);

    /**
     * Sipariş ID'sine göre tüm kalemleri bulur
     * @param orderId Sipariş ID
     * @return Sipariş kalemleri listesi
     */
    List<OrderItem> findByOrderId(Long orderId);
} 