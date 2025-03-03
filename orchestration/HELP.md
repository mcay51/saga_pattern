# Orchestration Servisinin Rolünü Anlamak

Bu dokümanda, Saga Pattern'in Orchestration yaklaşımını kullanarak dağıtık işlemleri nasıl yönettiğini adım adım bir senaryo üzerinden inceleyeceğiz.

## Orchestration Nedir?

Orchestration, mikroservis mimarisinde dağıtık işlemleri yönetmek için kullanılan bir yaklaşımdır. Bu yaklaşımda, merkezi bir servis (Orchestrator) tüm işlemleri koordine eder, her adımın durumunu takip eder ve hata durumunda telafi edici işlemleri başlatır.

## Proje Yapısı

Projemizde, sınıf isimlendirmeleri Orchestration yaklaşımını yansıtacak şekilde düzenlenmiştir:

- `OrchestrationService`: Dağıtık işlemleri koordine eden servis arayüzü
- `OrchestrationServiceImpl`: Servis implementasyonu
- `OrchestrationController`: API endpoint'lerini sunan controller
- `OrchestrationState`: Saga durumunu tutan entity
- `OrchestrationStatus`: Saga durumlarını tanımlayan enum

## E-Ticaret Sipariş Senaryosu

Aşağıda, bir e-ticaret uygulamasında sipariş oluşturma sürecini Orchestration yaklaşımıyla nasıl yönettiğimizi adım adım inceleyeceğiz.

### Senaryo: Başarılı Sipariş Oluşturma

#### 1. Kullanıcı Siparişi Başlatır

Kullanıcı, web arayüzünden bir ürün seçer ve sipariş verir. Frontend, Orchestration servisine bir POST isteği gönderir:

```http
POST /api/saga/orders
Content-Type: application/json

{
  "customerId": 12345,
  "items": [
    {
      "productId": 1001,
      "productName": "iPhone 15 Pro",
      "quantity": 1,
      "unitPrice": 1299.99
    },
    {
      "productId": 2002,
      "productName": "AirPods Pro",
      "quantity": 1,
      "unitPrice": 249.99
    }
  ],
  "totalAmount": 1549.98
}
```

#### 2. OrchestrationController İsteği Alır

`OrchestrationController` sınıfı, gelen isteği alır ve `OrchestrationService`'e yönlendirir:

```java
@PostMapping("/orders")
public ResponseEntity<CustomApiResponse<OrderResponse>> createOrder(
        @Valid @RequestBody OrderRequest orderRequest) {
    OrderResponse orderResponse = orchestrationService.createOrder(orderRequest);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomApiResponse.success("Sipariş başarıyla oluşturuldu", orderResponse));
}
```

#### 3. OrchestrationService Saga İşlemini Başlatır

`OrchestrationServiceImpl.createOrder()` metodu çağrılır ve saga işlemi başlatılır:

```java
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
        // Sipariş oluştur
        OrderResponse orderResponse = orderService.createOrder(orderRequest);
        
        // Saga durumunu güncelle
        orchestrationState.setStatus(OrchestrationStatus.ORDER_CREATED);
        orchestrationState.setCurrentStep("PROCESS_PAYMENT");
        orchestrationStateRepository.save(orchestrationState);
        
        // Ödeme işlemini gerçekleştir
        processPayment(orderResponse);
        
        // Saga durumunu güncelle
        orchestrationState.setStatus(OrchestrationStatus.PAYMENT_COMPLETED);
        orchestrationState.setCurrentStep("UPDATE_INVENTORY");
        orchestrationStateRepository.save(orchestrationState);
        
        // Stok güncellemesi yap
        updateInventory(orderResponse);
        
        // Saga durumunu güncelle
        orchestrationState.setStatus(OrchestrationStatus.COMPLETED);
        orchestrationState.setCurrentStep("ORDER_COMPLETED");
        orchestrationStateRepository.save(orchestrationState);
        
        // Siparişi tamamla
        orderService.updateOrderStatus(orderResponse.getOrderNumber(), OrderStatus.CONFIRMED);
        
        return orderResponse;
    } catch (Exception e) {
        // Hata durumunda telafi işlemlerini başlat
        handleCompensation(orchestrationState, e.getMessage());
        throw e;
    }
}
```

#### 4. Sipariş Servisi Çağrılır

`OrderService.createOrder()` metodu çağrılır ve sipariş oluşturulur:

```java
// OrderServiceImpl sınıfında
@Override
@Transactional
public OrderResponse createOrder(OrderRequest orderRequest) {
    // Sipariş numarası oluştur
    String orderNumber = "ORD-" + System.currentTimeMillis();
    
    // Sipariş entity'sini oluştur
    Order order = Order.builder()
            .orderNumber(orderNumber)
            .customerId(orderRequest.getCustomerId())
            .totalAmount(orderRequest.getTotalAmount())
            .status(OrderStatus.PENDING)
            .build();
    
    // Sipariş kalemlerini ekle
    List<OrderItem> orderItems = orderRequest.getItems().stream()
            .map(item -> OrderItem.builder()
                    .order(order)
                    .productId(item.getProductId())
                    .productName(item.getProductName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getUnitPrice())
                    .totalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .build())
            .collect(Collectors.toList());
    
    order.setItems(orderItems);
    
    // Siparişi kaydet
    Order savedOrder = orderRepository.save(order);
    
    // OrderResponse nesnesini oluştur ve dön
    return mapToOrderResponse(savedOrder);
}
```

#### 5. Ödeme İşlemi Gerçekleştirilir

`OrchestrationServiceImpl.processPayment()` metodu çağrılır ve ödeme işlemi gerçekleştirilir:

```java
private void processPayment(OrderResponse orderResponse) {
    log.info("Ödeme işlemi gerçekleştiriliyor: {}", orderResponse.getOrderNumber());
    
    // Payment Service'e HTTP isteği gönder
    Map<String, Object> paymentRequest = new HashMap<>();
    paymentRequest.put("orderNumber", orderResponse.getOrderNumber());
    paymentRequest.put("amount", orderResponse.getTotalAmount());
    paymentRequest.put("customerId", orderResponse.getCustomerId());
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(paymentRequest, headers);
    
    try {
        restTemplate.exchange(
                paymentServiceUrl + "/api/payments",
                HttpMethod.POST,
                entity,
                Object.class
        );
    } catch (Exception e) {
        log.error("Ödeme işlemi başarısız: {}", e.getMessage());
        throw new RuntimeException("Ödeme işlemi başarısız: " + e.getMessage());
    }
}
```

#### 6. Stok Güncellemesi Yapılır

`OrchestrationServiceImpl.updateInventory()` metodu çağrılır ve stok güncellemesi yapılır:

```java
private void updateInventory(OrderResponse orderResponse) {
    log.info("Stok güncellemesi yapılıyor: {}", orderResponse.getOrderNumber());
    
    // Inventory Service'e HTTP isteği gönder
    Map<String, Object> inventoryRequest = new HashMap<>();
    inventoryRequest.put("orderNumber", orderResponse.getOrderNumber());
    inventoryRequest.put("items", orderResponse.getItems());
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(inventoryRequest, headers);
    
    try {
        restTemplate.exchange(
                inventoryServiceUrl + "/api/inventory",
                HttpMethod.POST,
                entity,
                Object.class
        );
    } catch (Exception e) {
        log.error("Stok güncellemesi başarısız: {}", e.getMessage());
        throw new RuntimeException("Stok güncellemesi başarısız: " + e.getMessage());
    }
}
```

#### 7. Sipariş Tamamlanır

Sipariş durumu "CONFIRMED" olarak güncellenir:

```java
orderService.updateOrderStatus(orderResponse.getOrderNumber(), OrderStatus.CONFIRMED);
```

#### 8. Kullanıcıya Yanıt Döner

Orchestration servisi, başarılı sipariş bilgilerini kullanıcıya döner:

```json
{
  "success": true,
  "message": "Sipariş başarıyla oluşturuldu",
  "data": {
    "orderNumber": "ORD-1678123456789",
    "customerId": 12345,
    "totalAmount": 1549.98,
    "status": "CONFIRMED",
    "items": [
      {
        "productId": 1001,
        "productName": "iPhone 15 Pro",
        "quantity": 1,
        "unitPrice": 1299.99,
        "totalPrice": 1299.99
      },
      {
        "productId": 2002,
        "productName": "AirPods Pro",
        "quantity": 1,
        "unitPrice": 249.99,
        "totalPrice": 249.99
      }
    ],
    "createdAt": "2023-03-06T15:30:56.789Z"
  }
}
```

### Senaryo: Stok Yetersiz Olduğunda Hata Telafisi

Şimdi, stok güncellemesi sırasında bir hata oluştuğunda ne olacağını inceleyelim.

#### 1-5. Adımlar Aynı (Sipariş Oluşturma ve Ödeme İşlemi)

İlk 5 adım, başarılı senaryo ile aynıdır.

#### 6. Stok Güncellemesi Sırasında Hata Oluşur

Stok güncellemesi sırasında, yetersiz stok nedeniyle bir hata oluşur:

```java
private void updateInventory(OrderResponse orderResponse) {
    // ... (önceki kod)
    
    try {
        restTemplate.exchange(
                inventoryServiceUrl + "/api/inventory",
                HttpMethod.POST,
                entity,
                Object.class
        );
    } catch (Exception e) {
        log.error("Stok güncellemesi başarısız: {}", e.getMessage());
        throw new RuntimeException("Stok güncellemesi başarısız: " + e.getMessage());
    }
}
```

#### 7. Telafi İşlemleri Başlatılır

`OrchestrationServiceImpl.handleCompensation()` metodu çağrılır ve telafi işlemleri başlatılır:

```java
private void handleCompensation(OrchestrationState orchestrationState, String errorMessage) {
    log.info("Telafi işlemleri başlatılıyor: {}", orchestrationState.getSagaId());
    
    orchestrationState.setStatus(OrchestrationStatus.COMPENSATING);
    orchestrationState.setErrorMessage(errorMessage);
    orchestrationState.setCurrentStep("COMPENSATION_STARTED");
    orchestrationStateRepository.save(orchestrationState);
    
    try {
        // Hangi adımda hata oluştuğuna göre telafi işlemlerini gerçekleştir
        switch (orchestrationState.getStatus()) {
            case INVENTORY_UPDATE_FAILED:
                // Ödeme iadesini gerçekleştir
                refundPayment(orchestrationState);
                // Siparişi iptal et
                cancelOrder(orchestrationState);
                break;
            case PAYMENT_FAILED:
                // Siparişi iptal et
                cancelOrder(orchestrationState);
                break;
            case ORDER_CREATION_FAILED:
                // Herhangi bir telafi işlemi gerekmez
                break;
            default:
                log.warn("Bilinmeyen saga durumu için telafi işlemi: {}", orchestrationState.getStatus());
        }
        
        orchestrationState.setStatus(OrchestrationStatus.COMPENSATED);
        orchestrationState.setCurrentStep("COMPENSATION_COMPLETED");
        orchestrationStateRepository.save(orchestrationState);
    } catch (Exception e) {
        log.error("Telafi işlemleri başarısız: {}", e.getMessage());
        orchestrationState.setStatus(OrchestrationStatus.FAILED);
        orchestrationState.setErrorMessage("Telafi işlemleri başarısız: " + e.getMessage());
        orchestrationStateRepository.save(orchestrationState);
    }
}
```

#### 8. Ödeme İadesi Yapılır

`OrchestrationServiceImpl.refundPayment()` metodu çağrılır ve ödeme iadesi yapılır:

```java
private void refundPayment(OrchestrationState orchestrationState) {
    log.info("Ödeme iadesi yapılıyor: {}", orchestrationState.getSagaId());
    
    // Payment Service'e HTTP isteği gönder
    String orderNumber = extractOrderNumberFromPayload(orchestrationState);
    
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    
    try {
        restTemplate.exchange(
                paymentServiceUrl + "/api/payments/" + orderNumber + "/refund",
                HttpMethod.POST,
                entity,
                Object.class
        );
    } catch (Exception e) {
        log.error("Ödeme iadesi başarısız: {}", e.getMessage());
        throw new RuntimeException("Ödeme iadesi başarısız: " + e.getMessage());
    }
}
```

#### 9. Sipariş İptal Edilir

`OrchestrationServiceImpl.cancelOrder()` metodu çağrılır ve sipariş iptal edilir:

```java
private void cancelOrder(OrchestrationState orchestrationState) {
    log.info("Sipariş iptal ediliyor: {}", orchestrationState.getSagaId());
    
    // Order Service'e HTTP isteği gönder
    String orderNumber = extractOrderNumberFromPayload(orchestrationState);
    
    try {
        orderService.updateOrderStatus(orderNumber, OrderStatus.CANCELLED);
    } catch (Exception e) {
        log.error("Sipariş iptali başarısız: {}", e.getMessage());
        throw new RuntimeException("Sipariş iptali başarısız: " + e.getMessage());
    }
}
```

#### 10. Kullanıcıya Hata Yanıtı Döner

Orchestration servisi, hata bilgilerini kullanıcıya döner:

```json
{
  "success": false,
  "message": "Sipariş oluşturulamadı",
  "data": null,
  "error": {
    "code": "INVENTORY_ERROR",
    "message": "Stok güncellemesi başarısız: Yetersiz stok"
  }
}
```

## Orchestration Servisinin Avantajları

Bu senaryolarda gördüğümüz gibi, Orchestration servisi:

1. **Merkezi Kontrol**: Tüm işlem adımlarını merkezi olarak kontrol eder
2. **İzlenebilirlik**: Her adımın durumunu veritabanında saklar, böylece işlemin hangi aşamada olduğu her zaman bilinir
3. **Hata Yönetimi**: Herhangi bir adımda hata oluştuğunda, önceki adımları geri alarak tutarlılığı sağlar
4. **Esneklik**: Yeni adımlar eklemek veya mevcut adımları değiştirmek kolaydır

## Orchestration Servisini Anlamak İçin Kod İncelemesi

Orchestration servisinin rolünü daha iyi anlamak için aşağıdaki sınıfları inceleyebilirsiniz:

1. **OrchestrationController**: API endpoint'lerini sunar
   - `createOrder()`: Sipariş oluşturma saga işlemini başlatır
   - `getSagaStatus()`: Saga işlem durumunu getirir
   - `getSagasByStatus()`: Belirli durumdaki saga işlemlerini getirir

2. **OrchestrationService**: Servis arayüzü
   - `createOrder()`: Sipariş oluşturma saga işlemini başlatır
   - `getSagaStatus()`: Saga işlem durumunu getirir
   - `getSagasByStatus()`: Belirli durumdaki saga işlemlerini getirir

3. **OrchestrationServiceImpl**: Servis implementasyonu
   - `createOrder()`: Saga işlemini başlatır ve adımları koordine eder
   - `processPayment()`: Ödeme işlemini gerçekleştirir
   - `updateInventory()`: Stok güncellemesi yapar
   - `handleCompensation()`: Hata durumunda telafi işlemlerini başlatır
   - `refundPayment()`: Ödeme iadesini gerçekleştirir
   - `cancelOrder()`: Siparişi iptal eder

4. **OrchestrationState**: Saga durumunu tutan entity
   - `sagaId`: Saga işlem ID'si
   - `status`: Saga işlem durumu
   - `currentStep`: Mevcut adım
   - `errorMessage`: Hata mesajı

5. **OrchestrationStatus**: Saga durumlarını tanımlayan enum
   - `STARTED`: Saga işlemi başladı
   - `ORDER_CREATED`: Sipariş oluşturuldu
   - `PAYMENT_COMPLETED`: Ödeme tamamlandı
   - `INVENTORY_UPDATED`: Stok güncellendi
   - `COMPLETED`: Saga işlemi tamamlandı
   - `FAILED`: Saga işlemi başarısız oldu
   - `COMPENSATING`: Telafi işlemleri devam ediyor
   - `COMPENSATED`: Telafi işlemleri tamamlandı

## Sonuç

Bu dokümanda, Orchestration servisinin rolünü bir e-ticaret sipariş senaryosu üzerinden adım adım inceledik. Orchestration servisi, dağıtık işlemleri koordine ederek, her adımın durumunu takip eder ve hata durumunda telafi edici işlemleri başlatarak tutarlılığı sağlar.

Saga Pattern'in Orchestration yaklaşımı, mikroservis mimarisinde dağıtık işlemleri yönetmek için güçlü bir çözüm sunar. Bu yaklaşım, işlemlerin izlenebilirliğini artırır, hata yönetimini kolaylaştırır ve sistemin tutarlılığını sağlar. 