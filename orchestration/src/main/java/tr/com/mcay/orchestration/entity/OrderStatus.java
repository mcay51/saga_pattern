package tr.com.mcay.orchestration.entity;

/**
 * Sipariş durumlarını tanımlayan enum sınıfı
 */
public enum OrderStatus {
    CREATED,
    PENDING,
    PAYMENT_COMPLETED,
    CONFIRMED,
    CANCELLED,
    FAILED
} 