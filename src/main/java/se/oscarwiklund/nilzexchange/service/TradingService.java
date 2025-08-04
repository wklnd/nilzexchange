package se.oscarwiklund.nilzexchange.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.oscarwiklund.nilzexchange.controller.TradingController;
import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.model.Trade;
import se.oscarwiklund.nilzexchange.repository.OrderbookRepository;
import se.oscarwiklund.nilzexchange.repository.TradeRepository;
import se.oscarwiklund.nilzexchange.repository.TransactionRepository;
import se.oscarwiklund.nilzexchange.service.matching.MatchingEngine;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TradingService {

    private final OrderbookRepository orderbookRepository;
    private final TradeRepository tradeRepository;
    private final TransactionRepository transactionRepository;
    private final MatchingEngine matchingEngine;

    public TradingService(
            OrderbookRepository orderbookRepository,
            TradeRepository tradeRepository,
            TransactionRepository transactionRepository,
            MatchingEngine matchingEngine
    ) {
        this.orderbookRepository = orderbookRepository;
        this.tradeRepository = tradeRepository;
        this.transactionRepository = transactionRepository;
        this.matchingEngine = matchingEngine;
    }

    @Transactional
    public Orderbook placeOrder(TradingController.OrderRequest orderRequest) {
        Orderbook order = new Orderbook();
        order.setSymbol(orderRequest.getSymbol());
        order.setSide(orderRequest.getSide());
        order.setPrice(orderRequest.getPrice());
        order.setQuantity(orderRequest.getQuantity());
        order.setFilledQuantity(0L);
        order.setStatus(Orderbook.OrderStatus.OPEN);
        order.setOrderType(orderRequest.getOrderType());
        order.setUserId(orderRequest.getUserId());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        return orderbookRepository.save(order);
    }

    @Transactional
    public Map<String, Object> triggerMatching(String symbol) {
        Map<String, Object> result = new HashMap<>();

        // Get counts before matching
        List<Orderbook> buyOrdersBefore = orderbookRepository.findMatchingBuyOrders(symbol);
        List<Orderbook> sellOrdersBefore = orderbookRepository.findMatchingSellOrders(symbol);
        long tradesBefore = tradeRepository.count();

        // Trigger the matching engine
        matchingEngine.matchOrders(symbol);

        // Get counts after matching
        List<Orderbook> buyOrdersAfter = orderbookRepository.findMatchingBuyOrders(symbol);
        List<Orderbook> sellOrdersAfter = orderbookRepository.findMatchingSellOrders(symbol);
        long tradesAfter = tradeRepository.count();

        // Get recent trades
        List<Trade> recentTrades = tradeRepository.findTop10BySymbolOrderByExecutedAtDesc(symbol);

        result.put("symbol", symbol);
        result.put("buyOrdersBefore", buyOrdersBefore.size());
        result.put("sellOrdersBefore", sellOrdersBefore.size());
        result.put("buyOrdersAfter", buyOrdersAfter.size());
        result.put("sellOrdersAfter", sellOrdersAfter.size());
        result.put("tradesExecuted", tradesAfter - tradesBefore);
        result.put("recentTrades", recentTrades);
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    public Map<String, Object> getOrderBook(String symbol) {
        List<Orderbook> buyOrders = orderbookRepository.findMatchingBuyOrders(symbol);
        List<Orderbook> sellOrders = orderbookRepository.findMatchingSellOrders(symbol);
        List<Trade> recentTrades = tradeRepository.findTop10BySymbolOrderByExecutedAtDesc(symbol);

        Map<String, Object> orderBook = new HashMap<>();
        orderBook.put("symbol", symbol);
        orderBook.put("buyOrders", buyOrders);
        orderBook.put("sellOrders", sellOrders);
        orderBook.put("recentTrades", recentTrades);
        orderBook.put("timestamp", LocalDateTime.now());

        return orderBook;
    }

    public List<Orderbook> getUserOrders(Long userId) {
        return orderbookRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public Map<String, Object> createDemoData() {
        Map<String, Object> result = new HashMap<>();

        // Create demo buy orders
        createDemoOrder("AAPL", Orderbook.Side.BUY, new BigDecimal("150.00"), 100L, 1L);
        createDemoOrder("AAPL", Orderbook.Side.BUY, new BigDecimal("149.50"), 200L, 2L);
        createDemoOrder("AAPL", Orderbook.Side.BUY, new BigDecimal("149.00"), 150L, 3L);

        // Create demo sell orders
        createDemoOrder("AAPL", Orderbook.Side.SELL, new BigDecimal("150.50"), 80L, 4L);
        createDemoOrder("AAPL", Orderbook.Side.SELL, new BigDecimal("151.00"), 120L, 5L);
        createDemoOrder("AAPL", Orderbook.Side.SELL, new BigDecimal("151.50"), 200L, 6L);

        // Create orders for another symbol
        createDemoOrder("TSLA", Orderbook.Side.BUY, new BigDecimal("250.00"), 50L, 1L);
        createDemoOrder("TSLA", Orderbook.Side.SELL, new BigDecimal("255.00"), 75L, 2L);

        result.put("message", "Demo data created successfully");
        result.put("ordersCreated", 8);
        result.put("symbols", List.of("AAPL", "TSLA"));
        result.put("timestamp", LocalDateTime.now());

        return result;
    }

    private void createDemoOrder(String symbol, Orderbook.Side side, BigDecimal price, Long quantity, Long userId) {
        Orderbook order = new Orderbook();
        order.setSymbol(symbol);
        order.setSide(side);
        order.setPrice(price);
        order.setQuantity(quantity);
        order.setFilledQuantity(0L);
        order.setStatus(Orderbook.OrderStatus.OPEN);
        order.setOrderType(Orderbook.OrderType.LIMIT);
        order.setUserId(userId);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderbookRepository.save(order);
    }
}
