package demo;

import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

public class DatabaseTest {
    @Test
    public void testInsertAccount(){
        Account account = new Account(2,200);
        Database.createAccount(account);
    }
}
