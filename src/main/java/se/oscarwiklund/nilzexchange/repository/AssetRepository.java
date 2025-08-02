package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.oscarwiklund.nilzexchange.model.Asset;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Asset findBySymbol(String symbol);
}
