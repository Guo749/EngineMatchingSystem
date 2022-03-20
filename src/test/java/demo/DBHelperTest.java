package demo;

import org.junit.jupiter.api.Test;

public class DBHelperTest {
    @Test
    public void testDBConnection(){
        DBHelper dbHelper = new DBHelper();
        dbHelper.garbageCollection();
        DBHelper dbHelper1 = new DBHelper();
    }
}
