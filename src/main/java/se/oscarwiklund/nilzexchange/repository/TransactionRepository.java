package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.oscarwiklund.nilzexchange.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    // Add custom query methods if needed
}

