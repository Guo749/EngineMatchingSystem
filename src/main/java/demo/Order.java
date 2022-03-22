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
    private float amount;

    @javax.persistence.Basic
    private float priceLimit;

    @javax.persistence.Basic
    private OrderStatus status;

    // Seconds since the epoch
    @javax.persistence.Basic
    private long time;

    public Order() {}

    public Order(String sym, int amount, int priceLimit) {
        this.sym = sym;
        this.amount = amount;
        this.priceLimit = priceLimit;
        this.status = OrderStatus.OPEN;
    }

    public String getSym() {
        return sym;
    }

    public float getAmount() {
        return amount;
    }

    public float getPriceLimit() {
        return priceLimit;
    }
}
