package demo;

import org.w3c.dom.Element;

public class QueryTransaction implements Transaction {
    private String accountId;

    public QueryTransaction(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void parse(Element element) {

    }

    @Override
    public void execute() {

    }
}
