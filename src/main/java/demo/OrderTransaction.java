package demo;

import org.w3c.dom.Element;

public class OrderTransaction implements Transaction {
    private String accountId;
    private Order order;

    public OrderTransaction(String accountId, String sym, double amount, double priceLimit) {
        this.accountId = accountId;
        this.order = new Order(sym, amount, priceLimit);
    }

    @Override
    public void execute() {
        Database.addOrder(order);
    }

    public String getAccountId() {
        return accountId;
    }

    public Order getOrder() {
        return order;
    }
}
