package se.oscarwiklund.nilzexchange.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.service.TradingService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingService tradingService;

    public TradingController(TradingService tradingService) {
        this.tradingService = tradingService;
    }

    @PostMapping("/orders")
    public ResponseEntity<Orderbook> placeOrder(@RequestBody OrderRequest orderRequest) {
        Orderbook order = tradingService.placeOrder(orderRequest);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/match/{symbol}")
    public ResponseEntity<Map<String, Object>> triggerMatching(@PathVariable String symbol) {
        Map<String, Object> result = tradingService.triggerMatching(symbol);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<Map<String, Object>> getOrderBook(@PathVariable String symbol) {
        Map<String, Object> orderBook = tradingService.getOrderBook(symbol);
        return ResponseEntity.ok(orderBook);
    }

    @GetMapping("/orders/user/{userId}")
    public ResponseEntity<List<Orderbook>> getUserOrders(@PathVariable Long userId) {
        List<Orderbook> orders = tradingService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/demo/populate")
    public ResponseEntity<Map<String, Object>> populateDemoData() {
        Map<String, Object> result = tradingService.createDemoData();
        return ResponseEntity.ok(result);
    }

    public static class OrderRequest {
        private String symbol;
        private Orderbook.Side side;
        private BigDecimal price;
        private Long quantity;
        private Long userId;
        private Orderbook.OrderType orderType = Orderbook.OrderType.LIMIT;

        // Getters and setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }

        public Orderbook.Side getSide() { return side; }
        public void setSide(Orderbook.Side side) { this.side = side; }

        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }

        public Long getQuantity() { return quantity; }
        public void setQuantity(Long quantity) { this.quantity = quantity; }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public Orderbook.OrderType getOrderType() { return orderType; }
        public void setOrderType(Orderbook.OrderType orderType) { this.orderType = orderType; }
    }
}
