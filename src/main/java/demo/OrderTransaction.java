package demo;

import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import javafx.util.Pair;

public class OrderTransaction implements Transaction {
    private int accountId;
    private Order order;

    public OrderTransaction(int accountId, String sym, double amount, double priceLimit) {
        this.accountId = accountId;
        this.order = new Order(sym, amount, priceLimit);
    }

    @Override
    public void execute() {
        // TODO: When a Buy order is placed, the total cost is deducted from the buyer’s account
        // TODO: When a Sell order is placed, the shares are deducted from the seller's account
        Database.addOrder(order);
        // The relatedOrder is sorted by price limit (descending)
        List<Order> relatedOrders = Database.getOpenOrdersWithSym(order.getSym());
        while (relatedOrders.size() > 1) {
            Pair<Order, Order> matchedOrders = findMatchedOrders(relatedOrders);
            if (matchedOrders == null) {
                break;
            }
            Database.executeMatchedOrders(order, matchedOrders.getKey(), matchedOrders.getValue());
            relatedOrders = Database.getOpenOrdersWithSym(order.getSym());
        }
    }

    /**
     * Try to find two orders that can be matched and executed from the order list
     * @param orders is the order list
     * @return a pair of orders. The key is the Buy order, and the value is the Sell order
     * TODO: Cannot match Buy and Sell orders from the same account. Perhaps solve it by getting the first order
     *       that is not this account's order or is the current order, but need to prove the correctness.
     */
    private Pair<Order, Order> findMatchedOrders(List<Order> orders) {
        Integer firstBuyOrderIndex = null;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getAmount() > 0) {
                firstBuyOrderIndex = i;
                break;
            }
        }
        // There is no Buy order
        if (firstBuyOrderIndex == null) {
            return null;
        }
        // Find the first Sell order after the Buy order
        Integer firstSellOrderIndex = null;
        for (int i = firstBuyOrderIndex + 1; i < orders.size(); i++) {
            if (orders.get(i).getAmount() < 0) {
                firstSellOrderIndex = i;
                break;
            }
        }
        // There is no Sell order after the first Buy order => Cannot match any orders
        if (firstSellOrderIndex == null) {
            return null;
        }
        // Already got a Buy order and a Sell order => Match them
        return new Pair<>(orders.get(firstBuyOrderIndex), orders.get(firstSellOrderIndex));
    }

    public int getAccountId() {
        return accountId;
    }

    public Order getOrder() {
        return order;
    }
}
