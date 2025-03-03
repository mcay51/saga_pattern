package tr.com.mcay.choreography.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.mcay.choreography.dto.StockCheckRequest;
import tr.com.mcay.choreography.dto.StockCheckResponse;
import tr.com.mcay.choreography.service.StockService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/check")
    public ResponseEntity<StockCheckResponse> checkAndReserveStock(@Valid @RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(stockService.checkAndReserveStock(request));
    }

    @PostMapping("/rollback/{orderId}")
    public ResponseEntity<Void> rollbackStockReservation(@PathVariable Long orderId) {
        stockService.rollbackStockReservation(orderId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/confirm/{orderId}")
    public ResponseEntity<Void> confirmStockReservation(@PathVariable Long orderId) {
        stockService.confirmStockReservation(orderId);
        return ResponseEntity.ok().build();
    }
} 