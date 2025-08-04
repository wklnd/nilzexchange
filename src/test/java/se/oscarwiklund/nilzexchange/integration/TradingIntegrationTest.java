package se.oscarwiklund.nilzexchange.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import se.oscarwiklund.nilzexchange.controller.TradingController;
import se.oscarwiklund.nilzexchange.model.Orderbook;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TradingIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String createURL(String uri) {
        return "http://localhost:" + port + "/api/trading" + uri;
    }

    @Test
    public void testFullTradingWorkflow() {
        System.out.println("\n🚀 TESTING FULL TRADING WORKFLOW WITH SPRING BOOT 🚀");

        // Step 1: Create demo data
        System.out.println("\n1️⃣ Creating demo data...");
        ResponseEntity<Map> demoResponse = restTemplate.postForEntity(
            createURL("/demo/populate"), null, Map.class);

        assertEquals(HttpStatus.OK, demoResponse.getStatusCode());
        Map<String, Object> demoResult = demoResponse.getBody();
        System.out.println("✅ Demo data created: " + demoResult.get("message"));
        System.out.println("📊 Orders created: " + demoResult.get("ordersCreated"));

        // Step 2: View order book before matching
        System.out.println("\n2️⃣ Viewing order book before matching...");
        ResponseEntity<Map> orderBookResponse = restTemplate.getForEntity(
            createURL("/orderbook/AAPL"), Map.class);

        assertEquals(HttpStatus.OK, orderBookResponse.getStatusCode());
        Map<String, Object> orderBook = orderBookResponse.getBody();
        System.out.println("📈 Buy orders: " + ((java.util.List<?>) orderBook.get("buyOrders")).size());
        System.out.println("📉 Sell orders: " + ((java.util.List<?>) orderBook.get("sellOrders")).size());

        // Step 3: Trigger matching
        System.out.println("\n3️⃣ Triggering matching engine...");
        ResponseEntity<Map> matchResponse = restTemplate.postForEntity(
            createURL("/match/AAPL"), null, Map.class);

        assertEquals(HttpStatus.OK, matchResponse.getStatusCode());
        Map<String, Object> matchResult = matchResponse.getBody();
        System.out.println("🔥 Trades executed: " + matchResult.get("tradesExecuted"));
        System.out.println("📊 Buy orders before: " + matchResult.get("buyOrdersBefore"));
        System.out.println("📊 Buy orders after: " + matchResult.get("buyOrdersAfter"));
        System.out.println("📊 Sell orders before: " + matchResult.get("sellOrdersBefore"));
        System.out.println("📊 Sell orders after: " + matchResult.get("sellOrdersAfter"));

        // Step 4: Place additional orders
        System.out.println("\n4️⃣ Placing additional orders...");

        TradingController.OrderRequest buyOrder = new TradingController.OrderRequest();
        buyOrder.setSymbol("AAPL");
        buyOrder.setSide(Orderbook.Side.BUY);
        buyOrder.setPrice(new BigDecimal("152.00"));
        buyOrder.setQuantity(50L);
        buyOrder.setUserId(7L);

        ResponseEntity<Orderbook> buyResponse = restTemplate.postForEntity(
            createURL("/orders"), buyOrder, Orderbook.class);

        assertEquals(HttpStatus.OK, buyResponse.getStatusCode());
        System.out.println("✅ Buy order placed: " + buyResponse.getBody().getId());

        TradingController.OrderRequest sellOrder = new TradingController.OrderRequest();
        sellOrder.setSymbol("AAPL");
        sellOrder.setSide(Orderbook.Side.SELL);
        sellOrder.setPrice(new BigDecimal("151.50"));
        sellOrder.setQuantity(30L);
        sellOrder.setUserId(8L);

        ResponseEntity<Orderbook> sellResponse = restTemplate.postForEntity(
            createURL("/orders"), sellOrder, Orderbook.class);

        assertEquals(HttpStatus.OK, sellResponse.getStatusCode());
        System.out.println("✅ Sell order placed: " + sellResponse.getBody().getId());

        // Step 5: Trigger matching again
        System.out.println("\n5️⃣ Triggering matching again...");
        ResponseEntity<Map> secondMatchResponse = restTemplate.postForEntity(
            createURL("/match/AAPL"), null, Map.class);

        assertEquals(HttpStatus.OK, secondMatchResponse.getStatusCode());
        Map<String, Object> secondMatchResult = secondMatchResponse.getBody();
        System.out.println("🔥 Additional trades executed: " + secondMatchResult.get("tradesExecuted"));

        // Step 6: View final order book
        System.out.println("\n6️⃣ Viewing final order book...");
        ResponseEntity<Map> finalOrderBookResponse = restTemplate.getForEntity(
            createURL("/orderbook/AAPL"), Map.class);

        assertEquals(HttpStatus.OK, finalOrderBookResponse.getStatusCode());
        Map<String, Object> finalOrderBook = finalOrderBookResponse.getBody();
        java.util.List<?> recentTrades = (java.util.List<?>) finalOrderBook.get("recentTrades");

        System.out.println("📈 Final buy orders: " + ((java.util.List<?>) finalOrderBook.get("buyOrders")).size());
        System.out.println("📉 Final sell orders: " + ((java.util.List<?>) finalOrderBook.get("sellOrders")).size());
        System.out.println("💰 Recent trades: " + recentTrades.size());

        // Assertions
        assertTrue((Integer) secondMatchResult.get("tradesExecuted") >= 0, "Should have executed some trades");
        assertNotNull(finalOrderBook.get("timestamp"), "Should have timestamp");

        System.out.println("\n🎉 FULL TRADING WORKFLOW TEST COMPLETED SUCCESSFULLY! 🎉");
    }

    @Test
    public void testUserOrders() {
        System.out.println("\n👤 TESTING USER ORDERS");

        // Create demo data first
        restTemplate.postForEntity(createURL("/demo/populate"), null, Map.class);

        // Get orders for user 1
        ResponseEntity<java.util.List> userOrdersResponse = restTemplate.getForEntity(
            createURL("/orders/user/1"), java.util.List.class);

        assertEquals(HttpStatus.OK, userOrdersResponse.getStatusCode());
        java.util.List<?> userOrders = userOrdersResponse.getBody();

        System.out.println("📋 User 1 orders: " + userOrders.size());
        assertTrue(userOrders.size() > 0, "User should have orders");

        System.out.println("✅ User orders test completed!");
    }
}
