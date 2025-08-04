package se.oscarwiklund.nilzexchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.oscarwiklund.nilzexchange.model.Orderbook;

import java.util.List;

public interface OrderbookRepository extends JpaRepository<Orderbook, Long> {

    @Query("SELECT o FROM Orderbook o WHERE o.symbol = :symbol AND o.side = 'BUY' AND o.status = 'OPEN' ORDER BY o.price DESC, o.createdAt ASC")
    List<Orderbook> findMatchingBuyOrders(@Param("symbol") String symbol);

    @Query("SELECT o FROM Orderbook o WHERE o.symbol = :symbol AND o.side = 'SELL' AND o.status = 'OPEN' ORDER BY o.price ASC, o.createdAt ASC")
    List<Orderbook> findMatchingSellOrders(@Param("symbol") String symbol);

    @Query("SELECT o FROM Orderbook o WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Orderbook> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    // Add more custom queries as needed
}
