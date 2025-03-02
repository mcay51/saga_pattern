package tr.com.mcay.orchestration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.mcay.orchestration.entity.SagaStatus;

import java.time.LocalDateTime;

/**
 * Saga işlem durumu yanıtı için DTO sınıfı
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaResponse {

    private String sagaId;
    private SagaStatus status;
    private String currentStep;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
} 