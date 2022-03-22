package demo;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    @Test
    public void testInsertAccount(){
        Account account = new Account(2,200);
        Database.createAccount(account);
    }

    @Test
    public void testInsertOrder() {
        Database.addOrder(new Order("BTC", 12, 3223));

        List<Order> orders = Database.getAllOrders();
        assertEquals(1, orders.size());
        assertEquals("BTC", orders.get(0).getSym());
        assertEquals(12, orders.get(0).getAmount(), 0.001);
        assertEquals(3223, orders.get(0).getPriceLimit(), 0.001);

        Database.addOrder(new Order("ABC", 166, 43));

        orders = Database.getAllOrders();
        assertEquals(2, orders.size());
    }
}
