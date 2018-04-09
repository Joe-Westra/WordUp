package WordUp;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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

    public static boolean createTables(Connection connection){
        try {
            Statement stmt = connection.createStatement();

            String createRootWordTable =
                    " create table if not EXISTS ROOT_WORDS(" +
                    " root_word VARCHAR(20) UNIQUE PRIMARY KEY," +
                    " etymology VARCHAR(250)," +
                    " phonetic VARCHAR(25) NOT NULL)";
            stmt.execute(createRootWordTable);

            String createQueriedWordsTable =
                            " create table if not EXISTS QUERIED_WORDS(" +
                            " quer_word VARCHAR(20) UNIQUE PRIMARY KEY," +
                            " root_word VARCHAR(20) NOT NULL," +
                            " FOREIGN KEY (root_word) references ROOT_WORDS(root_word))";
            stmt.execute(createQueriedWordsTable);



            String createLexiCatTable =
                            " create table if not EXISTS LEXI_CAT(" +
                            " cat_id int AUTO_INCREMENT UNIQUE PRIMARY KEY," + // use INDEX descriptor?
                            " root_word VARCHAR(20) NOT NULL," +
                            " lexi_cat varchar(10) not null," +
                            " FOREIGN KEY (root_word) references ROOT_WORDS(root_word))";
            stmt.execute(createLexiCatTable);

            String createDefinitionTable =
                            " create table if not EXISTS DEFINITION(" +
                            " def_id int auto_increment UNIQUE PRIMARY KEY," +
                            " cat_id int not null," +
                            " parent_id int," +
                            " definition VARCHAR(250) NOT NULL," +
                            " FOREIGN KEY (cat_id) references LEXI_CAT(cat_id)," +
                            " FOREIGN KEY (parent_id) references DEFINITION(def_id))";

            stmt.execute(createDefinitionTable);

            String createExampleTable =
                            " create table if not EXISTS EXAMPLE(" +
                            " def_id int," +
                            " example VARCHAR(250) NOT NULL PRIMARY KEY UNIQUE," +
                            " FOREIGN KEY (def_id) references DEFINITION(def_id))";

            stmt.execute(createExampleTable);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
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