package demo;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * The class is to do the create, two parts
 *  1. creat the account
 *  2. put the symbol in account
 */
public class CreateOrderTransaction implements Transaction{
    /* the action we should execute */
    private final List<Command> actions;

    private Session session;
    private org.hibernate.Transaction tx;

    public CreateOrderTransaction(List<Command> actions){
        this.actions = actions;
        this.session = null;
        this.tx      = null;
    }


    @Override
    public void execute() {
        SessionFactory sessionFactory = Database.getSessionFactory();

        try{
            this.session = sessionFactory.openSession();
            this.tx      = this.session.beginTransaction();

            //step1. we check about the account number
            checkIfCanCreateAccount(actions);
            //step2. we check about the sym
            checkIfCanPutSym(actions);
            //step3. if all ok, we do it
            for(Command command : this.actions){
                if(command instanceof CreateAccount){
                    CreateAccount ca = (CreateAccount) command;
                    this.session.save(ca.account);
                }else{
                    PutSymbol ps = (PutSymbol) command;

                    CriteriaBuilder   builder       = sessionFactory.getCriteriaBuilder();
                    CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
                    Root<Account> root = criteria.from(Account.class);
                    criteria.select(root).where(builder.equal(root.get("account_num"), ps.account.getAccountNum()),
                                                builder.equal(root.get("sym"), ps.symbol));
                    List<Account> result = this.session.createQuery(criteria).getResultList();
                    if(result.size() == 0){

                    }else{

                    }
                }
            }

            tx.commit();
        }catch (Exception e){
            e.printStackTrace();
            if(tx != null) {
                tx.rollback();
            }
        }finally {
            if(session != null) {
                session.close();
            }
        }

    }

    /**
     * Check if the action to create account has not been done before
     * @param actions
     */
    private void checkIfCanCreateAccount(List<Command> actions) throws IllegalArgumentException{
        int len = actions.size();
        List<String> list = new ArrayList<>();

        for(int i = 0; i < len; i++){
            Command command = actions.get(i);
            if(command instanceof CreateAccount){
                CreateAccount ca = (CreateAccount) command;
                list.add(ca.account.getAccountNum());
            }
        }

        String sql = concatenateSQL(list);

        List result = session.createQuery(sql).getResultList();
        System.out.println(result);
        //we have query something
        if(result.size() != 0){
            throw new IllegalArgumentException("bad request, account has exist");
        }
    }

    /**
     * check if it is legal to put the symbol here
     * @param actions
     */
    private void checkIfCanPutSym(List<Command> actions) {
        int len = actions.size();
        List<String> list = new ArrayList<>();
        for(int i = 0; i < len; i++){
            Command command = actions.get(i);
            if(command instanceof PutSymbol){
                PutSymbol ps = (PutSymbol) command;
                list.add(ps.account.getAccountNum());
            }
        }

        String sql = concatenateSQL(list);

        List result = session.createQuery(sql).getResultList();

        //todo: check it we can put the symbol in this account
    }

    /**
     * Command method, use list to concatenate the symbol
     *
     * @param list the list to concatenate the account_num
     * @return sql query
     */
    private String concatenateSQL(List<String> list){
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM ACCOUNT WHERE ");
        for(int j = 0; j < list.size(); j++){
            if(j == 0)
                sql.append(" account_num = ").append(list.get(j));
            else
                sql.append(" OR account_num = ").append(list.get(j));
        }

        return sql.toString();
    }

}
