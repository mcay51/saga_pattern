package tr.com.mcay.orchestration.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Sipariş oluşturma isteği için DTO sınıfı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {

    @NotNull(message = "Müşteri ID boş olamaz")
    @Positive(message = "Müşteri ID pozitif bir değer olmalıdır")
    private Long customerId;

    @NotEmpty(message = "Sipariş kalemleri boş olamaz")
    private List<@Valid OrderItemRequest> items;

    /**
     * Sipariş kalemi isteği için iç DTO sınıfı
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {

        @NotNull(message = "Ürün ID boş olamaz")
        @Positive(message = "Ürün ID pozitif bir değer olmalıdır")
        private Long productId;

        @NotNull(message = "Ürün adı boş olamaz")
        private String productName;

        @NotNull(message = "Miktar boş olamaz")
        @Positive(message = "Miktar pozitif bir değer olmalıdır")
        private Integer quantity;

        @NotNull(message = "Birim fiyat boş olamaz")
        @Positive(message = "Birim fiyat pozitif bir değer olmalıdır")
        private BigDecimal unitPrice;
    }
} 