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
    @Description("test if DB can be initialized properly")
    public void testInitDB() throws SQLException, ClassNotFoundException {
        Database.init();
        Database.init();
    }
}
