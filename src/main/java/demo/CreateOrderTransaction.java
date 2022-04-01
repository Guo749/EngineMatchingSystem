package demo;

import javafx.util.Pair;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
public class CreateOrderTransaction extends Transaction{
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
    public Element execute(Document results) {
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
                            oldSymbol.setShare(oldSymbol.share + ps.share);
                            this.session.update(oldSymbol);
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

            session.flush();
            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            this.tx.rollback();

            /* make sure all actions are not properly executed */
            if(this.actions != null) {
                int len = this.actions.size();
                for (int i = 0; i < len; i++){
                    this.actions.get(i).successfulExecute = false;
                }
            }
        }finally {
            if(session != null) {
                session.close();
            }
        }

        // TODO: Useless return to make this method comply with the interface
        return results.createElement("temp");
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


}
