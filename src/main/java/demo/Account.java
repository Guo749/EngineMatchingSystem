package demo;

/* account model */
@javax.persistence.Entity
public class Account {

    @javax.persistence.Id
    private int id;

    @javax.persistence.Basic
    private int balance;

    public Account(){}

    public Account(int id, int balance){
        this.id      = id;
        this.balance = balance;
    }

    public int getId() {
        return id;
    }
}
