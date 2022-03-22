package demo;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;


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
