package demo;

import org.w3c.dom.Element;

public class QueryTransaction implements Transaction {
    private int accountId;
    private int transactionId;

    public QueryTransaction(int accountId, int transactionId) {
        this.accountId = accountId;
        this.transactionId = transactionId;
    }

    @Override
    public void execute() {

    }

    public int getAccountId() {
        return accountId;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
