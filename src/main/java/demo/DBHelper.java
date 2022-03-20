package demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBHelper {
    /* for each db helper, we have one specific connection */
    private Connection conn = null;

    /* what's the name of the account table ? */
    private final static String accountTable = "ACCOUNT";

    /* what's the name of symbol table? */
    private final static String symbolTable  = "SYMBOL";

    private static int counter = 0;

    /**
     * Constructor, used to initialize the connection with db
     */
    public DBHelper(){
        try{
            Class.forName("org.postgresql.Driver");
            this.conn
                = DriverManager.getConnection("jdbc:postgresql://db:5432/postgres", "postgres", "postgres");

            this.conn.setAutoCommit(false);
            System.out.println("establish db successfully");

            if(counter == 0) {
                createAccountTable(this.conn);
                createSymbolTable(this.conn);
                counter++;
                System.out.println("initialize tables OK");
            }
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

    /**
     * Close the resource
     */
    public void garbageCollection(){
        try {
            if(this.conn != null)
                this.conn.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This is used to create symbol table
     */
    private static void createSymbolTable(Connection connection) throws SQLException {
        //drop if possible
        String dropSQL = "DROP TABLE IF EXISTS " + symbolTable + ";";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(dropSQL);

        //create it
        String createSQL =
            "CREATE TABLE  " + symbolTable +
                "(ID   INT PRIMARY KEY NOT NULL," +
                "SHARE TEXT            NOT NULL);"
            ;

        stmt.executeUpdate(createSQL);
        connection.commit();
    }

    /**
     * This is used to create account table
     */
    private static void createAccountTable(Connection connection) throws SQLException {
        //drop if possible
        String dropSQL = "DROP TABLE IF EXISTS " + accountTable + ";";
        Statement stmt = connection.createStatement();
        stmt.executeUpdate(dropSQL);

        //create it
        String createSQL =
            "CREATE TABLE " + accountTable+
                "(ID     INT PRIMARY KEY NOT NULL," +
                "balance INT            NOT NULL);"
            ;
        stmt.executeUpdate(createSQL);
        connection.commit();
    }
}
