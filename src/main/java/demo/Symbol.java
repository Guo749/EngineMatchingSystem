package demo;

import javax.persistence.*;
import java.util.Set;

@javax.persistence.Entity
public class Symbol {

    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int id;

    /* the name of the symbol */
    @Basic
    public String name;

    /* corresponding account id */
    @JoinColumn(name= "account_id", referencedColumnName = "account_id", nullable = false)
    public String account_id;

    /* how many shares in this account */
    @javax.persistence.Basic
    public double share;

    public Symbol(){}


    public Symbol(String name, String account_id, double share) {
        this.name = name;
        this.account_id = account_id;
        this.share = share;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }
}
