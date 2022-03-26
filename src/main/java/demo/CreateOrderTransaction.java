package demo;

import javafx.util.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.beans.PropertyEditorSupport;
import java.util.*;

/**
 * The class is to do the create, two parts
 *  1. creat the account
 *  2. put the symbol in account
 */
public class CreateOrderTransaction implements Transaction{
    /* the action we should execute */
    private final List<Command> actions;

    /* sql session */
    private Session session;

    /* transaction handler */
    private org.hibernate.Transaction tx;

    public CreateOrderTransaction(List<Command> actions){
        this.actions   = actions;
        this.session   = null;
        this.tx        = null;

    }


    @Override
    public void execute() {
        SessionFactory sessionFactory = Database.getSessionFactory();

        try{
            this.session = sessionFactory.openSession();
            this.tx      = this.session.beginTransaction();

            for(Command command : actions){
                if(command instanceof CreateAccount){
                    CreateAccount ca = (CreateAccount) command;

                    //check if we have created it before
                    if(accountIsExist(ca.account.getAccountNum())) {
                        command.successfulExecute = false;
                    }else{
                        this.session.save(ca.account);
                        command.successfulExecute = true;
                    }
                }else{
                    PutSymbol ps = (PutSymbol) command;

                    if(accountIsExist(ps.account.getAccountNum())){
                        CriteriaBuilder builder = this.session.getCriteriaBuilder();
                        CriteriaQuery<Symbol> criteria = builder.createQuery(Symbol.class);
                        Root<Symbol> root = criteria.from(Symbol.class);
                        criteria.select(root).where(
                            builder.equal(root.get("account_id"), ps.account.getAccountNum()),
                            builder.equal(root.get("name"), ps.symbol));

                        List<Symbol> res = this.session.createQuery(criteria).getResultList();
                        if(res.size() != 0){
                            assert(res.size() == 1);
                            Symbol oldSymbol = res.get(0);
                            Symbol newSymbol = new Symbol(oldSymbol.name, oldSymbol.account_id, oldSymbol.share + ps.share);
                            this.session.save(newSymbol);
                        }else{
                            Symbol newSymbol = new Symbol(ps.symbol, ps.account.getAccountNum(), ps.share);
                            this.session.save(newSymbol);
                        }

                        command.successfulExecute = true;
                    }else{
                        command.successfulExecute = false;
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            this.tx.rollback();
        }finally {
            if(session != null) {
                session.close();
            }
        }

    }

    /**
     *  do one query if the account has exist
     *
     */
    private boolean accountIsExist(String accountNum){
        CriteriaBuilder builder = this.session.getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account> root = criteria.from(Account.class);
        criteria.select(root).where(
            builder.equal(root.get("account_id"), accountNum));

        List<Account> results = this.session.createQuery(criteria).getResultList();
        return results.size() != 0;
    }

    /**
     * Check if the action to create account has not been done before
     * @param actions commands from XML <create></create>
     * @return all accounts that exist in the database
     */
    private Set<String> checkIfCanCreateAccount(List<Command> actions, List<Account> accountToAdd) throws IllegalArgumentException{
        int len = actions.size();

        Set<String> toAddAccount = new HashSet<>();
        for(int i = 0; i < len; i++){
            Command command = actions.get(i);
            if(command instanceof CreateAccount){
                CreateAccount ca = (CreateAccount) command;
                String account_id               = ca.account.getAccountNum();

                if(toAddAccount.contains(account_id)){
                    throw new IllegalArgumentException("Duplicate account created " + account_id);
                }
                toAddAccount.add(account_id);
                accountToAdd.add(ca.account);
            }
        }

        /* get all account number and see if duplicates */
        CriteriaBuilder builder         = this.session.getCriteriaBuilder();
        CriteriaQuery<Account> criteria = builder.createQuery(Account.class);
        Root<Account>          root     = criteria.from(Account.class);
        criteria.select(root);
        List<Account> results = this.session.createQuery(criteria).getResultList();


        for(Account ac : results){
            if(toAddAccount.contains(ac.getAccountNum())){
                throw new IllegalArgumentException("account has already exist " + ac.getAccountNum());
            }
        }


        return toAddAccount;
    }

    /**
     * check if it is legal to put the symbol here
     * The logic to check if the symbol can be put in the account is
     *  1. if the account has exist in the existAccount, that it will be fine
     *  2. if the account has occurred in the accountToAdd, it will be fine
     *
     *  other than that, it will be illegal
     * @param actions
     */
    private Map<String, Map<String, Double>> checkIfCanPutSym(List<Command> actions, Set<String> existAccount) {
        int len = actions.size();
        Map<String, Map<String, Double>> res = new HashMap<>();
        Set<String> accountToAdd = new HashSet<>();

        for(int i = 0; i < len; i++){
            Command command = actions.get(i);
            if(command instanceof PutSymbol){
                PutSymbol ps = (PutSymbol) command;
                String checkAccount = ps.account.getAccountNum();

                if(!existAccount.contains(checkAccount) && !accountToAdd.contains(checkAccount)){
                    throw new IllegalArgumentException("put symbol in an account that does not exist " + checkAccount);
                }

                double originShare = 0.0;
                res.putIfAbsent(checkAccount, new HashMap<>());
                if(res.containsKey(checkAccount) && res.get(checkAccount).containsKey(ps.symbol)){
                    originShare = res.get(checkAccount).get(ps.symbol);
                }

                originShare += ps.share;
                res.get(checkAccount).put(ps.symbol, originShare);
            }else{
                CreateAccount ca = (CreateAccount) command;
                accountToAdd.add(ca.account.getAccountNum());
            }
        }

        return res;
    }

}
