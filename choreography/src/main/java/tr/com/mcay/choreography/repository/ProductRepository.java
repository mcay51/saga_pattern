package tr.com.mcay.choreography.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.mcay.choreography.entity.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
} 