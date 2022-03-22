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
     * Test Method, used to print all accounts in the database
     */
    public static void getAllAccounts() {
        try {
            Session session = sessionFactory.openSession();
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
            criteria.from(Account.class);
            List<Account> accountList = session.createQuery(criteria).getResultList();
            for (Account account : accountList) {
                System.out.println(account.getId());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
