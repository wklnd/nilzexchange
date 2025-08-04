package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.oscarwiklund.nilzexchange.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    // Add custom query methods if needed
}

