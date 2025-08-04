package se.oscarwiklund.nilzexchange.service.matching;

import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.model.Trade;
import se.oscarwiklund.nilzexchange.model.Transaction;

import java.util.List;

public class MatchResult {
    private final Trade trade;
    private final List<Transaction> transactions;
    private final Orderbook buyOrder;
    private final Orderbook sellOrder;

    public MatchResult(Trade trade, List<Transaction> transactions, Orderbook buyOrder, Orderbook sellOrder) {
        this.trade = trade;
        this.transactions = transactions;
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
    }

    public Trade getTrade() { return trade; }
    public List<Transaction> getTransactions() { return transactions; }
    public Orderbook getBuyOrder() { return buyOrder; }
    public Orderbook getSellOrder() { return sellOrder; }
}
