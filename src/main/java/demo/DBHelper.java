package demo;

import javax.swing.plaf.nimbus.State;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper {
    private Connection conn = null;

    /**
     * Constructor, used to initialize the connection with db
     */
    public DBHelper(){
        try{
            Class.forName("org.postgresql.Driver");
            this.conn
                = DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb", "postgres", "postgres");

            System.out.println("establish db successfully");
            //todo: create table

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This is used to handle command which is not read-only
     * like write / insert / modify
     *
     * @param sql the command to execute
     * @return if we execute successfully
     */
    public boolean execNotReadOnlyCommand(String sql){
        Statement stmt;

        try {
            stmt = this.conn.createStatement();
            stmt.executeUpdate(sql);
            this.conn.commit();
            this.conn.close();
            return true;
        }catch (Exception e){return false;}
    }

    /**
     * Read only command, like query
     *
     * @return the result of query
     * todo: may change the structure of return to be an object
     */
    public String execReadOnlyCommand(String sql){
            return "";
    }

    public void garbageCollection(){
        try {
            this.conn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
