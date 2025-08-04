package se.oscarwiklund.nilzexchange.service.matching;

import org.springframework.stereotype.Component;
import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.model.Trade;
import se.oscarwiklund.nilzexchange.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderMatcher {

    public MatchResult executeMatch(Orderbook buyOrder, Orderbook sellOrder) {
        long quantityToTrade = Math.min(
                buyOrder.getQuantity() - buyOrder.getFilledQuantity(),
                sellOrder.getQuantity() - sellOrder.getFilledQuantity()
        );

        BigDecimal tradePrice = sellOrder.getPrice();
        BigDecimal total = tradePrice.multiply(BigDecimal.valueOf(quantityToTrade));

        Trade trade = new Trade();
        trade.setBuyerId(buyOrder.getUserId());
        trade.setSellerId(sellOrder.getUserId());
        trade.setSymbol(buyOrder.getSymbol());
        trade.setQuantity(quantityToTrade);
        trade.setPrice(tradePrice);
        trade.setExecutedAt(LocalDateTime.now());
        trade.setBuyOrderId(buyOrder.getId());
        trade.setSellOrderId(sellOrder.getId());

        buyOrder.setFilledQuantity(buyOrder.getFilledQuantity() + quantityToTrade);
        sellOrder.setFilledQuantity(sellOrder.getFilledQuantity() + quantityToTrade);

        if (buyOrder.getFilledQuantity().equals(buyOrder.getQuantity())) {
            buyOrder.setStatus(Orderbook.OrderStatus.COMPLETED);
        }
        if (sellOrder.getFilledQuantity().equals(sellOrder.getQuantity())) {
            sellOrder.setStatus(Orderbook.OrderStatus.COMPLETED);
        }

        // Transaktioner
        List<Transaction> txs = List.of(
                createTx(buyOrder.getUserId(), Transaction.Type.DEBIT_CASH, "SEK", total, trade),
                createTx(buyOrder.getUserId(), Transaction.Type.CREDIT_ASSET, buyOrder.getSymbol(), BigDecimal.valueOf(quantityToTrade), trade),
                createTx(sellOrder.getUserId(), Transaction.Type.DEBIT_ASSET, sellOrder.getSymbol(), BigDecimal.valueOf(quantityToTrade), trade),
                createTx(sellOrder.getUserId(), Transaction.Type.CREDIT_CASH, "SEK", total, trade)
        );

        return new MatchResult(trade, txs, buyOrder, sellOrder);
    }

    private Transaction createTx(Long userId, Transaction.Type type, String asset, BigDecimal amount, Trade trade) {
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setType(type);
        tx.setAsset(asset);
        tx.setAmount(amount);
        tx.setCreatedAt(LocalDateTime.now());
        tx.setTradeId(trade.getId()); // sätts efter persistens om du inte kör cascade
        return tx;
    }
}
