package demo;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DatabaseTest {
    @Test
    public void testInsertAccount(){
        Account account = new Account(2,200);
        Database.createAccount(account);
    }

    @Test
    public void testInsertOrder() {
        Database.createOrder(new Order("BTC", 12, 3223));
        Database.createOrder(new Order("ABC", 166, 43));
        List<Order> orders = Database.getAllOrders();
        for (Order order : orders) {
            System.out.println(order.getSym() + order.getAmount() + order.getLimit());
        }
    }
}
