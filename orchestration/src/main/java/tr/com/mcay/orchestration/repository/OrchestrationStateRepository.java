package tr.com.mcay.orchestration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.mcay.orchestration.entity.OrchestrationState;
import tr.com.mcay.orchestration.entity.OrchestrationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Saga durumları için repository sınıfı
 */
@Repository
public interface OrchestrationStateRepository extends JpaRepository<OrchestrationState, Long> {

    /**
     * Saga ID'ye göre saga durumunu bulur
     * @param sagaId Saga ID
     * @return Saga durumu
     */
    Optional<OrchestrationState> findBySagaId(String sagaId);

    /**
     * Belirli bir duruma sahip tüm saga işlemlerini bulur
     * @param status Saga durumu
     * @return Saga işlemleri listesi
     */
    List<OrchestrationState> findByStatus(OrchestrationStatus status);
} 