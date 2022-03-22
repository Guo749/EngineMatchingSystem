package demo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

@javax.persistence.Entity
@javax.persistence.Table(name="orders")
public class Order {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @javax.persistence.Basic
    private String sym;

    @javax.persistence.Basic
    private int amount;

    @javax.persistence.Basic
    private int limit;

    @javax.persistence.Basic
    private OrderStatus status;

    // Seconds since the epoch
    @javax.persistence.Basic
    private long time;

    public Order() {}

    public Order(String sym, int amount, int limit) {
        this.sym = sym;
        this.amount = amount;
        this.limit = limit;
    }

    public String getSym() {
        return sym;
    }

    public int getAmount() {
        return amount;
    }

    public int getLimit() {
        return limit;
    }
}
