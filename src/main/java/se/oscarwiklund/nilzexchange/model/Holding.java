package se.oscarwiklund.nilzexchange.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "holding")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "averagebuyprice", nullable = false)
    private BigDecimal averageBuyPrice;

    @ManyToOne
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    public Long getId() { return id; }
    public int getQuantity() { return quantity; }
    public BigDecimal getAverageBuyPrice() { return averageBuyPrice; }
    public Portfolio getPortfolio() { return portfolio; }
    public Asset getAsset() { return asset; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setAverageBuyPrice(BigDecimal averageBuyPrice) { this.averageBuyPrice = averageBuyPrice; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }
    public void setAsset(Asset asset) { this.asset = asset; }
}
