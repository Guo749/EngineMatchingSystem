package demo;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.sql.SQLException;
import java.util.List;

public class OrderTransactionTest {
    @Test
    public void testExecuteOrder() throws SQLException, ClassNotFoundException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document results = builder.newDocument();
        Database.init();
        Account account = new Account(16643, "5533");
        Order buyOrder = new Order(account, "BTC", 30, 90);
        Database.addOrder(buyOrder);

        OrderTransaction transaction1 = new OrderTransaction(account, "BTC", 5, 100);
        transaction1.execute(results);
        printAllOrders();

        System.out.println("-----------------------");
        OrderTransaction transaction2 = new OrderTransaction(account, "BTC", -60, 80);
        transaction2.execute(results);
        printAllOrders();

        System.out.println("-----------------------");
        OrderTransaction transaction3 = new OrderTransaction(account, "BTC", 40, 120);
        transaction3.execute(results);
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
