package demo;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.time.Instant;

@javax.persistence.Entity
@javax.persistence.Table(name="orders")
public class Order {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // TODO: Perhaps need an additional transaction ID

    @javax.persistence.Basic
    private String sym;

    @javax.persistence.Basic
    private double amount;

    @javax.persistence.Basic
    private double priceLimit;

    @javax.persistence.Basic
    private OrderStatus status;

    // Seconds since the epoch
    @javax.persistence.Basic
    private long time;

    public Order() {}

    public Order(String sym, double amount, double priceLimit) {
        this.sym = sym;
        this.amount = amount;
        this.priceLimit = priceLimit;
        this.status = OrderStatus.OPEN;
        this.time = Instant.now().getEpochSecond();
    }

    public String getSym() {
        return sym;
    }

    public double getAmount() {
        return amount;
    }

    public double getPriceLimit() {
        return priceLimit;
    }

    public long getTime() {
        return time;
    }
}
