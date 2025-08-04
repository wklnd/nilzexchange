package se.oscarwiklund.nilzexchange.service.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.model.Trade;
import se.oscarwiklund.nilzexchange.model.Transaction;
import se.oscarwiklund.nilzexchange.repository.OrderbookRepository;
import se.oscarwiklund.nilzexchange.repository.TradeRepository;
import se.oscarwiklund.nilzexchange.repository.TransactionRepository;
import se.oscarwiklund.nilzexchange.service.analytics.TradeAnalyzer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchingEngineTest {
    @Mock OrderbookRepository orderbookRepository;
    @Mock TradeRepository tradeRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock OrderMatcher orderMatcher;
    @InjectMocks MatchingEngine matchingEngine;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        matchingEngine = new MatchingEngine(orderbookRepository, tradeRepository, transactionRepository, orderMatcher);
    }

    @Test
    void testMatchOrders_MatchesBuyAndSell() {
        // Create mock Orderbook objects instead of real ones
        Orderbook buy = mock(Orderbook.class);
        Orderbook sell = mock(Orderbook.class);

        // Set up the mock behaviors for buy order
        when(buy.getSymbol()).thenReturn("AAPL");
        when(buy.getSide()).thenReturn(Orderbook.Side.BUY);
        when(buy.getPrice()).thenReturn(new BigDecimal("100.50"));
        when(buy.getQuantity()).thenReturn(10L);
        when(buy.getFilledQuantity()).thenReturn(0L);
        when(buy.getStatus()).thenReturn(Orderbook.OrderStatus.OPEN).thenReturn(Orderbook.OrderStatus.COMPLETED);
        when(buy.getUserId()).thenReturn(1L);
        when(buy.getId()).thenReturn(1L);

        // Set up the mock behaviors for sell order
        when(sell.getSymbol()).thenReturn("AAPL");
        when(sell.getSide()).thenReturn(Orderbook.Side.SELL);
        when(sell.getPrice()).thenReturn(new BigDecimal("100.00"));
        when(sell.getQuantity()).thenReturn(10L);
        when(sell.getFilledQuantity()).thenReturn(0L);
        when(sell.getStatus()).thenReturn(Orderbook.OrderStatus.OPEN).thenReturn(Orderbook.OrderStatus.COMPLETED);
        when(sell.getUserId()).thenReturn(2L);
        when(sell.getId()).thenReturn(2L);

        when(orderbookRepository.findMatchingBuyOrders("AAPL")).thenReturn(List.of(buy));
        when(orderbookRepository.findMatchingSellOrders("AAPL")).thenReturn(List.of(sell));

        MatchResult result = mock(MatchResult.class);
        Trade mockTrade = mock(Trade.class);
        when(mockTrade.getId()).thenReturn(100L);
        when(mockTrade.getSymbol()).thenReturn("AAPL");
        when(mockTrade.getQuantity()).thenReturn(10L);
        when(mockTrade.getPrice()).thenReturn(new BigDecimal("100.00"));
        when(mockTrade.getBuyerId()).thenReturn(1L);
        when(mockTrade.getSellerId()).thenReturn(2L);

        when(orderMatcher.executeMatch(buy, sell)).thenReturn(result);
        when(result.getTrade()).thenReturn(mockTrade);
        when(result.getTransactions()).thenReturn(List.of(mock(Transaction.class)));
        when(result.getBuyOrder()).thenReturn(buy);
        when(result.getSellOrder()).thenReturn(sell);

        // Capture the trade that gets saved
        ArgumentCaptor<Trade> tradeCaptor = ArgumentCaptor.forClass(Trade.class);

        matchingEngine.matchOrders("AAPL");

        verify(orderMatcher).executeMatch(buy, sell);
        verify(tradeRepository).save(tradeCaptor.capture());
        verify(transactionRepository).saveAll(any());
        verify(orderbookRepository).save(buy);
        verify(orderbookRepository).save(sell);

        // Print trade summary
        Trade capturedTrade = tradeCaptor.getValue();
        System.out.println("\n=== TRADE EXECUTED ===");
        System.out.println("Trade ID: " + capturedTrade.getId());
        System.out.println("Symbol: " + capturedTrade.getSymbol());
        System.out.println("Quantity: " + capturedTrade.getQuantity());
        System.out.println("Price: $" + capturedTrade.getPrice());
        System.out.println("Total Value: $" + capturedTrade.getPrice().multiply(BigDecimal.valueOf(capturedTrade.getQuantity())));
        System.out.println("Buyer ID: " + capturedTrade.getBuyerId());
        System.out.println("Seller ID: " + capturedTrade.getSellerId());
        System.out.println("=====================\n");
    }

    @Test
    void testMultipleTradesScenario() {
        System.out.println("\n=== MULTIPLE TRADES SCENARIO ===");

        // Multiple buy orders at different prices
        Orderbook buy1 = createMockOrder(1L, "AAPL", Orderbook.Side.BUY, "105.00", 5L, 1L);
        Orderbook buy2 = createMockOrder(2L, "AAPL", Orderbook.Side.BUY, "104.00", 8L, 2L);

        // Multiple sell orders at different prices
        Orderbook sell1 = createMockOrder(3L, "AAPL", Orderbook.Side.SELL, "103.00", 3L, 3L);
        Orderbook sell2 = createMockOrder(4L, "AAPL", Orderbook.Side.SELL, "104.50", 7L, 4L);

        when(orderbookRepository.findMatchingBuyOrders("AAPL")).thenReturn(List.of(buy1, buy2));
        when(orderbookRepository.findMatchingSellOrders("AAPL")).thenReturn(List.of(sell1, sell2));

        // Mock multiple trades
        MatchResult result1 = createMockMatchResult(buy1, sell1, 101L);
        MatchResult result2 = createMockMatchResult(buy1, sell2, 102L);

        when(orderMatcher.executeMatch(buy1, sell1)).thenReturn(result1);
        when(orderMatcher.executeMatch(buy1, sell2)).thenReturn(result2);

        matchingEngine.matchOrders("AAPL");

        System.out.println("Multiple trades executed successfully!");
        System.out.println("================================\n");
    }

    private Orderbook createMockOrder(Long id, String symbol, Orderbook.Side side, String price, Long quantity, Long userId) {
        Orderbook order = mock(Orderbook.class);
        when(order.getId()).thenReturn(id);
        when(order.getSymbol()).thenReturn(symbol);
        when(order.getSide()).thenReturn(side);
        when(order.getPrice()).thenReturn(new BigDecimal(price));
        when(order.getQuantity()).thenReturn(quantity);
        when(order.getFilledQuantity()).thenReturn(0L);
        when(order.getStatus()).thenReturn(Orderbook.OrderStatus.OPEN).thenReturn(Orderbook.OrderStatus.COMPLETED);
        when(order.getUserId()).thenReturn(userId);
        return order;
    }

    private MatchResult createMockMatchResult(Orderbook buy, Orderbook sell, Long tradeId) {
        MatchResult result = mock(MatchResult.class);
        Trade trade = mock(Trade.class);

        // Pre-calculate values to avoid calling mock methods during stubbing
        String symbol = "AAPL"; // Use fixed value since all our test orders are AAPL
        Long buyQuantity = 5L; // Default quantity for simplicity
        Long sellQuantity = 3L; // Default quantity for simplicity
        Long tradeQuantity = Math.min(buyQuantity, sellQuantity);
        BigDecimal tradePrice = new BigDecimal("103.00"); // Use sell price from test
        Long buyUserId = 1L;
        Long sellUserId = 3L;

        // Complete all trade stubbing first with pre-calculated values
        when(trade.getId()).thenReturn(tradeId);
        when(trade.getSymbol()).thenReturn(symbol);
        when(trade.getQuantity()).thenReturn(tradeQuantity);
        when(trade.getPrice()).thenReturn(tradePrice);
        when(trade.getBuyerId()).thenReturn(buyUserId);
        when(trade.getSellerId()).thenReturn(sellUserId);
        when(trade.getExecutedAt()).thenReturn(LocalDateTime.now());

        // Then complete all result stubbing
        when(result.getTrade()).thenReturn(trade);
        when(result.getTransactions()).thenReturn(List.of(mock(Transaction.class)));
        when(result.getBuyOrder()).thenReturn(buy);
        when(result.getSellOrder()).thenReturn(sell);

        return result;
    }

    @Test
    void testComprehensiveTradeAnalysis() {
        System.out.println("\n=== COMPREHENSIVE TRADE ANALYSIS TEST ===");

        // Create a realistic trading scenario with randomized trades
        List<Trade> mockTrades = createRandomizedTradeScenario();

        // Mock the trade repository to return our test trades
        TradeRepository mockTradeRepo = mock(TradeRepository.class);
        when(mockTradeRepo.findAll()).thenReturn(mockTrades);

        // Create analyzer and generate reports
        TradeAnalyzer analyzer = new TradeAnalyzer(mockTradeRepo);
        analyzer.generateTradeReport("AAPL");
        analyzer.generateOrderbookVisualization(mockTrades);

        System.out.println("\n=== MARKET DEPTH ANALYSIS ===");
        generateMarketDepthVisualization(mockTrades);

        System.out.println("\n=== PRICE MOVEMENT TIMELINE ===");
        generatePriceMovementChart(mockTrades);

        System.out.println("\n=== VOLATILITY ANALYSIS ===");
        generateVolatilityAnalysis(mockTrades);
    }

    @Test
    void testRandomizedMultipleTradesScenario() {
        System.out.println("\n=== RANDOMIZED MULTIPLE TRADES SCENARIO ===");

        // Generate random orders with varying prices and quantities
        List<Orderbook> buyOrders = generateRandomBuyOrders(5);
        List<Orderbook> sellOrders = generateRandomSellOrders(5);

        when(orderbookRepository.findMatchingBuyOrders("AAPL")).thenReturn(buyOrders);
        when(orderbookRepository.findMatchingSellOrders("AAPL")).thenReturn(sellOrders);

        // Create match results for potential matches
        for (int i = 0; i < Math.min(buyOrders.size(), sellOrders.size()); i++) {
            Orderbook buyOrder = buyOrders.get(i);
            Orderbook sellOrder = sellOrders.get(i);

            MatchResult result = createRandomizedMatchResult(buyOrder, sellOrder, 200L + i);
            when(orderMatcher.executeMatch(buyOrder, sellOrder)).thenReturn(result);
        }

        matchingEngine.matchOrders("AAPL");

        System.out.println("Randomized multiple trades executed successfully!");
        System.out.println("==========================================\n");

        // Print order summary
        printOrderSummary(buyOrders, sellOrders);
    }

    private List<Trade> createRandomizedTradeScenario() {
        java.util.Random random = new java.util.Random();
        List<Trade> trades = new java.util.ArrayList<>();

        // Base price around $150 with random fluctuations
        BigDecimal basePrice = new BigDecimal("150.00");
        int numberOfTrades = 15 + random.nextInt(10); // 15-24 trades

        System.out.println("Generating " + numberOfTrades + " randomized trades...");

        for (int i = 1; i <= numberOfTrades; i++) {
            // Random price fluctuation between -5% and +5%
            double priceVariation = (random.nextDouble() - 0.5) * 0.1; // -5% to +5%
            BigDecimal tradePrice = basePrice.multiply(BigDecimal.valueOf(1 + priceVariation))
                .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Random quantity between 50 and 500 shares
            Long quantity = 50L + random.nextLong(451); // 50-500 shares

            // Random buyer and seller IDs
            Long buyerId = 1L + random.nextLong(20);
            Long sellerId = 1L + random.nextLong(20);
            while (sellerId.equals(buyerId)) {
                sellerId = 1L + random.nextLong(20); // Ensure different users
            }

            trades.add(createMockTrade(
                (long) i,
                "AAPL",
                tradePrice,
                quantity,
                buyerId,
                sellerId
            ));

            // Update base price slightly for next trade (market drift)
            double drift = (random.nextDouble() - 0.5) * 0.02; // Small drift
            basePrice = basePrice.multiply(BigDecimal.valueOf(1 + drift))
                .setScale(2, BigDecimal.ROUND_HALF_UP);
        }

        return trades;
    }

    private List<Orderbook> generateRandomBuyOrders(int count) {
        java.util.Random random = new java.util.Random();
        List<Orderbook> orders = new java.util.ArrayList<>();

        for (int i = 1; i <= count; i++) {
            // Random buy prices between $145-155
            BigDecimal price = new BigDecimal(145 + random.nextDouble() * 10)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Random quantities between 100-1000
            Long quantity = 100L + random.nextLong(901);

            orders.add(createMockOrder(
                (long) i,
                "AAPL",
                Orderbook.Side.BUY,
                price.toString(),
                quantity,
                (long) i
            ));
        }

        // Sort buy orders by price (highest first)
        orders.sort((o1, o2) -> o2.getPrice().compareTo(o1.getPrice()));
        return orders;
    }

    private List<Orderbook> generateRandomSellOrders(int count) {
        java.util.Random random = new java.util.Random();
        List<Orderbook> orders = new java.util.ArrayList<>();

        for (int i = 1; i <= count; i++) {
            // Random sell prices between $148-158
            BigDecimal price = new BigDecimal(148 + random.nextDouble() * 10)
                .setScale(2, BigDecimal.ROUND_HALF_UP);

            // Random quantities between 100-1000
            Long quantity = 100L + random.nextLong(901);

            orders.add(createMockOrder(
                (long) (i + 100), // Different ID range for sell orders
                "AAPL",
                Orderbook.Side.SELL,
                price.toString(),
                quantity,
                (long) (i + 50) // Different user ID range
            ));
        }

        // Sort sell orders by price (lowest first)
        orders.sort((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()));
        return orders;
    }

    private MatchResult createRandomizedMatchResult(Orderbook buy, Orderbook sell, Long tradeId) {
        java.util.Random random = new java.util.Random();
        MatchResult result = mock(MatchResult.class);
        Trade trade = mock(Trade.class);

        // Calculate realistic trade quantity and price
        Long buyQuantity = 100L + random.nextLong(401); // Random between 100-500
        Long sellQuantity = 100L + random.nextLong(401);
        Long tradeQuantity = Math.min(buyQuantity, sellQuantity);

        // Use sell price (market maker gets the price)
        BigDecimal tradePrice = new BigDecimal(150 + random.nextDouble() * 5)
            .setScale(2, BigDecimal.ROUND_HALF_UP);

        // Complete all trade stubbing
        when(trade.getId()).thenReturn(tradeId);
        when(trade.getSymbol()).thenReturn("AAPL");
        when(trade.getQuantity()).thenReturn(tradeQuantity);
        when(trade.getPrice()).thenReturn(tradePrice);
        when(trade.getBuyerId()).thenReturn(1L + random.nextLong(20));
        when(trade.getSellerId()).thenReturn(21L + random.nextLong(20));
        when(trade.getExecutedAt()).thenReturn(LocalDateTime.now().minusMinutes(random.nextInt(60)));

        // Complete result stubbing
        when(result.getTrade()).thenReturn(trade);
        when(result.getTransactions()).thenReturn(List.of(mock(Transaction.class)));
        when(result.getBuyOrder()).thenReturn(buy);
        when(result.getSellOrder()).thenReturn(sell);

        return result;
    }

    private Trade createMockTrade(Long id, String symbol, BigDecimal price, Long quantity, Long buyerId, Long sellerId) {
        Trade trade = mock(Trade.class);
        when(trade.getId()).thenReturn(id);
        when(trade.getSymbol()).thenReturn(symbol);
        when(trade.getPrice()).thenReturn(price);
        when(trade.getQuantity()).thenReturn(quantity);
        when(trade.getBuyerId()).thenReturn(buyerId);
        when(trade.getSellerId()).thenReturn(sellerId);
        when(trade.getExecutedAt()).thenReturn(LocalDateTime.now().minusMinutes(id * 5));
        return trade;
    }

    private void generateMarketDepthVisualization(List<Trade> trades) {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                      MARKET DEPTH                          │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.println("│ Price Level │ Cumulative Volume │ Visual Depth            │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        // Group trades by price and calculate cumulative volumes
        var volumeByPrice = trades.stream()
            .collect(java.util.stream.Collectors.groupingBy(
                Trade::getPrice,
                java.util.stream.Collectors.summingLong(Trade::getQuantity)
            ));

        long maxVolume = volumeByPrice.values().stream().mapToLong(Long::longValue).max().orElse(1);

        volumeByPrice.entrySet().stream()
            .sorted(java.util.Map.Entry.<BigDecimal, Long>comparingByKey().reversed())
            .forEach(entry -> {
                BigDecimal price = entry.getKey();
                Long volume = entry.getValue();
                String depthBar = "█".repeat((int)((double)volume / maxVolume * 20));
                System.out.printf("│ $%-10s │ %-17d │ %-20s   │%n", price, volume, depthBar);
            });

        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private void generatePriceMovementChart(List<Trade> trades) {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                   PRICE MOVEMENT CHART                     │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        if (trades.isEmpty()) return;

        // Sort by execution time
        var sortedTrades = trades.stream()
            .sorted((t1, t2) -> t1.getExecutedAt().compareTo(t2.getExecutedAt()))
            .collect(java.util.stream.Collectors.toList());

        BigDecimal minPrice = trades.stream().map(Trade::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = trades.stream().map(Trade::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        BigDecimal priceRange = maxPrice.subtract(minPrice);

        System.out.println("│ Time   │ Price    │ Movement Chart (Low ← → High)        │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        for (int i = 0; i < sortedTrades.size(); i++) {
            Trade trade = sortedTrades.get(i);
            String timeStr = String.format("T+%02d", i);

            // Calculate position on chart (0-30 scale)
            int position = 15; // middle position as default
            if (priceRange.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal relativePosition = trade.getPrice().subtract(minPrice).divide(priceRange, 4, BigDecimal.ROUND_HALF_UP);
                position = (int)(relativePosition.doubleValue() * 30);
            }

            String chart = " ".repeat(Math.max(0, position)) + "●" + " ".repeat(Math.max(0, 30 - position));
            String trend = i > 0 ? getTrendIndicator(sortedTrades.get(i-1).getPrice(), trade.getPrice()) : "─";

            System.out.printf("│ %-6s │ $%-7s │ %s %s │%n", timeStr, trade.getPrice(), chart, trend);
        }

        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private String getTrendIndicator(BigDecimal previousPrice, BigDecimal currentPrice) {
        int comparison = currentPrice.compareTo(previousPrice);
        if (comparison > 0) return "↗";
        else if (comparison < 0) return "↘";
        else return "→";
    }

    private void printOrderSummary(List<Orderbook> buyOrders, List<Orderbook> sellOrders) {
        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                       ORDER SUMMARY                        │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.println("│ BUY ORDERS (Highest to Lowest Price)                       │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        for (Orderbook order : buyOrders) {
            BigDecimal totalValue = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            System.out.printf("│ User %-3d │ $%-8s │ %-6d shares │ $%-10s total │%n",
                order.getUserId(), order.getPrice(), order.getQuantity(), totalValue);
        }

        System.out.println("├─────────────────────────────────────────────────────────────┤");
        System.out.println("│ SELL ORDERS (Lowest to Highest Price)                      │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        for (Orderbook order : sellOrders) {
            BigDecimal totalValue = order.getPrice().multiply(BigDecimal.valueOf(order.getQuantity()));
            System.out.printf("│ User %-3d │ $%-8s │ %-6d shares │ $%-10s total │%n",
                order.getUserId(), order.getPrice(), order.getQuantity(), totalValue);
        }

        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }

    private void generateVolatilityAnalysis(List<Trade> trades) {
        if (trades.size() < 2) return;

        System.out.println("┌─────────────────────────────────────────────────────────────┐");
        System.out.println("│                    VOLATILITY ANALYSIS                     │");
        System.out.println("├─────────────────────────────────────────────────────────────┤");

        // Sort trades by execution time
        var sortedTrades = trades.stream()
            .sorted((t1, t2) -> t1.getExecutedAt().compareTo(t2.getExecutedAt()))
            .collect(java.util.stream.Collectors.toList());

        BigDecimal minPrice = trades.stream().map(Trade::getPrice).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = trades.stream().map(Trade::getPrice).max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
        BigDecimal priceRange = maxPrice.subtract(minPrice);

        BigDecimal firstPrice = sortedTrades.get(0).getPrice();
        BigDecimal lastPrice = sortedTrades.get(sortedTrades.size() - 1).getPrice();
        BigDecimal totalChange = lastPrice.subtract(firstPrice);

        double volatilityPercentage = priceRange.divide(firstPrice, 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();

        System.out.printf("│ Price Range:      $%-8s - $%-8s                    │%n", minPrice, maxPrice);
        System.out.printf("│ Total Movement:   $%-8s (%.2f%%)                      │%n",
            priceRange, volatilityPercentage);
        System.out.printf("│ Session Change:   $%-8s                            │%n", totalChange);
        System.out.printf("│ Number of Trades: %-6d                               │%n", trades.size());

        long totalVolume = trades.stream().mapToLong(Trade::getQuantity).sum();
        BigDecimal avgPrice = trades.stream()
            .map(Trade::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(trades.size()), 2, BigDecimal.ROUND_HALF_UP);

        System.out.printf("│ Total Volume:     %-6d shares                          │%n", totalVolume);
        System.out.printf("│ Average Price:    $%-8s                            │%n", avgPrice);

        System.out.println("└─────────────────────────────────────────────────────────────┘");
    }
}
