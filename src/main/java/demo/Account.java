package demo;

import javax.persistence.Basic;

/* account model */
@javax.persistence.Entity
public class Account {

    @javax.persistence.Id
    private String account_id;

    @Basic
    private int balance;

    public Account(){}

    public Account(int balance, String accountNum){
        this.balance    = balance;
        this.account_id = accountNum;
    }

    public String getAccountNum() {
        return account_id;
    }

    public void setAccountNum(String accountNum) {
        this.account_id = accountNum;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }
}
