package demo;

import jdk.jfr.Description;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    @Test
    public void testInit() throws SQLException, ClassNotFoundException {
        Database.init();
    }

    @Test
    public void testInsertOrder() throws InterruptedException, SQLException, ClassNotFoundException {
        Database.init();
        Account account = new Account(3242, "124");
        Order order1 = new Order(account, "BTC", 12, 3223);
        Order order2 = new Order(account, "USD", 233, 6879);
        Order order3 = new Order(account, "CNY", 3456, 978);
        Order order4 = new Order(account, "USD", 2467, 63);
        order1.addChildOrder(order2);
        order2.addChildOrder(order3);
        order1.addChildOrder(order4);
        Database.addOrder(order1);
        Database.addOrder(order2);
        Database.addOrder(order3);
        Database.addOrder(order4);

        List<Order> orders = Database.getAllOrders();
        assertEquals(4, orders.size());
        for (Order order : orders) {
            System.out.println(order.getId() + " " + order.getSym() + " " + order.getAmount() + " " + order.getPriceLimit());
            if (order.getParentOrder() != null) {
                System.out.println("parent: "+ order.getParentOrder().getId());
            }
            System.out.println("num of child orders: " + order.getChildOrders().size());
        }
    }

    @Test
    @Description("test if DB can be initialized properly")
    public void testInitDB() throws SQLException, ClassNotFoundException {
        Database.init();
        Database.init();
    }
}
