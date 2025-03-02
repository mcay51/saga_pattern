package tr.com.mcay.orchestration.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Saga işlem durumunu tutan entity sınıfı
 */
@Entity
@Table(name = "saga_states")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sagaId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SagaStatus status;

    @Column(nullable = false)
    private String currentStep;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(length = 1000)
    private String payload;

    @Column(length = 1000)
    private String errorMessage;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 