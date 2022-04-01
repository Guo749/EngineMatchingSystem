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
                = DriverManager.getConnection("jdbc:postgresql://db:5432/postgres", "postgres", "postgres");

            conn.setAutoCommit(false);

            sessionFactory = buildSessionFactory();
            System.out.println("establish db successfully");
            hasInitialize = true;
        }
    }


    public static SessionFactory getSessionFactory(){
        return sessionFactory;
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
