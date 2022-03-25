package demo;

import org.junit.jupiter.api.Test;

import java.util.List;

public class OrderTransactionTest {
    @Test
    public void testExecuteOrder() {
        Order buyOrder = new Order("BTC", 30, 200);
        Database.addOrder(buyOrder);

        OrderTransaction transaction1 = new OrderTransaction(5345, "BTC", -5, 100);
        transaction1.execute();
        printAllOrders();

        System.out.println("-----------------------");
        OrderTransaction transaction2 = new OrderTransaction(1455, "BTC", -15, 110);
        transaction2.execute();
        printAllOrders();

        System.out.println("-----------------------");
        OrderTransaction transaction3 = new OrderTransaction(1455, "BTC", -40, 50);
        transaction3.execute();
        printAllOrders();
    }

    private void printAllOrders() {
        List<Order> orders = Database.getAllOrders();
        for (Order order : orders) {
            System.out.println(order.getId() + " " + order.getSym() + " " + order.getAmount() + " " +
                    order.getPriceLimit() + " " + order.getStatus());
            if (order.getParentOrder() != null) {
                System.out.println("parent: "+ order.getParentOrder().getId());
            }
            System.out.println("num of child orders: " + order.getChildOrders().size());
        }
    }
}
