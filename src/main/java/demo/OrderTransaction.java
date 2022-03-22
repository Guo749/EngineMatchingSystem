package demo;

import org.w3c.dom.Element;

public class OrderTransaction implements Transaction {
    private String accountId;
    private Order order;

    public OrderTransaction(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void parse(Element element) {

    }

    @Override
    public void execute() {

    }
}
