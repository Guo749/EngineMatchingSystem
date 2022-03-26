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

            /* step1. we check about the account number */
            List<Account> toAddAccount = new ArrayList<>();
            Set<String> existAccounts = checkIfCanCreateAccount(actions, toAddAccount);

            /* step2. we check about the sym */
            Map<String, Map<String, Double>> putWorkLoad = checkIfCanPutSym(actions, existAccounts);

            /* step3. if all ok, we do it */
            for(Account account : toAddAccount){
                //create the account
                this.session.save(account);
            }

            for(String account_id : putWorkLoad.keySet()){
                for(String symbolName : putWorkLoad.get(account_id).keySet()) {
                    //update share in given symbol
                    CriteriaBuilder builder = this.session.getCriteriaBuilder();
                    CriteriaQuery<Symbol> criteria = builder.createQuery(Symbol.class);
                    Root<Symbol> root = criteria.from(Symbol.class);
                    criteria.select(root).where(
                        builder.equal(root.get("account_id"), account_id),
                        builder.equal(root.get("name"), symbolName));

                    List<Symbol> results = this.session.createQuery(criteria).getResultList();
                    double share = 0.0;
                    if(results.size() == 1){
                        Symbol oldSymbol = results.get(0);
                        share = oldSymbol.share + putWorkLoad.get(account_id).get(symbolName);
                        oldSymbol.setShare(share);

                        this.session.update(oldSymbol);
                    }else{
                        share += putWorkLoad.get(account_id).get(symbolName);
                        Symbol newSymbol = new Symbol(symbolName, account_id, share);
                        this.session.save(newSymbol);
                    }
                }
            }

            tx.commit();
        }
        catch (Exception e) {

            e.printStackTrace();
            this.tx.rollback();

        }finally {
            if(session != null) {
                session.close();
            }
        }

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
