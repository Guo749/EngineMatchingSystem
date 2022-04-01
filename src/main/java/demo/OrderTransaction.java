package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javafx.util.Pair;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class OrderTransaction extends Transaction {
    private Account account;
    private Order order;

    public OrderTransaction(Account account, String sym, double amount, double priceLimit) {
        this.account = account;
        this.order = new Order(account, sym, amount, priceLimit);
    }

    @Override
    public Element execute(Document results) {
        Element openedResult = null;
        SessionFactory sessionFactory = Database.getSessionFactory();
        Session session = sessionFactory.openSession();
        try (session) {
            org.hibernate.Transaction tx = session.beginTransaction();
            if (isBuyOrder()) {
                deductBuyOrderCost(session);
            } else {
                deductSellOrderShares(session);
            }
            session.save(order);
            openedResult = createOpenedResult(results);
            // The relatedOrder is sorted by price limit (descending)
            List<Order> relatedOrders = getOpenOrdersWithSym(session, order.getSym());
            while (relatedOrders.size() > 1) {
                Pair<Order, Order> matchedOrders = findMatchedOrders(relatedOrders);
                if (matchedOrders == null) {
                    break;
                }
                executeMatchedOrders(session, order, matchedOrders.getKey(), matchedOrders.getValue());
                relatedOrders = getOpenOrdersWithSym(session, order.getSym());
            }
            session.flush();
            tx.commit();
        }
        catch (Exception e) {
            return createErrorResultForOrderTrans(results, e.getMessage());
        }
        return openedResult;
    }

    private boolean isBuyOrder () {
        return order.getAmount() > 0;
    }

    /**
     * When a buy order is placed, this method will deduct the total cost from the buyer's account
     * @param session is the database session
     */
    private void deductBuyOrderCost(Session session) {
        double totalCost = order.getAmount() * order.getPriceLimit();
        if (account.getBalance() < totalCost) {
            throw new IllegalArgumentException("The buyer's account does not have enough balance");
        }
        account.setBalance(account.getBalance() - totalCost);
        session.update(account);
    }

    /**
     * When a sell order is placed, this method will deduct the shares from the seller's account
     * @param session is the database session
     */
    private void deductSellOrderShares(Session session) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Symbol> criteria = builder.createQuery(Symbol.class);
        Root<Symbol> root = criteria.from(Symbol.class);
        criteria.select(root).where(builder.equal(root.get("account_id"), account.getAccountNum()),
                builder.equal(root.get("name"), order.getSym()));
        List<Symbol> results = session.createQuery(criteria).getResultList();
        if (results.size() <= 0) {
            throw new IllegalArgumentException("The seller's account does not have shares of " + order.getSym() + " to sell");
        }
        if (results.size() > 1) {
            throw new IllegalArgumentException("The seller's account has duplicate record of " + order.getSym() + " stock");
        }
        Symbol symbol = results.get(0);
        if (symbol.getShare() < (0 - order.getAmount())) {
            throw new IllegalArgumentException("The seller's account does not have enough shares of " + order.getSym() + " to sell");
        }
        symbol.setShare(symbol.getShare() + order.getAmount());
        session.update(symbol);
    }

    public List<Order> getOpenOrdersWithSym(Session session, String sym) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Order> criteria = builder.createQuery(Order.class);
        Root<Order> root = criteria.from(Order.class);
        criteria.select(root).where(builder.equal(root.get("sym"), sym),
                        builder.equal(root.get("status"), OrderStatus.OPEN))
                .orderBy(builder.desc(root.get("priceLimit")),
                        builder.asc(root.get("time")));
        return session.createQuery(criteria).getResultList();
    }

    /**
     * Try to find two orders that can be matched and executed from the order list
     * @param orders is the order list
     * @return a pair of orders. The key is the Buy order, and the value is the Sell order
     * TODO: Confirming the meaning of best price match
     */
    private Pair<Order, Order> findMatchedOrders(List<Order> orders) {
        Integer firstBuyOrderIndex = null;
        for (int i = 0; i < orders.size(); i++) {
            if (orders.get(i).getAmount() > 0 && isValidOrder(orders.get(i))) {
                firstBuyOrderIndex = i;
                break;
            }
        }
        // There is no Buy order
        if (firstBuyOrderIndex == null) {
            return null;
        }
        // Find the first Sell order after the Buy order
        Integer lastSellOrderIndex = null;
        for (int i = firstBuyOrderIndex + 1; i < orders.size(); i++) {
            if (orders.get(i).getAmount() < 0 && isValidOrder(orders.get(i))) {
                lastSellOrderIndex = i;
            }
        }
        // There is no Sell order after the first Buy order => Cannot match any orders
        if (lastSellOrderIndex == null) {
            return null;
        }
        // Already got a Buy order and a Sell order => Match them
        return new Pair<>(orders.get(firstBuyOrderIndex), orders.get(lastSellOrderIndex));
    }

    private boolean isValidOrder(Order orderToCheck) {
        if (orderToCheck.getId() == order.getId()) {
            return true;
        }
        return !Objects.equals(orderToCheck.getAccount().getAccountNum(), account.getAccountNum());
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
    public void executeMatchedOrders(Session session, Order currentOrder, Order buyOrder, Order sellOrder) {
        try {
            Order buyOrderToExecute = buyOrder, sellOrderToExecute = sellOrder;
            List<Order> ordersToUpdate = new ArrayList<>();
            ordersToUpdate.add(buyOrder);
            ordersToUpdate.add(sellOrder);

            double transactionPrice = determinePrice(currentOrder, buyOrder, sellOrder);
            if (buyOrder.getAmount() < (0 - sellOrder.getAmount())) {
                // It is doing PLUS because the selling amount is negative
                sellOrder.setAmount(sellOrder.getAmount() + buyOrder.getAmount());
                sellOrderToExecute = new Order(sellOrder.getAccount(), sellOrder.getSym(), (0 - buyOrderToExecute.getAmount()), transactionPrice);
                session.save(sellOrderToExecute);
                sellOrder.addChildOrder(sellOrderToExecute);
                ordersToUpdate.add(sellOrderToExecute);
            }
            else if (buyOrder.getAmount() > (0 - sellOrder.getAmount())) {
                buyOrder.setAmount(buyOrder.getAmount() + sellOrder.getAmount());
                buyOrderToExecute = new Order(buyOrder.getAccount(), buyOrder.getSym(), (0 - sellOrderToExecute.getAmount()), transactionPrice);
                session.save(buyOrderToExecute);
                buyOrder.addChildOrder(buyOrderToExecute);
                ordersToUpdate.add(buyOrderToExecute);
            }

            // If buyOrder amount == sellOrder amount, the buy/sellOrderToExecute is unchanged (original buy/sellOrder)
            buyOrderToExecute.setStatus(OrderStatus.EXECUTED);
            sellOrderToExecute.setStatus(OrderStatus.EXECUTED);
            double priceDifference = buyOrder.getPriceLimit() - transactionPrice;
            buyOrderToExecute.setPriceLimit(transactionPrice);
            sellOrderToExecute.setPriceLimit(transactionPrice);
            // Set the time in Order to current time (executed time)
            buyOrderToExecute.setTimeToNow();
            sellOrderToExecute.setTimeToNow();

            creditSellerAccountBalance(session, sellOrderToExecute);
            creditBuyerAccountShares(session, buyOrderToExecute);
            refundMoneyToBuyer(session, buyOrderToExecute, priceDifference);

            for (Order order : ordersToUpdate) {
                session.update(order);
            }
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
    private double determinePrice(Order currentOrder, Order buyOrder, Order sellOrder) {
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

    private void creditSellerAccountBalance(Session session, Order executedSellOrder) {
        double totalEarned = executedSellOrder.getAmount() * (0 - executedSellOrder.getPriceLimit());
        Account seller = executedSellOrder.getAccount();
        seller.setBalance(seller.getBalance() + totalEarned);
        session.update(seller);
    }

    private void creditBuyerAccountShares(Session session, Order executedBuyOrder) {
        double shares = executedBuyOrder.getAmount();
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Symbol> criteria = builder.createQuery(Symbol.class);
        Root<Symbol> root = criteria.from(Symbol.class);
        criteria.select(root).where(builder.equal(root.get("account_id"), executedBuyOrder.getAccount().getAccountNum()),
                builder.equal(root.get("name"), executedBuyOrder.getSym()));
        List<Symbol> results = session.createQuery(criteria).getResultList();
        if (results.size() <= 0) {
            Symbol newSymbol = new Symbol(executedBuyOrder.getSym(), executedBuyOrder.getAccount().getAccountNum(),
                    executedBuyOrder.getAmount());
            session.save(newSymbol);
        }
        else {
            Symbol currentSymbol = results.get(0);
            currentSymbol.setShare(currentSymbol.getShare() + executedBuyOrder.getAmount());
            session.update(currentSymbol);
        }
    }

    private void refundMoneyToBuyer(Session session, Order executedBuyOrder, double priceDifference) {
        double moneyToRefund = priceDifference * executedBuyOrder.getAmount();
        Account buyer = executedBuyOrder.getAccount();
        buyer.setBalance(buyer.getBalance() + moneyToRefund);
        session.update(buyer);
    }

    private Element createOpenedResult(Document results) {
        Element opened = results.createElement("opened");
        setOrderAttributes(opened);
        opened.setAttribute("id", Integer.toString(order.getId()));
        return opened;
    }

    private Element createErrorResultForOrderTrans(Document results, String errorMsg) {
        Element error = results.createElement("error");
        setOrderAttributes(error);
        error.appendChild(results.createTextNode("Error when executing the order: " + errorMsg));
        return error;
    }

    private void setOrderAttributes(Element element) {
        element.setAttribute("sym", order.getSym());
        element.setAttribute("amount", Double.toString(order.getAmount()));
        element.setAttribute("limit", Double.toString(order.getPriceLimit()));
    }

    public Account getAccount() {
        return account;
    }

    public Order getOrder() {
        return order;
    }
}
