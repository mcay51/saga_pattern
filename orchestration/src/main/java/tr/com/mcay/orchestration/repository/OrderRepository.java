package tr.com.mcay.orchestration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.mcay.orchestration.entity.Order;
import tr.com.mcay.orchestration.entity.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Sipariş işlemleri için repository sınıfı
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Sipariş numarasına göre siparişi bulur
     * @param orderNumber Sipariş numarası
     * @return Sipariş
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Müşteri ID'sine göre siparişleri bulur
     * @param customerId Müşteri ID
     * @return Siparişler listesi
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Belirli bir duruma sahip tüm siparişleri bulur
     * @param status Sipariş durumu
     * @return Siparişler listesi
     */
    List<Order> findByStatus(OrderStatus status);
} 