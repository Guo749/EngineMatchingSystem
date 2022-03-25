package demo;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;


public class Database {
    /* note this is globally unique, we should only instantiate one */
    private static final SessionFactory sessionFactory = buildSessionFactory();

    /**
     * Build session factory
     * @return unique session factory
     */
    private static SessionFactory buildSessionFactory(){
        try{
            if(sessionFactory == null){
                Configuration cfg = new Configuration().configure();

                //todo: add account, order info
                cfg.addAnnotatedClass(Account.class);
                cfg.addAnnotatedClass(Order.class);
                org.hibernate.boot.registry.StandardServiceRegistryBuilder builder =
                    new org.hibernate.boot.registry.StandardServiceRegistryBuilder().applySettings(cfg.getProperties());
                return cfg.buildSessionFactory(builder.build());
            }else{
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }

    /**
     * Test Method, used to create account
     * @param account
     */
    public static void createAccount(Account account){
        try{
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            session.save(account);
            tx.commit();
            session.close();
        }catch (Exception e){

        }
    }

    /**
     * Used to add an order to the database
     * @param order is the order to add to the database
     */
    public static void addOrder(Order order) {
        try {
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            session.save(order);
            tx.commit();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
    public static void executeMatchedOrders(Order currentOrder, Order buyOrder, Order sellOrder) {
        try {
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();

            Order buyOrderToExecute = buyOrder, sellOrderToExecute = sellOrder;
            List<Order> ordersToUpdate = new ArrayList<>();
            ordersToUpdate.add(buyOrder);
            ordersToUpdate.add(sellOrder);

            double transactionPrice = determinePrice(currentOrder, buyOrder, sellOrder);
            if (buyOrder.getAmount() < (0 - sellOrder.getAmount())) {
                // It is doing PLUS because the selling amount is negative
                sellOrder.setAmount(sellOrder.getAmount() + buyOrder.getAmount());
                sellOrderToExecute = new Order(sellOrder.getSym(), (0 - buyOrderToExecute.getAmount()), transactionPrice);
                session.save(sellOrderToExecute);
                sellOrder.addChildOrder(sellOrderToExecute);
                ordersToUpdate.add(sellOrderToExecute);
            }
            else if (buyOrder.getAmount() > (0 - sellOrder.getAmount())) {
                buyOrder.setAmount(buyOrder.getAmount() + sellOrder.getAmount());
                buyOrderToExecute = new Order(buyOrder.getSym(), (0 - sellOrderToExecute.getAmount()), transactionPrice);
                session.save(buyOrderToExecute);
                buyOrder.addChildOrder(buyOrderToExecute);
                ordersToUpdate.add(buyOrderToExecute);
            }

            // If buyOrder amount == sellOrder amount, the buy/sellOrderToExecute is unchanged (original buy/sellOrder)
            buyOrderToExecute.setStatus(OrderStatus.EXECUTED);
            sellOrderToExecute.setStatus(OrderStatus.EXECUTED);
            buyOrderToExecute.setPriceLimit(transactionPrice);
            sellOrderToExecute.setPriceLimit(transactionPrice);

            // TODO: Change the seller’s account balance and the buyer’s number of shares

            for (Order order : ordersToUpdate) {
                session.update(order);
            }
            tx.commit();
            session.close();
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
    private static double determinePrice(Order currentOrder, Order buyOrder, Order sellOrder) {
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

    // TODO: Several similar methods, try to refactor
    public static List<Order> getOpenOrdersWithSym(String sym) {
        List<Order> results = new ArrayList<Order>();
        try {
            Session session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Order> criteria = builder.createQuery(Order.class);
            Root<Order> root = criteria.from(Order.class);
            criteria.select(root).where(builder.equal(root.get("sym"), sym),
                            builder.equal(root.get("status"), OrderStatus.OPEN))
                    .orderBy(builder.desc(root.get("priceLimit")),
                            builder.asc(root.get("time")));
            results = session.createQuery(criteria).getResultList();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public static List<Order> getOrdersWithId(String id) {
        List<Order> results = new ArrayList<Order>();
        try {
            Session session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Order> criteria = builder.createQuery(Order.class);
            Root<Order> root = criteria.from(Order.class);
            criteria.select(root).where(builder.equal(root.get("id"), id));
            results = session.createQuery(criteria).getResultList();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    public static void updateOrders(List<Order> orders) {
        try {
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            for (Order order : orders) {
                session.update(order);
            }
            tx.commit();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Test Method, used to print all orders in the database
     */
    public static List<Order> getAllOrders() {
        try {
            Session session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Order> criteria = builder.createQuery(Order.class);
            criteria.from(Order.class);
            return session.createQuery(criteria).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
