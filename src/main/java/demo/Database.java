package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
    private static SessionFactory sessionFactory;

    private static boolean hasInitialize = false;

    public static void init() throws ClassNotFoundException, SQLException {
        if(!hasInitialize) {
            System.out.println("begin initialized db");
            Class.forName("org.postgresql.Driver");
            Connection conn                                     //change it to localhost if you want connect with local db
                = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");

            conn.setAutoCommit(false);

            sessionFactory = buildSessionFactory();
            System.out.println("establish db successfully");
            hasInitialize = true;
        }
    }


    public static SessionFactory getSessionFactory(){
        return sessionFactory;
    }

    /**
     * Test Method, used to create account
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

    public static Account checkAccountIdExistsAndGetIt(int accountId) {
        List<Account> results = null;
        try {
            Session session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
            Root<Account> root = criteria.from(Account.class);
            criteria.select(root).where(builder.equal(root.get("account_id"), Integer.toString(accountId)));
            results = session.createQuery(criteria).getResultList();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (results == null || results.size() <= 0) {
            throw new IllegalArgumentException("Account does not exist");
        }
        return results.get(0);
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

    public static void addSymbol(Symbol symbol) {
        try {
            Session session = sessionFactory.openSession();
            org.hibernate.Transaction tx = session.beginTransaction();
            session.save(symbol);
            tx.commit();
            session.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    /****************** initialize the table when DB is booted   **********************/

    /**
     * Build session factory
     * @return unique session factory
     */
    private static SessionFactory buildSessionFactory(){
        try{
            if(sessionFactory == null){
                Configuration cfg = new Configuration().configure();

                /** adding these will create table automatically */
                cfg.addAnnotatedClass(Account.class);
                cfg.addAnnotatedClass(Order.class);
                cfg.addAnnotatedClass(Symbol.class);
                org.hibernate.boot.registry.StandardServiceRegistryBuilder builder =
                    new org.hibernate.boot.registry.StandardServiceRegistryBuilder().applySettings(cfg.getProperties());
                return cfg.buildSessionFactory(builder.build());
            }else{
                return sessionFactory;
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
