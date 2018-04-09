package WordUp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.*;
import java.util.Properties;

public class MySQLConnector {
    final static String user = "java";
    final static String password = "JavaPa$$";
    final static String database = "wordup";


    public MySQLConnector(){
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }
    }

    public Connection getConnection(){
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("useSSL", "false");
        properties.setProperty("autoReconnect", "true");
        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:mysql://localhost/" + database + "?", properties);
            System.out.println("connection established to " + database + " database");
        } catch (SQLException ex) {
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        } finally {
            return conn;
        }
    }

    public boolean createTables(Connection connection){
        try {
            Statement stmt = connection.createStatement();
            String createSenseSchema = "create table if not EXISTS SENSE(" +
                    " id INT not null UNIQUE PRIMARY KEY AUTO_INCREMENT," +
                    " definition VARCHAR(250) not null," +
                    " example VARCHAR(250)," +
                    " subsenses INT )";
            stmt.execute(createSenseSchema);

            String createSubsensesSchema = "create table if not EXISTS SUBSENSE(" +
                    "  originalDef INT not null," +
                    "  subsense INT," +
                    "  FOREIGN KEY (originalDef) references SENSE(id)," +
                    "  FOREIGN KEY (subsense) references SENSE(id)," +
                    "  PRIMARY KEY (originalDef,subsense) )";
            stmt.execute(createSubsensesSchema);

            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }

        /*
            "create table if not EXISTS SENSE(\n" +
                "  id INT not null UNIQUE PRIMARY KEY AUTO_INCREMENT,\n" +
                "  definition VARCHAR(250) not null,\n" +
                "  example VARCHAR(250),\n" +
                "  subsenses INT\n" +
                ");\n" +
                "create table if not EXISTS SUBSENSE(\n" +
                "  originalDef INT not null UNIQUE,\n" +
                "  subsense INT,\n" +
                "\n" +
                "  FOREIGN KEY (originalDef, subsense) references SENSE(id,id),\n" +
                "  PRIMARY KEY (originalDef,subsense)\n" +
                ");"
         */
    }
}


/*
public static void viewTable(Connection con, String dbName)
    throws SQLException {

    Statement stmt = null;
    String query = "select COF_NAME, SUP_ID, PRICE, " +
                   "SALES, TOTAL " +
                   "from " + dbName + ".COFFEES";
    try {
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String coffeeName = rs.getString("COF_NAME");
            int supplierID = rs.getInt("SUP_ID");
            float price = rs.getFloat("PRICE");
            int sales = rs.getInt("SALES");
            int total = rs.getInt("TOTAL");
            System.out.println(coffeeName + "\t" + supplierID +
                               "\t" + price + "\t" + sales +
                               "\t" + total);
        }
    } catch (SQLException e ) {
        JDBCTutorialUtilities.printSQLException(e);
    } finally {
        if (stmt != null) { stmt.close(); }
    }
}

 */


/*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

Connection conn = null;
...
try {
    conn =
       DriverManager.getConnection("jdbc:mysql://localhost/test?" +
                                   "user=minty&password=greatsqldb");

    // Do something with the Connection

   ...
} catch (SQLException ex) {
    // handle any errors
    System.out.println("SQLException: " + ex.getMessage());
    System.out.println("SQLState: " + ex.getSQLState());
    System.out.println("VendorError: " + ex.getErrorCode());
}
 */