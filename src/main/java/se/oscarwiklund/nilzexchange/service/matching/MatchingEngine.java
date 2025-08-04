package se.oscarwiklund.nilzexchange.service.matching;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.oscarwiklund.nilzexchange.model.Orderbook;
import se.oscarwiklund.nilzexchange.model.Transaction;
import se.oscarwiklund.nilzexchange.repository.*;

import java.util.List;

/*
Step 1: Recive relevant data from Orderbook
Step 2. Sort and match BUY and SELL orders
Step 3: Create trade objects for matched orders
Step 4: Update Orderbook with filled quantities and status
Step 5: Send to TransactionService
 */
@Service
public class MatchingEngine {

    private final OrderbookRepository orderbookRepository;
    private final TradeRepository tradeRepository;
    private final TransactionRepository transactionRepository;
    private final OrderMatcher orderMatcher;

    public MatchingEngine(
            OrderbookRepository orderbookRepository,
            TradeRepository tradeRepository,
            TransactionRepository transactionRepository,
            OrderMatcher orderMatcher
    ) {
        this.orderbookRepository = orderbookRepository;
        this.tradeRepository = tradeRepository;
        this.transactionRepository = transactionRepository;
        this.orderMatcher = orderMatcher;
    }

    @Transactional
    public void matchOrders(String symbol) {
        List<Orderbook> buyOrders = orderbookRepository.findMatchingBuyOrders(symbol);
        List<Orderbook> sellOrders = orderbookRepository.findMatchingSellOrders(symbol);

        int buyIndex = 0;
        int sellIndex = 0;
        while (buyIndex < buyOrders.size() && sellIndex < sellOrders.size()) {
            Orderbook buyOrder = buyOrders.get(buyIndex);
            Orderbook sellOrder = sellOrders.get(sellIndex);

            if (!canMatch(buyOrder, sellOrder)) {
                // If not matchable, move to next sell order
                sellIndex++;
                continue;
            }

            MatchResult result = orderMatcher.executeMatch(buyOrder, sellOrder);
            tradeRepository.save(result.getTrade());

            for (Transaction tx : result.getTransactions()) {
                tx.setTradeId(result.getTrade().getId());
            }
            transactionRepository.saveAll(result.getTransactions());
            orderbookRepository.save(result.getBuyOrder());
            orderbookRepository.save(result.getSellOrder());

            // Move to next buy/sell order if completed
            if (result.getBuyOrder().getStatus() == Orderbook.OrderStatus.COMPLETED) buyIndex++;
            if (result.getSellOrder().getStatus() == Orderbook.OrderStatus.COMPLETED) sellIndex++;
        }
    }

    private boolean canMatch(Orderbook buy, Orderbook sell) {
        return buy.getPrice().compareTo(sell.getPrice()) >= 0 &&
                buy.getSymbol().equals(sell.getSymbol()) &&
                !buy.getUserId().equals(sell.getUserId()) &&
                buy.getStatus() == Orderbook.OrderStatus.OPEN &&
                sell.getStatus() == Orderbook.OrderStatus.OPEN;
    }
}
