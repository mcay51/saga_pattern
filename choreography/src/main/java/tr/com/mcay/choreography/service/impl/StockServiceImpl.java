package tr.com.mcay.choreography.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.com.mcay.choreography.dto.StockCheckRequest;
import tr.com.mcay.choreography.dto.StockCheckResponse;
import tr.com.mcay.choreography.entity.Product;
import tr.com.mcay.choreography.exception.InsufficientStockException;
import tr.com.mcay.choreography.exception.ResourceNotFoundException;
import tr.com.mcay.choreography.repository.ProductRepository;
import tr.com.mcay.choreography.service.StockService;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public StockCheckResponse checkAndReserveStock(StockCheckRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (product.getStockQuantity() < request.getQuantity()) {
            return StockCheckResponse.builder()
                    .orderId(request.getOrderId())
                    .available(false)
                    .message(String.format("Yetersiz stok. İstenen: %d, Mevcut: %d", 
                            request.getQuantity(), product.getStockQuantity()))
                    .build();
        }

        // Stok rezervasyonu yap
        product.setStockQuantity(product.getStockQuantity() - request.getQuantity());
        productRepository.save(product);

        return StockCheckResponse.builder()
                .orderId(request.getOrderId())
                .available(true)
                .message("Stok başarıyla rezerve edildi")
                .build();
    }

    @Override
    @Transactional
    public void rollbackStockReservation(Long orderId) {
        // Normalde burada sipariş detaylarını alıp, ürün ve miktar bilgisine göre stok iadesi yapılır
        // Basitlik için direkt olarak sipariş ID'si ile ilişkili bir stok rezervasyon tablosu kullanılabilir
        // Bu örnekte basit tutmak için varsayımsal bir işlem yapıyoruz
        
        // Örnek: Sipariş ID'sine göre stok rezervasyonunu bul ve iade et
        // StockReservation reservation = stockReservationRepository.findByOrderId(orderId);
        // Product product = productRepository.findById(reservation.getProductId()).get();
        // product.setStockQuantity(product.getStockQuantity() + reservation.getQuantity());
        // productRepository.save(product);
        // stockReservationRepository.delete(reservation);
        
        System.out.println("Stok rezervasyonu geri alındı: " + orderId);
    }

    @Override
    @Transactional
    public void confirmStockReservation(Long orderId) {
        // Normalde burada sipariş detaylarını alıp, stok rezervasyonunu onaylama işlemi yapılır
        // Basitlik için direkt olarak sipariş ID'si ile ilişkili bir stok rezervasyon tablosu kullanılabilir
        // Bu örnekte basit tutmak için varsayımsal bir işlem yapıyoruz
        
        // Örnek: Sipariş ID'sine göre stok rezervasyonunu bul ve onayla
        // StockReservation reservation = stockReservationRepository.findByOrderId(orderId);
        // reservation.setStatus(ReservationStatus.CONFIRMED);
        // stockReservationRepository.save(reservation);
        
        System.out.println("Stok rezervasyonu onaylandı: " + orderId);
    }
} 