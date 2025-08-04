package se.oscarwiklund.nilzexchange.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction")
public class Transaction {

    public enum Type { // REDO
        DEBIT_CASH,      // Pengar dras från användaren
        CREDIT_CASH,     // Pengar tillförs
        DEBIT_ASSET,     // Tillgång dras (t.ex. sälj av aktier)
        CREDIT_ASSET     // Tillgång tillförs (t.ex. köp av aktier)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false)
    private String asset; // "SEK", "AAPL", "TSLA", etc.

    @Column(nullable = false, precision = 19, scale = 4) // scale 4?
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Länka till en Trade om det är en marknadstransaktion
    @Column
    private Long tradeId;

    // Getters/setters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Type getType() { return type; }
    public String getAsset() { return asset; }
    public BigDecimal getAmount() { return amount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getTradeId() { return tradeId; }

    public void setUserId(Long userId) { this.userId = userId; }
    public void setType(Type type) { this.type = type; }
    public void setAsset(String asset) { this.asset = asset; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setTradeId(Long tradeId) { this.tradeId = tradeId; }
}
