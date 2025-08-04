package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.oscarwiklund.nilzexchange.model.Holding;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    // Add custom query methods if needed
}

