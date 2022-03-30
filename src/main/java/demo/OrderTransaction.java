package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
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
    public Element execute(Document results) {
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
            executeMatchedOrders(order, matchedOrders.getKey(), matchedOrders.getValue());
            relatedOrders = Database.getOpenOrdersWithSym(order.getSym());
        }
        return createOpenedResult(results);
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

    /**
     * Given a pair of matched orders, try to execute them and update the database correspondingly.
     * There are 3 cases:
     * - buying amount == selling amount => mark original buyOrder and sellOrder as executed
     * - buying amount < selling amount => {
     *     original sellOrder amount -= buying amount
     *     original sellOrder is kept open
     *     create a new sell order, amount = buying amount
     *     mark the new sell order and the original buy order as executed
     * }
     * - buying amount > selling amount => similar to the above case
     * @param buyOrder is the Buy order
     * @param sellOrder is the Sell order
     */
    public static void executeMatchedOrders(Order currentOrder, Order buyOrder, Order sellOrder) {
        try {
            SessionFactory sessionFactory = Database.getSessionFactory();
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            Order buyOrderToExecute = buyOrder, sellOrderToExecute = sellOrder;
            List<Order> ordersToUpdate = new ArrayList<>();
            ordersToUpdate.add(buyOrder);
            ordersToUpdate.add(sellOrder);

            double transactionPrice = determinePrice(currentOrder, buyOrder, sellOrder);
            if (buyOrder.getAmount() < (0 - sellOrder.getAmount())) {
                // It is doing PLUS because the selling amount is negative
                sellOrder.setAmount(sellOrder.getAmount() + buyOrder.getAmount());
                sellOrderToExecute = new Order(sellOrder.getSym(), (0 - buyOrderToExecute.getAmount()), transactionPrice);
                session.save(sellOrderToExecute);
                sellOrder.addChildOrder(sellOrderToExecute);
                ordersToUpdate.add(sellOrderToExecute);
            }
            else if (buyOrder.getAmount() > (0 - sellOrder.getAmount())) {
                buyOrder.setAmount(buyOrder.getAmount() + sellOrder.getAmount());
                buyOrderToExecute = new Order(buyOrder.getSym(), (0 - sellOrderToExecute.getAmount()), transactionPrice);
                session.save(buyOrderToExecute);
                buyOrder.addChildOrder(buyOrderToExecute);
                ordersToUpdate.add(buyOrderToExecute);
            }

            // If buyOrder amount == sellOrder amount, the buy/sellOrderToExecute is unchanged (original buy/sellOrder)
            buyOrderToExecute.setStatus(OrderStatus.EXECUTED);
            sellOrderToExecute.setStatus(OrderStatus.EXECUTED);
            buyOrderToExecute.setPriceLimit(transactionPrice);
            sellOrderToExecute.setPriceLimit(transactionPrice);

            // TODO: Change the seller’s account balance and the buyer’s number of shares

            for (Order order : ordersToUpdate) {
                session.update(order);
            }
            tx.commit();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Determine the price for executing the matched orders. The price should be the early order (the order
     * that have been in the database). So first check whether one of the order is the currentOrder (newly added
     * to the database). If yes, the other order's price should be the executing price.
     * Otherwise, use the earlier order.
     * @param currentOrder
     * @param buyOrder
     * @param sellOrder
     * @return
     */
    private static double determinePrice(Order currentOrder, Order buyOrder, Order sellOrder) {
        if (buyOrder.getId() == currentOrder.getId()) {
            return sellOrder.getPriceLimit();
        }
        if (sellOrder.getId() == currentOrder.getId()) {
            return buyOrder.getPriceLimit();
        }
        if (sellOrder.getTime() < buyOrder.getTime()) {
            return sellOrder.getPriceLimit();
        }
        else {
            return buyOrder.getPriceLimit();
        }
    }

    private Element createOpenedResult(Document results) {
        Element opened = results.createElement("opened");
        setOrderAttributes(opened);
        opened.setAttribute("id", Integer.toString(order.getId()));
        return opened;
    }

    private void setOrderAttributes(Element element) {
        element.setAttribute("sym", order.getSym());
        element.setAttribute("amount", Double.toString(order.getAmount()));
        element.setAttribute("limit", Double.toString(order.getPriceLimit()));
    }

    public int getAccountId() {
        return accountId;
    }

    public Order getOrder() {
        return order;
    }
}
