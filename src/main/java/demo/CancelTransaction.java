package demo;

import org.w3c.dom.Element;

public class CancelTransaction implements Transaction {
    private String accountId;

    public CancelTransaction(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public void execute() {

    }
}
