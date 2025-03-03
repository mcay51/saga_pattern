package tr.com.mcay.orchestration.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import tr.com.mcay.orchestration.dto.OrderRequest;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.dto.OrchestrationResponse;
import tr.com.mcay.orchestration.entity.OrderStatus;
import tr.com.mcay.orchestration.entity.OrchestrationState;
import tr.com.mcay.orchestration.entity.OrchestrationStatus;
import tr.com.mcay.orchestration.exception.ResourceNotFoundException;
import tr.com.mcay.orchestration.repository.OrchestrationStateRepository;
import tr.com.mcay.orchestration.service.OrderService;
import tr.com.mcay.orchestration.service.OrchestrationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Saga işlemleri için servis implementasyonu
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrchestrationServiceImpl implements OrchestrationService {

    private final OrderService orderService;
    private final OrchestrationStateRepository orchestrationStateRepository;
    private final RestTemplate restTemplate;

    @Value("${service.order.url}")
    private String orderServiceUrl;

    @Value("${service.payment.url}")
    private String paymentServiceUrl;

    @Value("${service.inventory.url}")
    private String inventoryServiceUrl;

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Saga işlemini başlat
        String sagaId = UUID.randomUUID().toString();
        OrchestrationState orchestrationState = OrchestrationState.builder()
                .sagaId(sagaId)
                .status(OrchestrationStatus.STARTED)
                .currentStep("CREATE_ORDER")
                .build();
        orchestrationStateRepository.save(orchestrationState);

        try {
            // 1. Sipariş oluştur
            log.info("Sipariş oluşturuluyor: {}", sagaId);
            OrderResponse orderResponse = orderService.createOrder(orderRequest);
            
            // Saga durumunu güncelle
            orchestrationState.setStatus(OrchestrationStatus.ORDER_CREATED);
            orchestrationState.setCurrentStep("PROCESS_PAYMENT");
            orchestrationStateRepository.save(orchestrationState);

            // 2. Ödeme işlemini başlat
            log.info("Ödeme işlemi başlatılıyor: {}", sagaId);
            processPayment(orderResponse);
            
            // Saga durumunu güncelle
            orchestrationState.setStatus(OrchestrationStatus.PAYMENT_COMPLETED);
            orchestrationState.setCurrentStep("UPDATE_INVENTORY");
            orchestrationStateRepository.save(orchestrationState);

            // 3. Stok kontrolü yap
            log.info("Stok kontrolü yapılıyor: {}", sagaId);
            updateInventory(orderResponse);
            
            // Saga durumunu güncelle
            orchestrationState.setStatus(OrchestrationStatus.INVENTORY_UPDATED);
            orchestrationState.setCurrentStep("COMPLETE_ORDER");
            orchestrationStateRepository.save(orchestrationState);

            // 4. Siparişi onayla
            log.info("Sipariş onaylanıyor: {}", sagaId);
            orderService.updateOrderStatus(orderResponse.getOrderNumber(), OrderStatus.CONFIRMED);
            
            // Saga durumunu güncelle
            orchestrationState.setStatus(OrchestrationStatus.COMPLETED);
            orchestrationState.setCurrentStep("COMPLETED");
            orchestrationStateRepository.save(orchestrationState);

            // Güncellenmiş siparişi getir
            return orderService.getOrderByOrderNumber(orderResponse.getOrderNumber());
        } catch (Exception e) {
            // Hata durumunda telafi işlemlerini başlat
            log.error("Saga işlemi sırasında hata oluştu: {}", sagaId, e);
            orchestrationState.setStatus(OrchestrationStatus.FAILED);
            orchestrationState.setErrorMessage(e.getMessage());
            orchestrationStateRepository.save(orchestrationState);
            
            // Telafi işlemlerini başlat
            compensateTransaction(orchestrationState);
            
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public OrchestrationResponse getSagaStatus(String sagaId) {
        OrchestrationState orchestrationState = orchestrationStateRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new ResourceNotFoundException("Saga işlemi bulunamadı: " + sagaId));

        return mapToSagaResponse(orchestrationState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<OrchestrationResponse> getSagasByStatus(OrchestrationStatus status) {
        List<OrchestrationState> orchestrationStates = orchestrationStateRepository.findByStatus(status);

        return orchestrationStates.stream()
                .map(this::mapToSagaResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ödeme işlemini gerçekleştirir
     * @param orderResponse Sipariş yanıtı
     */
    private void processPayment(OrderResponse orderResponse) {
        // Gerçek uygulamada burada Payment Service'e HTTP isteği gönderilir
        log.info("Ödeme işlemi gerçekleştiriliyor: {}", orderResponse.getOrderNumber());
        
        // Simülasyon amaçlı kod
        Map<String, Object> paymentRequest = new HashMap<>();
        paymentRequest.put("orderNumber", orderResponse.getOrderNumber());
        paymentRequest.put("amount", orderResponse.getTotalAmount());
        paymentRequest.put("customerId", orderResponse.getCustomerId());
        
        // Gerçek uygulamada aşağıdaki kod kullanılır
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
        // restTemplate.exchange(paymentServiceUrl + "/api/payments", HttpMethod.POST, entity, Object.class);
    }

    /**
     * Stok güncellemesi yapar
     * @param orderResponse Sipariş yanıtı
     */
    private void updateInventory(OrderResponse orderResponse) {
        // Gerçek uygulamada burada Inventory Service'e HTTP isteği gönderilir
        log.info("Stok güncellemesi yapılıyor: {}", orderResponse.getOrderNumber());
        
        // Simülasyon amaçlı kod
        Map<String, Object> inventoryRequest = new HashMap<>();
        inventoryRequest.put("orderNumber", orderResponse.getOrderNumber());
        inventoryRequest.put("items", orderResponse.getItems());
        
        // Gerçek uygulamada aşağıdaki kod kullanılır
        // HttpHeaders headers = new HttpHeaders();
        // headers.setContentType(MediaType.APPLICATION_JSON);
        // HttpEntity<Map<String, Object>> entity = new HttpEntity<>(inventoryRequest, headers);
        // restTemplate.exchange(inventoryServiceUrl + "/api/inventory", HttpMethod.POST, entity, Object.class);
    }

    /**
     * Hata durumunda telafi işlemlerini gerçekleştirir
     * @param orchestrationState Saga durumu
     */
    private void compensateTransaction(OrchestrationState orchestrationState) {
        log.info("Telafi işlemleri başlatılıyor: {}", orchestrationState.getSagaId());
        
        orchestrationState.setStatus(OrchestrationStatus.COMPENSATING);
        orchestrationStateRepository.save(orchestrationState);
        
        try {
            // Saga durumuna göre telafi işlemlerini gerçekleştir
            switch (orchestrationState.getStatus()) {
                case INVENTORY_UPDATED:
                    // Stok güncellemesini geri al
                    log.info("Stok güncellemesi geri alınıyor: {}", orchestrationState.getSagaId());
                    // Gerçek uygulamada burada Inventory Service'e HTTP isteği gönderilir
                    
                case PAYMENT_COMPLETED:
                    // Ödemeyi iade et
                    log.info("Ödeme iade ediliyor: {}", orchestrationState.getSagaId());
                    // Gerçek uygulamada burada Payment Service'e HTTP isteği gönderilir
                    
                case ORDER_CREATED:
                    // Siparişi iptal et
                    log.info("Sipariş iptal ediliyor: {}", orchestrationState.getSagaId());
                    // Gerçek uygulamada burada Order Service'e HTTP isteği gönderilir
                    
                default:
                    break;
            }
            
            orchestrationState.setStatus(OrchestrationStatus.COMPENSATED);
            orchestrationStateRepository.save(orchestrationState);
            
        } catch (Exception e) {
            log.error("Telafi işlemleri sırasında hata oluştu: {}", orchestrationState.getSagaId(), e);
            orchestrationState.setErrorMessage("Telafi işlemleri sırasında hata: " + e.getMessage());
            orchestrationStateRepository.save(orchestrationState);
        }
    }

    /**
     * SagaState nesnesini SagaResponse nesnesine dönüştürür
     * @param orchestrationState Saga durumu
     * @return SagaResponse nesnesi
     */
    private OrchestrationResponse mapToSagaResponse(OrchestrationState orchestrationState) {
        return OrchestrationResponse.builder()
                .sagaId(orchestrationState.getSagaId())
                .status(orchestrationState.getStatus())
                .currentStep(orchestrationState.getCurrentStep())
                .createdAt(orchestrationState.getCreatedAt())
                .updatedAt(orchestrationState.getUpdatedAt())
                .errorMessage(orchestrationState.getErrorMessage())
                .build();
    }
} 