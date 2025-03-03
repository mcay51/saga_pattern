package tr.com.mcay.orchestration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.mcay.orchestration.dto.CustomApiResponse;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.service.OrderService;

import java.util.List;

/**
 * Sipariş işlemleri için controller sınıfı
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order Controller", description = "Sipariş İşlemleri API")
public class OrderController {

    private final OrderService orderService;

    /**
     * Sipariş numarasına göre siparişi getirir
     * @param orderNumber Sipariş numarası
     * @return Sipariş
     */
    @GetMapping("/{orderNumber}")
    @Operation(summary = "Sipariş detaylarını getirir", description = "Sipariş numarasına göre sipariş detaylarını getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İşlem başarılı",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
    })
    public ResponseEntity<CustomApiResponse<OrderResponse>> getOrderByOrderNumber(
            @Parameter(description = "Sipariş numarası", required = true)
            @PathVariable String orderNumber) {
        OrderResponse orderResponse = orderService.getOrderByOrderNumber(orderNumber);
        return ResponseEntity.ok(CustomApiResponse.success("Sipariş başarıyla getirildi", orderResponse));
    }

    /**
     * Müşteri ID'sine göre siparişleri getirir
     * @param customerId Müşteri ID
     * @return Siparişler listesi
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Müşteri siparişlerini getirir", description = "Müşteri ID'sine göre tüm siparişleri getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İşlem başarılı",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
    })
    public ResponseEntity<CustomApiResponse<List<OrderResponse>>> getOrdersByCustomerId(
            @Parameter(description = "Müşteri ID", required = true)
            @PathVariable Long customerId) {
        List<OrderResponse> orderResponses = orderService.getOrdersByCustomerId(customerId);
        return ResponseEntity.ok(CustomApiResponse.success("Siparişler başarıyla getirildi", orderResponses));
    }
} 