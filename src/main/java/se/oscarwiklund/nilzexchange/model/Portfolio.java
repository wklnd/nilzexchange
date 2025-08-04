package se.oscarwiklund.nilzexchange.model;


import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolio")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // ISK, KF, etc.

    @Column(nullable = false)
    private String currency;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Holding> holdings = new ArrayList<>();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getCurrency() { return currency; }
    public List<Holding> getHoldings() { return holdings; }

    public void setUser(User user) { this.user = user; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setCurrency(String currency) { this.currency = currency; }
    // For holdings, prefer add/remove methods for encapsulation, but a setter can be provided if needed:
    public void setHoldings(List<Holding> holdings) { this.holdings = holdings; }
}
