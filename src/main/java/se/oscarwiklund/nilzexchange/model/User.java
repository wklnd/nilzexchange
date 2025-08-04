package se.oscarwiklund.nilzexchange.model;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return created_at; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setEmail(String email) { this.email = email; }
    public void setCreatedAt(LocalDateTime created_at) { this.created_at = created_at; }

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Portfolio> portfolios = new ArrayList<>();

    public void setPortfolios(List<Portfolio> portfolios) { this.portfolios = portfolios; }

    public void addPortfolio(Portfolio portfolio) {
        portfolios.add(portfolio);
        portfolio.setUser(this);
    }

    public void removePortfolio(Portfolio portfolio) {
        portfolios.remove(portfolio);
        portfolio.setUser(null);
    }
}
