package demo;

import org.w3c.dom.Element;

public class CancelTransaction implements Transaction {
    private int accountId;
    private int transactionId;

    public CancelTransaction(int accountId, int transactionId) {
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
