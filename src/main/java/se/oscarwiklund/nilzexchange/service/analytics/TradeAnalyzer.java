package se.oscarwiklund.nilzexchange.service.analytics;

import org.springframework.stereotype.Service;
import se.oscarwiklund.nilzexchange.model.Trade;
import se.oscarwiklund.nilzexchange.repository.TradeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TradeAnalyzer {

    private final TradeRepository tradeRepository;

    public TradeAnalyzer(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public void generateTradeReport(String symbol) {
        List<Trade> trades = tradeRepository.findAll(); // You can add filtering by symbol

        if (trades.isEmpty()) {
            System.out.println("No trades found for analysis.");
            return;
        }

        generateTextBasedChart(trades, symbol);
        generateTradeSummary(trades, symbol);
        generateVolumeAnalysis(trades, symbol);
    }

    private void generateTextBasedChart(List<Trade> trades, String symbol) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                        TRADE OVERVIEW - " + symbol + "                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        // Sort trades by price for visualization
        List<Trade> sortedTrades = trades.stream()
                .sorted((t1, t2) -> t1.getPrice().compareTo(t2.getPrice()))
                .collect(Collectors.toList());

        if (!sortedTrades.isEmpty()) {
            BigDecimal minPrice = sortedTrades.get(0).getPrice();
            BigDecimal maxPrice = sortedTrades.get(sortedTrades.size() - 1).getPrice();

            System.out.println("║ Price Range: $" + minPrice + " - $" + maxPrice + "                           ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");

            // Create a simple ASCII chart
            for (Trade trade : sortedTrades) {
                String bar = generatePriceBar(trade.getPrice(), minPrice, maxPrice, 40);
                System.out.printf("║ $%-8s │%-40s│ Vol: %-6d ║%n",
                    trade.getPrice(), bar, trade.getQuantity());
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    private String generatePriceBar(BigDecimal price, BigDecimal min, BigDecimal max, int maxLength) {
        if (max.equals(min)) return "█".repeat(maxLength);

        BigDecimal range = max.subtract(min);
        BigDecimal position = price.subtract(min);
        double ratio = position.divide(range, 4, BigDecimal.ROUND_HALF_UP).doubleValue();

        int barLength = (int) Math.round(ratio * maxLength);
        return "█".repeat(Math.max(1, barLength)) + " ".repeat(maxLength - barLength);
    }

    private void generateTradeSummary(List<Trade> trades, String symbol) {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                       TRADE SUMMARY                        │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        long totalVolume = trades.stream().mapToLong(Trade::getQuantity).sum();
        BigDecimal totalValue = trades.stream()
                .map(t -> t.getPrice().multiply(BigDecimal.valueOf(t.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avgPrice = trades.stream()
                .map(Trade::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(trades.size()), 2, BigDecimal.ROUND_HALF_UP);

        System.out.printf("│ Total Trades:     %-10d                          │%n", trades.size());
        System.out.printf("│ Total Volume:     %-10d shares                    │%n", totalVolume);
        System.out.printf("│ Total Value:      $%-10s                        │%n", totalValue);
        System.out.printf("│ Average Price:    $%-10s                        │%n", avgPrice);
        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private void generateVolumeAnalysis(List<Trade> trades, String symbol) {
        System.out.println("\n┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                      VOLUME ANALYSIS                       │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        // Group by price and sum volumes
        Map<BigDecimal, Long> volumeByPrice = trades.stream()
                .collect(Collectors.groupingBy(
                    Trade::getPrice,
                    Collectors.summingLong(Trade::getQuantity)
                ));

        long maxVolume = volumeByPrice.values().stream().mapToLong(Long::longValue).max().orElse(1);

        volumeByPrice.entrySet().stream()
                .sorted(Map.Entry.<BigDecimal, Long>comparingByKey().reversed())
                .forEach(entry -> {
                    BigDecimal price = entry.getKey();
                    Long volume = entry.getValue();
                    String volumeBar = generateVolumeBar(volume, maxVolume, 30);
                    System.out.printf("│ $%-8s │%-30s│ %6d    │%n", price, volumeBar, volume);
                });

        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private String generateVolumeBar(Long volume, Long maxVolume, int maxLength) {
        double ratio = (double) volume / maxVolume;
        int barLength = (int) Math.round(ratio * maxLength);
        return "▓".repeat(Math.max(1, barLength)) + "░".repeat(maxLength - barLength);
    }

    public void generateOrderbookVisualization(List<Trade> recentTrades) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RECENT TRADE ACTIVITY                    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Time     │ Side │ Price    │ Quantity │ Total Value         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        recentTrades.stream()
                .limit(10) // Show last 10 trades
                .forEach(trade -> {
                    String time = trade.getExecutedAt() != null ?
                        trade.getExecutedAt().format(timeFormatter) : "N/A";
                    BigDecimal totalValue = trade.getPrice().multiply(BigDecimal.valueOf(trade.getQuantity()));

                    System.out.printf("║ %-8s │ %-4s │ $%-7s │ %-8d │ $%-18s ║%n",
                        time, "EXEC", trade.getPrice(), trade.getQuantity(), totalValue);
                });

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }
}
