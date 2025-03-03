package tr.com.mcay.choreography.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckRequest {
    private Long orderId;
    private Long productId;
    private Integer quantity;
} 