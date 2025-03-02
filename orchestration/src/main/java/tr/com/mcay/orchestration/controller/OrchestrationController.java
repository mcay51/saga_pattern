package tr.com.mcay.orchestration.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.mcay.orchestration.dto.CustomApiResponse;
import tr.com.mcay.orchestration.dto.OrderRequest;
import tr.com.mcay.orchestration.dto.OrderResponse;
import tr.com.mcay.orchestration.dto.OrchestrationResponse;
import tr.com.mcay.orchestration.entity.OrchestrationStatus;
import tr.com.mcay.orchestration.service.OrchestrationService;

import java.util.List;

/**
 * Saga işlemleri için controller sınıfı
 */
@RestController
@RequestMapping("/api/saga")
@RequiredArgsConstructor
@Tag(name = "Saga Controller", description = "Saga Pattern Orchestration API")
public class OrchestrationController {

    private final OrchestrationService orchestrationService;

    /**
     * Yeni bir sipariş oluşturma saga işlemini başlatır
     * @param orderRequest Sipariş isteği
     * @return Oluşturulan sipariş
     */
    @PostMapping("/orders")
    @Operation(summary = "Yeni bir sipariş oluşturur", description = "Sipariş oluşturma saga işlemini başlatır")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Sipariş başarıyla oluşturuldu",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
    })
    public ResponseEntity<CustomApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody OrderRequest orderRequest) {
        OrderResponse orderResponse = orchestrationService.createOrder(orderRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CustomApiResponse.success("Sipariş başarıyla oluşturuldu", orderResponse));
    }

    /**
     * Saga işlem durumunu ID'ye göre getirir
     * @param sagaId Saga ID
     * @return Saga işlem durumu
     */
    @GetMapping("/{sagaId}")
    @Operation(summary = "Saga işlem durumunu getirir", description = "Saga ID'ye göre işlem durumunu getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İşlem başarılı",
                    content = @Content(schema = @Schema(implementation = OrchestrationResponse.class))),
            @ApiResponse(responseCode = "404", description = "Saga işlemi bulunamadı",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
    })
    public ResponseEntity<CustomApiResponse<OrchestrationResponse>> getSagaStatus(
            @Parameter(description = "Saga ID", required = true)
            @PathVariable String sagaId) {
        OrchestrationResponse orchestrationResponse = orchestrationService.getSagaStatus(sagaId);
        return ResponseEntity.ok(CustomApiResponse.success("Saga işlem durumu başarıyla getirildi", orchestrationResponse));
    }

    /**
     * Belirli bir duruma sahip tüm saga işlemlerini getirir
     * @param status Saga durumu
     * @return Saga işlemleri listesi
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Belirli durumdaki saga işlemlerini getirir", description = "Belirli bir duruma sahip tüm saga işlemlerini getirir")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "İşlem başarılı",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "Sunucu hatası",
                    content = @Content(schema = @Schema(implementation = CustomApiResponse.class)))
    })
    public ResponseEntity<CustomApiResponse<List<OrchestrationResponse>>> getSagasByStatus(
            @Parameter(description = "Saga durumu", required = true)
            @PathVariable OrchestrationStatus status) {
        List<OrchestrationResponse> orchestrationRespons = orchestrationService.getSagasByStatus(status);
        return ResponseEntity.ok(CustomApiResponse.success("Saga işlemleri başarıyla getirildi", orchestrationRespons));
    }
} 