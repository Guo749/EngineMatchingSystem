package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class CancelTransaction extends Transaction {
    private int accountId;
    private int transactionId;

    public CancelTransaction(int accountId, int transactionId) {
        this.accountId = accountId;
        this.transactionId = transactionId;
    }

    @Override
    public Element execute(Document results) {
        SessionFactory sessionFactory = Database.getSessionFactory();
        Session session = sessionFactory.openSession();
        List<Order> orderList = null;
        try (session) {
            org.hibernate.Transaction tx = session.beginTransaction();
            Order parentOrder = getOrderWithId(session, transactionId);
            orderList = getAllRelatedOrders(parentOrder);
            tryCancelOrders(session, orderList);
            tx.commit();
        }
        catch (Exception e) {
            return createErrorResult(results, e.getMessage());
        }
        return createStatusOrCanceledResult(false, results, orderList, transactionId);
    }

    private void tryCancelOrders(Session session, List<Order> orderList) {
        boolean hasOrderToCancel = false;
        for (Order order : orderList) {
            if (order.getStatus() == OrderStatus.OPEN) {
                hasOrderToCancel = true;
                order.setStatus(OrderStatus.CANCELED);
                // Set the time in the order to be current time (canceled time)
                order.setTimeToNow();
                session.update(order);
            }
        }
        if (!hasOrderToCancel) {
            throw new IllegalArgumentException("There is no OPEN order to cancel in the transaction " + transactionId);
        }
    }

    public int getAccountId() {
        return accountId;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
