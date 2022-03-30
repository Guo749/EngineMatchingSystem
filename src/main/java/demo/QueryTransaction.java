package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class QueryTransaction extends Transaction {
    private int accountId;
    private int transactionId;

    public QueryTransaction(int accountId, int transactionId) {
        this.accountId = accountId;
        this.transactionId = transactionId;
    }

    @Override
    public Element execute(Document results) {
        SessionFactory sessionFactory = Database.getSessionFactory();
        Session session = sessionFactory.openSession();
        List<Order> orderList = null;
        try (session) {
            Order parentOrder = getOrderWithId(session, transactionId);
            orderList = getAllRelatedOrders(parentOrder);
        }
        catch (Exception e) {
            return createErrorResult(results, e.getMessage());
        }
        return createStatusOrCanceledResult(true, results, orderList, transactionId);
    }

    public int getAccountId() {
        return accountId;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
