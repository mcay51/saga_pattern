package tr.com.mcay.orchestration.service;

import tr.com.mcay.orchestration.dto.OrderRequest;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.dto.OrchestrationResponse;
import tr.com.mcay.orchestration.entity.OrchestrationStatus;

import java.util.List;

/**
 * Saga işlemleri için servis arayüzü
 */
public interface OrchestrationService {

    /**
     * Yeni bir sipariş oluşturma saga işlemini başlatır
     * @param orderRequest Sipariş isteği
     * @return Oluşturulan sipariş
     */
    OrderResponse createOrder(OrderRequest orderRequest);

    /**
     * Saga işlem durumunu ID'ye göre getirir
     * @param sagaId Saga ID
     * @return Saga işlem durumu
     */
    OrchestrationResponse getSagaStatus(String sagaId);

    /**
     * Belirli bir duruma sahip tüm saga işlemlerini getirir
     * @param status Saga durumu
     * @return Saga işlemleri listesi
     */
    List<OrchestrationResponse> getSagasByStatus(OrchestrationStatus status);
} 