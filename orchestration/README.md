# Saga Pattern Orchestration Service

Bu proje, mikroservis mimarisinde Saga Pattern Orchestration yaklaşımını kullanarak dağıtık işlemleri yönetmek için geliştirilmiştir.

## Proje Hakkında

Saga Pattern, mikroservis mimarisinde dağıtık işlemleri yönetmek için kullanılan bir tasarım desenidir. Bu desen, birden fazla mikroservis arasında tutarlılığı sağlamak için kullanılır. İki temel yaklaşımı vardır:

1. **Koreografi (Choreography)**: Her servis bir olaya tepki verir ve kendi işlemlerini gerçekleştirir.
2. **Orkestrasyon (Orchestration)**: Merkezi bir servis (Orchestrator), tüm işlemleri koordine eder.

Bu projede Orkestrasyon yaklaşımı kullanılmıştır.

## Mimari

Proje, aşağıdaki bileşenlerden oluşmaktadır:

- **Saga Orchestrator**: Tüm işlemleri koordine eden merkezi servis
- **Order Service**: Sipariş işlemlerini yöneten servis
- **Payment Service**: Ödeme işlemlerini yöneten servis
- **Inventory Service**: Stok işlemlerini yöneten servis

## Akış Diyagramı

```mermaid
sequenceDiagram
  participant User as Kullanıcı
  participant Orchestrator as Saga Orchestrator
  participant OrderService as Order Service
  participant PaymentService as Payment Service
  participant InventoryService as Inventory Service

  User ->> Orchestrator: Sipariş oluşturma talebi
  Orchestrator ->> OrderService: Sipariş oluştur
  OrderService -->> Orchestrator: Sipariş oluşturuldu (Başarılı)
  Orchestrator ->> PaymentService: Ödeme işlemini başlat
  PaymentService -->> Orchestrator: Ödeme başarılı
  Orchestrator ->> InventoryService: Stok kontrolü yap
  InventoryService -->> Orchestrator: Stok mevcut
  Orchestrator -->> User: Sipariş onaylandı
  Orchestrator ->> OrderService: Sipariş durumu: Onaylandı
```

## Teknolojiler

- Java 17
- Spring Boot 3.2.3
- Spring Data JPA
- H2 Database
- Swagger/OpenAPI
- Lombok

## API Endpoints

### Saga Controller

- `POST /api/saga/orders`: Yeni bir sipariş oluşturur
- `GET /api/saga/{sagaId}`: Saga işlem durumunu getirir
- `GET /api/saga/status/{status}`: Belirli durumdaki saga işlemlerini getirir

### Order Controller

- `GET /api/orders/{orderNumber}`: Sipariş detaylarını getirir
- `GET /api/orders/customer/{customerId}`: Müşteri siparişlerini getirir

## Kurulum ve Çalıştırma

1. Projeyi klonlayın:
   ```bash
   git clone https://github.com/mcay/saga-pattern.git
   ```

2. Projeyi derleyin:
   ```bash
   mvn clean install
   ```

3. Uygulamayı çalıştırın:
   ```bash
   java -jar orchestration/target/orchestration-1.0.0.jar
   ```

4. Swagger UI'a erişin:
   ```
   http://localhost:8080/swagger-ui.html
   ```

## Hata Telafisi (Compensation)

Saga Pattern'in en önemli özelliklerinden biri, bir adımda hata oluştuğunda önceki adımları geri alabilmesidir. Bu projede, hata durumunda aşağıdaki telafi işlemleri gerçekleştirilir:

1. **Stok Güncellemesi Başarısız**: Stok güncellemesi geri alınır, ödeme iade edilir, sipariş iptal edilir.
2. **Ödeme Başarısız**: Ödeme iade edilir, sipariş iptal edilir.
3. **Sipariş Oluşturma Başarısız**: Sipariş iptal edilir.

## Postman Collection

Projeyi test etmek için kullanabileceğiniz Postman Collection'ı [buradan](./postman_collection.json) indirebilirsiniz. 