package se.oscarwiklund.nilzexchange.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orderbook")
public class Orderbook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol; // AAPL, GOOGL, etc

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Side side; // enum BUY, SELL

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price; // price for each unit of the asset

    @Column(nullable = false)
    private Long quantity; // total quantity of the asset to be bought or sold

    @Column(nullable = false)
    private Long filledQuantity = 0L; // quantity that has been filled so far

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderStatus status; // PENDING, COMPLETED, CANCELED

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType orderType; // LIMIT, MARKET, STOP

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long userId;


    public enum Side {
        BUY, SELL
    }

    public enum OrderStatus {
        OPEN, PENDING, COMPLETED, CANCELED
    }

    public enum OrderType {
        LIMIT, MARKET, STOP
    }

    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public Side getSide() { return side; }
    public BigDecimal getPrice() { return price; }
    public Long getQuantity() { return quantity; }
    public Long getFilledQuantity() { return filledQuantity; }
    public OrderStatus getStatus() { return status; }
    public OrderType getOrderType() { return orderType; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getUserId() { return userId; }

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setSide(Side side) { this.side = side; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public void setFilledQuantity(Long filledQuantity) { this.filledQuantity = filledQuantity; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setOrderType(OrderType orderType) { this.orderType = orderType; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setUserId(Long userId) { this.userId = userId; }
}
