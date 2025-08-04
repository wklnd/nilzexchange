package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.oscarwiklund.nilzexchange.model.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    // Add custom query methods if needed
}

