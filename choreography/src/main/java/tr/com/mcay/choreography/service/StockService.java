package tr.com.mcay.choreography.service;

import tr.com.mcay.choreography.dto.StockCheckRequest;
import tr.com.mcay.choreography.dto.StockCheckResponse;

public interface StockService {
    StockCheckResponse checkAndReserveStock(StockCheckRequest request);
    void rollbackStockReservation(Long orderId);
    void confirmStockReservation(Long orderId);
} 