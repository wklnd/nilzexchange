package se.oscarwiklund.nilzexchange.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Long buyerId;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false)
    private LocalDateTime executedAt;

    @Column
    private Long buyOrderId;

    @Column
    private Long sellOrderId;

    // Getters/setters
    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public Long getBuyerId() { return buyerId; }
    public Long getSellerId() { return sellerId; }
    public BigDecimal getPrice() { return price; }
    public Long getQuantity() { return quantity; }
    public LocalDateTime getExecutedAt() { return executedAt; }
    public Long getBuyOrderId() { return buyOrderId; }
    public Long getSellOrderId() { return sellOrderId; }

    public void setSymbol(String symbol) { this.symbol = symbol; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
    public void setBuyOrderId(Long buyOrderId) { this.buyOrderId = buyOrderId; }
    public void setSellOrderId(Long sellOrderId) { this.sellOrderId = sellOrderId; }
}
