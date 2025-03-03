package tr.com.mcay.choreography.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tr.com.mcay.choreography.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
} 