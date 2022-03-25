package demo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DatabaseTest {
    @Test
    public void testInsertAccount(){
        Account account = new Account(2,200);
        Database.createAccount(account);
    }

    @Test
    public void testInsertOrder() throws InterruptedException {
        Order order1 = new Order("BTC", 12, 3223);
        Order order2 = new Order("USD", 233, 6879);
        Order order3 = new Order("CNY", 3456, 978);
        Order order4 = new Order("USD", 2467, 63);
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

        List<Order> USDOrders = Database.getOpenOrdersWithSym("USD");
        for (Order order : USDOrders) {
            System.out.println(order.getId() + " " + order.getSym() + " " + order.getAmount() + " " +
                    order.getPriceLimit() + " " + order.getTime());
        }

        USDOrders.get(0).setAmount(87443);
        List<Order> ordersToUpdate = new ArrayList<>();
        ordersToUpdate.add(USDOrders.get(0));
        Database.updateOrders(ordersToUpdate);
        USDOrders = Database.getOpenOrdersWithSym("USD");
        for (Order order : USDOrders) {
            System.out.println(order.getId() + " " + order.getSym() + " " + order.getAmount() + " " +
                    order.getPriceLimit() + " " + order.getTime());
        }
    }
}
