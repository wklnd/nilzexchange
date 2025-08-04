package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.oscarwiklund.nilzexchange.model.Trade;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    @Query("SELECT t FROM Trade t WHERE t.symbol = :symbol ORDER BY t.executedAt DESC")
    List<Trade> findTop10BySymbolOrderByExecutedAtDesc(@Param("symbol") String symbol);

    // Add more custom queries as needed
}
