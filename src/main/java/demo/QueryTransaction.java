package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class QueryTransaction implements Transaction {
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
        return createStatusResult(results, orderList);
    }

    private Order getOrderWithId(Session session, int transactionId) {
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Order> criteria = builder.createQuery(Order.class);
        Root<Order> root = criteria.from(Order.class);
        criteria.select(root).where(builder.equal(root.get("id"), transactionId));
        List<Order> orderList = session.createQuery(criteria).getResultList();
        if (orderList.size() <= 0) {
            throw new IllegalArgumentException("The transaction ID is invalid");
        }
        return orderList.get(0);
    }

    private List<Order> getAllRelatedOrders(Order parentOrder) {
        List<Order> orderList = new ArrayList<>();
        orderList.add(parentOrder);
        Set<Order> childOrders = parentOrder.getChildOrders();
        for (Order childOrder : childOrders) {
            orderList.add(childOrder);
            if (childOrder.getChildOrders().size() > 0) {
                orderList.addAll(getAllRelatedOrders(childOrder));
            }
        }
        return orderList;
    }

    private Element createStatusResult(Document results, List<Order> orderList) {
        Element status = results.createElement("status");
        status.setAttribute("id", Integer.toString(transactionId));
        for (Order order : orderList) {
            Element orderStatus = null;
            switch (order.getStatus()) {
                case OPEN -> orderStatus = createOpenStatusResult(results, order);
                case CANCELED -> orderStatus = createCanceledStatusResult(results, order);
                case EXECUTED -> orderStatus = createExecutedStatusResult(results, order);
            }
            if (orderStatus == null) {
                throw new IllegalArgumentException("Invalid order status");
            }
            status.appendChild(orderStatus);
        }
        return status;
    }

    private Element createOpenStatusResult(Document results, Order order) {
        Element open = results.createElement("open");
        open.setAttribute("shares", Double.toString(order.getAmount()));
        return open;
    }

    private Element createCanceledStatusResult(Document results, Order order) {
        Element canceled = results.createElement("canceled");
        canceled.setAttribute("shares", Double.toString(order.getAmount()));
        canceled.setAttribute("time", Double.toString(order.getTime()));
        return canceled;
    }

    private Element createExecutedStatusResult(Document results, Order order) {
        Element executed = results.createElement("executed");
        executed.setAttribute("shares", Double.toString(order.getAmount()));
        executed.setAttribute("price", Double.toString(order.getPriceLimit()));
        executed.setAttribute("time", Long.toString(order.getTime()));
        return executed;
    }

    private Element createErrorResult(Document results, String errorMsg) {
        Element error = results.createElement("error");
        error.appendChild(results.createTextNode("Error when executing the order: " + errorMsg));
        return error;
    }

    public int getAccountId() {
        return accountId;
    }

    public int getTransactionId() {
        return transactionId;
    }
}
