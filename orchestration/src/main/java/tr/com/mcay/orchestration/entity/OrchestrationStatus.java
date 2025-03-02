package tr.com.mcay.orchestration.entity;

/**
 * Saga işlem durumlarını tanımlayan enum sınıfı
 */
public enum OrchestrationStatus {
    STARTED,
    ORDER_CREATED,
    ORDER_CREATION_FAILED,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED,
    INVENTORY_UPDATED,
    INVENTORY_UPDATE_FAILED,
    COMPLETED,
    FAILED,
    COMPENSATING,
    COMPENSATED
} 