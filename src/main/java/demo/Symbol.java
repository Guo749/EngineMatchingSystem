package demo;

import javax.persistence.Basic;

@javax.persistence.Entity
public class Symbol {
    /* the name of the symbol */
    @javax.persistence.Id
    public String name;

    /* corresponding account id */
    @javax.persistence.Basic
    public String accountId;

    /* how many shares in this account */
    @javax.persistence.Basic
    public double share;

    public Symbol(){}

    public Symbol(String name, String accountId, double share) {
        this.name = name;
        this.accountId = accountId;
        this.share = share;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public double getShare() {
        return share;
    }

    public void setShare(double share) {
        this.share = share;
    }
}
