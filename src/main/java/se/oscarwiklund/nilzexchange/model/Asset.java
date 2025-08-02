package se.oscarwiklund.nilzexchange.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Represents an asset in the NilzExchange system.
 *,
 */
@Entity
@Table(name = "assets") // change plz
public class Asset {

    // The Identifier for the asset, which is auto-generated.
    // It is unique and serves as the primary key for the asset.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The symbol for the asset, which is a unique identifier.
    // It is a short string (up to 10 characters) that represents the asset in
    // trading systems and is used for quick identification.
    // Example symbols could be "AAPL" for Apple Inc. or "GOOGL" for Alphabet Inc.
    @Column(nullable = false, unique = true, length = 10)
    private String symbol;

    // The name of the asset, which is a longer descriptive string.
    // It provides a human-readable name for the asset, such as "Apple Inc." or
    // "Alphabet Inc.".
    @Column(nullable = false)
    private String name;

    // The type of the asset, which indicates what kind of asset it is.
    // It can be one of several types, such as "STOCK", "FUND", or "ETF".
    // This field helps categorize the asset for trading and analysis purposes.
    @Column(nullable = false)
    private String type; // STOCK, FUND, ETF

    // The timestamp when the asset was created in the system.
    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    // The total number of shares outstanding for the asset.
    @Column(name = "totalsharesoutstanding", nullable = false) // Hibernate naming convention @FIX
    private Long totalSharesOutstanding;

    // The currency in which the asset is traded and the exchange where it is listed.
    @Column(nullable = false)
    private String currency; // The currency in which the asset is traded, e.g., "USD", "EUR".

    // The exchange where the asset is listed, such as "NASDAQ" or "NYSE".
    @Column(nullable = false)
    private String exchange; // The exchange where the asset is listed, e.g., "NASDAQ", "NYSE".



    public Long getId() { return id; }
    public String getSymbol() { return symbol; }
    public String getName() { return name; }
    public String getType() { return type; }
    public LocalDateTime getCreatedAt() { return created_at; }
    public Long getTotalSharesOutstanding() { return totalSharesOutstanding; }
    public String getCurrency() { return currency; }
    public String getExchange() { return exchange; }




}
