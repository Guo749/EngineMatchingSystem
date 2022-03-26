package demo;

import javax.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@javax.persistence.Entity
@javax.persistence.Table(name="orders")
public class Order {
    @javax.persistence.Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "account_id", referencedColumnName = "account_id")
    private Account account_id;

    @ManyToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "parent_order_id")
    private Order parentOrder;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "parentOrder")
    private Set<Order> childOrders = new HashSet<Order>();

    @Basic
    @Column(nullable = false)
    private String sym;

    @Basic
    private double amount;

    @Basic
    private double priceLimit;

    @Basic
    @Column(nullable = false)
    private OrderStatus status;

    // Seconds since the epoch
    @Basic
    private long time;

    public Order() {}

    public Order(String sym, double amount, double priceLimit) {
        this.sym = sym;
        this.amount = amount;
        this.priceLimit = priceLimit;
        this.status = OrderStatus.OPEN;
        this.time = Instant.now().getEpochSecond();
    }

    public void addChildOrder(Order childOrder) {
        this.childOrders.add(childOrder);
        childOrder.parentOrder = this;
    }

    public int getId() {
        return id;
    }

    public void setParentOrder(Order parentOrder) {
        this.parentOrder = parentOrder;
    }

    public Order getParentOrder() {
        return parentOrder;
    }

    public Set<Order> getChildOrders() {
        return childOrders;
    }

    public String getSym() {
        return sym;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setPriceLimit(double priceLimit) {
        this.priceLimit = priceLimit;
    }

    public double getPriceLimit() {
        return priceLimit;
    }

    public long getTime() {
        return time;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {
        return status;
    }
}
