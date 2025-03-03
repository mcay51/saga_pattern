# Saga Pattern - Choreography Yaklaşımı

Bu belge, Saga Pattern'in Choreography yaklaşımını uygulayan e-ticaret mikroservis mimarisinin nasıl çalıştığını ve kullanıldığını adım adım açıklamaktadır.

## İçindekiler

1. [Giriş](#giriş)
2. [Saga Pattern Nedir?](#saga-pattern-nedir)
3. [Choreography vs Orchestration](#choreography-vs-orchestration)
4. [Proje Yapısı](#proje-yapısı)
5. [Kullanım Senaryoları](#kullanım-senaryoları)
6. [Adım Adım Sipariş Süreci](#adım-adım-sipariş-süreci)
7. [Hata Senaryoları ve Telafi İşlemleri](#hata-senaryoları-ve-telafi-işlemleri)
8. [API Kullanımı](#api-kullanımı)
9. [Projeyi Çalıştırma](#projeyi-çalıştırma)
10. [Teknik Detaylar](#teknik-detaylar)
11. [Sonuç](#sonuç)

## Giriş

Bu proje, dağıtık sistemlerde veri tutarlılığını sağlamak için Saga Pattern'in Choreography yaklaşımını kullanarak bir e-ticaret senaryosunu modellemektedir. Projede sipariş oluşturma, stok kontrolü, ödeme işlemi ve bildirim gönderme gibi adımlar birbirleriyle event-driven (olay tabanlı) bir şekilde iletişim kurmaktadır.

## Saga Pattern Nedir?

Saga Pattern, mikroservis mimarisinde dağıtık işlemleri yönetmek için kullanılan bir tasarım kalıbıdır. Geleneksel monolitik uygulamalarda ACID (Atomicity, Consistency, Isolation, Durability) özelliklerine sahip işlemler yerine, mikroservis mimarisinde her servisin kendi veritabanı olduğu için dağıtık işlemleri yönetmek zorlaşır.

Saga Pattern, bir işlemi birden fazla alt işleme böler ve her alt işlem kendi yerel işlemini tamamlar. Eğer bir alt işlem başarısız olursa, daha önce tamamlanan işlemleri geri almak için telafi edici işlemler (compensating transactions) çalıştırılır.

## Choreography vs Orchestration

Saga Pattern'in iki temel uygulama yaklaşımı vardır:

1. **Orchestration (Orkestrasyon)**: Merkezi bir koordinatör (orchestrator) tüm işlem adımlarını yönetir, hangi servisin ne zaman çalışacağına karar verir ve hata durumunda telafi işlemlerini başlatır.

2. **Choreography (Koreografi)**: Her servis kendi işini yapar ve işlem tamamlandığında bir event (olay) yayınlar. Diğer servisler bu olayları dinler ve kendi işlemlerini başlatır. Hata durumunda, hata oluşturan servis bir hata olayı yayınlar ve ilgili servisler bu olayı dinleyerek telafi işlemlerini gerçekleştirir.

Bu projede **Choreography** yaklaşımı kullanılmıştır. Bu yaklaşımın avantajları:

- Daha az merkezi bağımlılık
- Daha iyi ölçeklenebilirlik
- Servislerin daha bağımsız olması
- Daha az karmaşık kod yapısı

### Choreography Yaklaşımı Diyagramı

```
┌─────────────┐     (1) Sipariş Oluşturuldu     ┌─────────────┐
│             │ ────────────────────────────────▶             │
│   Sipariş   │                                  │    Ürün     │
│   Servisi   │                                  │   Servisi   │
│             │ ◀────────────────────────────────│             │
└─────────────┘     (2) Stok Rezerve Edildi      └─────────────┘
      ▲  │                                              │
      │  │                                              │
      │  │                                              │
      │  │ (3) Stok                                     │ (2) Stok
      │  │ Rezerve Edildi                               │ Rezerve Edildi
      │  ▼                                              ▼
┌─────────────┐                                  ┌─────────────┐
│             │                                  │             │
│   Ödeme     │                                  │  Bildirim   │
│   Servisi   │ ────────────────────────────────▶   Servisi   │
│             │     (4) Ödeme Tamamlandı         │             │
└─────────────┘                                  └─────────────┘
```

## Proje Yapısı

Projemiz aşağıdaki bileşenlerden oluşmaktadır:

1. **Müşteri Servisi**: Müşteri bilgilerini ve bakiyelerini yönetir.
2. **Ürün Servisi**: Ürün bilgilerini ve stok durumlarını yönetir.
3. **Sipariş Servisi**: Siparişleri oluşturur ve durumlarını takip eder.
4. **Ödeme Servisi**: Ödeme işlemlerini gerçekleştirir.
5. **Bildirim Servisi**: Müşterilere bildirim gönderir.

Her servis kendi veritabanına sahiptir ve servisler arasındaki iletişim event'ler aracılığıyla sağlanır.

### Proje Dizin Yapısı

```
src/
├── main/
│   ├── java/
│   │   └── tr/
│   │       └── com/
│   │           └── mcay/
│   │               └── choreography/
│   │                   ├── ChoreographyApplication.java
│   │                   ├── config/
│   │                   │   └── DatabaseConfig.java
│   │                   ├── controller/
│   │                   │   ├── CustomerController.java
│   │                   │   ├── NotificationController.java
│   │                   │   ├── OrderController.java
│   │                   │   ├── PaymentController.java
│   │                   │   └── ProductController.java
│   │                   ├── dto/
│   │                   │   ├── request/
│   │                   │   └── response/
│   │                   ├── entity/
│   │                   │   ├── Customer.java
│   │                   │   ├── Notification.java
│   │                   │   ├── NotificationType.java
│   │                   │   ├── Order.java
│   │                   │   ├── OrderStatus.java
│   │                   │   ├── Payment.java
│   │                   │   ├── PaymentStatus.java
│   │                   │   └── Product.java
│   │                   ├── event/
│   │                   │   ├── OrderCreatedEvent.java
│   │                   │   ├── OrderCompletedEvent.java
│   │                   │   ├── PaymentCompletedEvent.java
│   │                   │   ├── PaymentFailedEvent.java
│   │                   │   ├── StockReservedEvent.java
│   │                   │   └── StockFailedEvent.java
│   │                   ├── listener/
│   │                   │   ├── OrderEventListener.java
│   │                   │   ├── PaymentEventListener.java
│   │                   │   └── StockEventListener.java
│   │                   ├── publisher/
│   │                   │   └── EventPublisher.java
│   │                   ├── repository/
│   │                   │   ├── CustomerRepository.java
│   │                   │   ├── NotificationRepository.java
│   │                   │   ├── OrderRepository.java
│   │                   │   ├── PaymentRepository.java
│   │                   │   └── ProductRepository.java
│   │                   └── service/
│   │                       ├── impl/
│   │                       │   ├── CustomerServiceImpl.java
│   │                       │   ├── NotificationServiceImpl.java
│   │                       │   ├── OrderServiceImpl.java
│   │                       │   ├── PaymentServiceImpl.java
│   │                       │   └── ProductServiceImpl.java
│   │                       ├── CustomerService.java
│   │                       ├── NotificationService.java
│   │                       ├── OrderService.java
│   │                       ├── PaymentService.java
│   │                       └── ProductService.java
│   └── resources/
│       ├── application.yml
│       └── data.sql
└── test/
    └── java/
        └── tr/
            └── com/
                └── mcay/
                    └── choreography/
                        └── ChoreographyApplicationTests.java
```

## Kullanım Senaryoları

Bu projede aşağıdaki temel kullanım senaryoları modellenmiştir:

1. **Başarılı Sipariş Süreci**: Müşteri sipariş verir, stok kontrolü başarılı olur, ödeme başarılı olur ve sipariş tamamlanır.
2. **Stok Yetersizliği**: Müşteri sipariş verir, stok yetersiz olduğu için sipariş iptal edilir.
3. **Ödeme Başarısız**: Müşteri sipariş verir, stok kontrolü başarılı olur, ancak ödeme başarısız olur ve sipariş iptal edilir.

## Adım Adım Sipariş Süreci

Choreography yaklaşımında bir sipariş süreci şu şekilde ilerler:

1. **Sipariş Oluşturma**:
   - Müşteri bir sipariş oluşturur.
   - Sipariş servisi siparişi "CREATED" durumunda kaydeder.
   - Sipariş servisi "OrderCreatedEvent" olayını yayınlar.

2. **Stok Kontrolü**:
   - Ürün servisi "OrderCreatedEvent" olayını dinler.
   - Ürün servisi stok kontrolü yapar.
   - Eğer stok yeterliyse, stok rezerve edilir ve "StockReservedEvent" olayı yayınlanır.
   - Eğer stok yetersizse, "StockFailedEvent" olayı yayınlanır.

3. **Ödeme İşlemi**:
   - Ödeme servisi "StockReservedEvent" olayını dinler.
   - Ödeme servisi müşteri bakiyesini kontrol eder.
   - Eğer bakiye yeterliyse, ödeme işlemi gerçekleştirilir ve "PaymentCompletedEvent" olayı yayınlanır.
   - Eğer bakiye yetersizse, "PaymentFailedEvent" olayı yayınlanır.

4. **Sipariş Tamamlama**:
   - Sipariş servisi "PaymentCompletedEvent" olayını dinler.
   - Sipariş servisi siparişi "COMPLETED" durumuna günceller.
   - Sipariş servisi "OrderCompletedEvent" olayını yayınlar.

5. **Bildirim Gönderme**:
   - Bildirim servisi tüm olayları dinler.
   - İlgili olaylara göre müşteriye bildirim gönderir.

### Başarılı Sipariş Süreci Diyagramı

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Müşteri   │     │   Sipariş   │     │    Ürün     │     │    Ödeme    │     │  Bildirim   │
│   Servisi   │     │   Servisi   │     │   Servisi   │     │   Servisi   │     │   Servisi   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │                   │
       │    Sipariş Ver    │                   │                   │                   │
       │ ─────────────────▶│                   │                   │                   │
       │                   │                   │                   │                   │
       │                   │ OrderCreatedEvent │                   │                   │
       │                   │ ─────────────────▶│                   │                   │
       │                   │                   │                   │                   │
       │                   │                   │  Stok Kontrolü    │                   │
       │                   │ ─────────────────▶│                   │                   │
       │                   │                   │                   │                   │
       │                   │                   │ StockReservedEvent│                   │
       │                   │                   │ ─────────────────▶│                   │
       │                   │                   │                   │                   │
       │                   │                   │                   │  Ödeme İşlemi     │
       │                   │                   │                   │ ─────────────────▶│
       │                   │                   │                   │                   │
       │                   │                   │                   │PaymentCompletedEv.│
       │                   │ ◀─────────────────┼───────────────────┼───────────────────│
       │                   │                   │                   │                   │
       │                   │   Siparişi        │                   │                   │
       │                   │   Tamamla         │                   │                   │
       │                   │ ─────────────────▶│                   │                   │
       │                   │                   │                   │                   │
       │                   │OrderCompletedEvent│                   │                   │
       │                   │ ─────────────────────────────────────────────────────────▶│
       │                   │                   │                   │                   │
       │                   │                   │                   │                   │  Bildirim
       │ ◀─────────────────┼───────────────────┼───────────────────┼───────────────────│  Gönder
       │                   │                   │                   │                   │
```

## Hata Senaryoları ve Telafi İşlemleri

Choreography yaklaşımında hata durumlarında telafi işlemleri şu şekilde gerçekleşir:

1. **Stok Yetersizliği Durumu**:
   - Ürün servisi stok yetersizliği tespit eder ve "StockFailedEvent" olayını yayınlar.
   - Sipariş servisi bu olayı dinler ve siparişi "STOCK_FAILED" durumuna günceller.
   - Bildirim servisi bu olayı dinler ve müşteriye stok yetersizliği bildirimi gönderir.

2. **Ödeme Başarısız Durumu**:
   - Ödeme servisi bakiye yetersizliği tespit eder ve "PaymentFailedEvent" olayını yayınlar.
   - Sipariş servisi bu olayı dinler ve siparişi "PAYMENT_FAILED" durumuna günceller.
   - Ürün servisi bu olayı dinler ve daha önce rezerve edilen stoku serbest bırakır.
   - Bildirim servisi bu olayı dinler ve müşteriye ödeme başarısızlığı bildirimi gönderir.

### Stok Yetersizliği Durumu Diyagramı

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Müşteri   │     │   Sipariş   │     │    Ürün     │     │  Bildirim   │
│   Servisi   │     │   Servisi   │     │   Servisi   │     │   Servisi   │
└──────┬──────┘     └──────┬──────┘     └──────┬──────┘     └──────┬──────┘
       │                   │                   │                   │
       │    Sipariş Ver    │                   │                   │
       │ ─────────────────▶│                   │                   │
       │                   │                   │                   │
       │                   │ OrderCreatedEvent │                   │
       │                   │ ─────────────────▶│                   │
       │                   │                   │                   │
       │                   │                   │  Stok Kontrolü    │
       │                   │                   │ ─────────────────▶│
       │                   │                   │                   │
       │                   │                   │  StockFailedEvent │
       │                   │ ◀─────────────────┼───────────────────│
       │                   │                   │                   │
       │                   │   Siparişi        │                   │
       │                   │   İptal Et        │                   │
       │                   │ ─────────────────▶│                   │
       │                   │                   │                   │
       │                   │  StockFailedEvent │                   │
       │                   │ ─────────────────────────────────────▶│
       │                   │                   │                   │
       │                   │                   │                   │  Bildirim
       │ ◀─────────────────┼───────────────────┼───────────────────│  Gönder
       │                   │                   │                   │
```

## API Kullanımı

Projede aşağıdaki API'ler bulunmaktadır:

### Sipariş API'leri

```
POST /api/orders - Yeni sipariş oluşturur
GET /api/orders - Tüm siparişleri listeler
GET /api/orders/{id} - Belirli bir siparişi getirir
```

### Ürün API'leri

```
GET /api/products - Tüm ürünleri listeler
GET /api/products/{id} - Belirli bir ürünü getirir
```

### Müşteri API'leri

```
GET /api/customers - Tüm müşterileri listeler
GET /api/customers/{id} - Belirli bir müşteriyi getirir
```

### Ödeme API'leri

```
GET /api/payments - Tüm ödemeleri listeler
GET /api/payments/{id} - Belirli bir ödemeyi getirir
```

### Bildirim API'leri

```
GET /api/notifications - Tüm bildirimleri listeler
GET /api/notifications/customer/{customerId} - Belirli bir müşterinin bildirimlerini getirir
```

## Projeyi Çalıştırma

Projeyi çalıştırmak için aşağıdaki adımları izleyin:

1. Projeyi klonlayın:
   ```
   git clone https://github.com/yourusername/saga-pattern-choreography.git
   ```

2. Proje dizinine gidin:
   ```
   cd saga-pattern-choreography
   ```

3. Maven ile projeyi derleyin:
   ```
   mvn clean install
   ```

4. Uygulamayı çalıştırın:
   ```
   mvn spring-boot:run
   ```

5. Swagger UI'a erişin:
   ```
   http://localhost:8088/choreography/swagger-ui.html
   ```

## Teknik Detaylar

### Event Yapısı

Projede kullanılan event'ler aşağıdaki gibidir:

1. **OrderCreatedEvent**: Sipariş oluşturulduğunda yayınlanır.
   ```java
   public class OrderCreatedEvent {
       private Long orderId;
       private Long customerId;
       private Long productId;
       private Integer quantity;
       private BigDecimal amount;
       // Getter ve Setter metodları
   }
   ```

2. **StockReservedEvent**: Stok rezerve edildiğinde yayınlanır.
   ```java
   public class StockReservedEvent {
       private Long orderId;
       private Long productId;
       private Integer quantity;
       // Getter ve Setter metodları
   }
   ```

3. **StockFailedEvent**: Stok yetersiz olduğunda yayınlanır.
   ```java
   public class StockFailedEvent {
       private Long orderId;
       private Long productId;
       private String reason;
       // Getter ve Setter metodları
   }
   ```

4. **PaymentCompletedEvent**: Ödeme tamamlandığında yayınlanır.
   ```java
   public class PaymentCompletedEvent {
       private Long orderId;
       private Long paymentId;
       private BigDecimal amount;
       // Getter ve Setter metodları
   }
   ```

5. **PaymentFailedEvent**: Ödeme başarısız olduğunda yayınlanır.
   ```java
   public class PaymentFailedEvent {
       private Long orderId;
       private String reason;
       // Getter ve Setter metodları
   }
   ```

6. **OrderCompletedEvent**: Sipariş tamamlandığında yayınlanır.
   ```java
   public class OrderCompletedEvent {
       private Long orderId;
       private Long customerId;
       // Getter ve Setter metodları
   }
   ```

### Event Listener Örneği

Aşağıda, StockEventListener sınıfının bir örneği verilmiştir:

```java
@Component
public class StockEventListener {

    private final ProductService productService;
    private final EventPublisher eventPublisher;

    @Autowired
    public StockEventListener(ProductService productService, EventPublisher eventPublisher) {
        this.productService = productService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        try {
            // Stok kontrolü yap
            boolean isStockAvailable = productService.checkAndReserveStock(
                event.getProductId(), 
                event.getQuantity()
            );
            
            if (isStockAvailable) {
                // Stok rezerve edildi, StockReservedEvent yayınla
                StockReservedEvent stockReservedEvent = new StockReservedEvent();
                stockReservedEvent.setOrderId(event.getOrderId());
                stockReservedEvent.setProductId(event.getProductId());
                stockReservedEvent.setQuantity(event.getQuantity());
                
                eventPublisher.publishEvent(stockReservedEvent);
            } else {
                // Stok yetersiz, StockFailedEvent yayınla
                StockFailedEvent stockFailedEvent = new StockFailedEvent();
                stockFailedEvent.setOrderId(event.getOrderId());
                stockFailedEvent.setProductId(event.getProductId());
                stockFailedEvent.setReason("Yetersiz stok");
                
                eventPublisher.publishEvent(stockFailedEvent);
            }
        } catch (Exception e) {
            // Hata durumunda StockFailedEvent yayınla
            StockFailedEvent stockFailedEvent = new StockFailedEvent();
            stockFailedEvent.setOrderId(event.getOrderId());
            stockFailedEvent.setProductId(event.getProductId());
            stockFailedEvent.setReason("İşlem sırasında hata: " + e.getMessage());
            
            eventPublisher.publishEvent(stockFailedEvent);
        }
    }
    
    @EventListener
    public void handlePaymentFailedEvent(PaymentFailedEvent event) {
        // Ödeme başarısız olduğunda stoku serbest bırak
        productService.releaseStock(event.getOrderId());
    }
}
```

### Event Publisher Örneği

```java
@Component
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(Object event) {
        applicationEventPublisher.publishEvent(event);
    }
}
```

## Sonuç

Bu proje, Saga Pattern'in Choreography yaklaşımını kullanarak mikroservis mimarisinde dağıtık işlemlerin nasıl yönetilebileceğini göstermektedir. Event-driven mimari sayesinde servisler arasında gevşek bağlantı (loose coupling) sağlanmış ve her servis kendi sorumluluğunu yerine getirmektedir.

Choreography yaklaşımı, özellikle ölçeklenebilirlik ve bağımsızlık gerektiren büyük sistemlerde tercih edilebilir. Ancak, işlem akışının takip edilmesi ve hata ayıklaması Orchestration yaklaşımına göre daha zor olabilir.

Bu proje, Saga Pattern'in Choreography yaklaşımını anlamak ve uygulamak için iyi bir başlangıç noktasıdır. Kendi projelerinizde bu pattern'i kullanarak dağıtık işlemlerinizi daha güvenilir ve tutarlı bir şekilde yönetebilirsiniz. 