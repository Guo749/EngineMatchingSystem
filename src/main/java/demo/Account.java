package demo;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/* account model */
@javax.persistence.Entity
public class Account {

    @javax.persistence.Id
    private String account_id;

    @Basic
    private double balance;

    @Version
    private long version;

    public Account(){}

    public Account(double balance, String accountNum){
        this.balance    = balance;
        this.account_id = accountNum;
    }

    public String getAccountNum() {
        return account_id;
    }

    public void setAccountNum(String accountNum) {
        this.account_id = accountNum;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

}
