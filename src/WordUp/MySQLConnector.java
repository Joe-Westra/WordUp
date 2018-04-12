package WordUp;


import java.sql.*;
import java.util.Properties;

public class MySQLConnector {
    private String user = "java";
    private String password = "JavaPa$$";
    private String database = "wordup";
    private Connection connection;


    public MySQLConnector(){
        this("java","JavaPa$$","wordup");
    }

    public MySQLConnector(String user, String password, String database){
        this.user = user;
        this.password = password;
        this.database = database;
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception e) {
            System.out.printf("ERROR: cannot access database '%s' with username '%s' and password '%s'.",database,user,password);
            e.printStackTrace();
            System.exit(-1);
        }
        connection = acquireConnection();
    }

    /**
     * Connects to the WordUp database, currently using hardcoded access authorization fields.
     * @return java.sql.Connection object for the database
     */
    public Connection acquireConnection(){
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

    /**
     * Creates the tables necessary for the wordup database.
     * @param connection
     * @return true if tables created without throwing error
     */
    public static boolean createTables(Connection connection){
        try {
            connection.setAutoCommit(false);
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
                            //" PRIMARY KEY (root_word,lexi_cat)";
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
            connection.commit();
            connection.setAutoCommit(true);
            return true;
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        }
    }

    public void addDefinition(DefinitionInformation totalDef) throws SQLException {
        //search for queried word in QUERIEDWORDtable


        String rootWord = totalDef.getRootWord();
        String queriedWord = totalDef.getQueriedWord();

        String addRW = String.format("insert into ROOT_WORDS values ('%s','%s','%s')" ,
                rootWord,
                totalDef.getEtymologies().replaceAll("'","''"),
                totalDef.getPhoneticSpelling());

        String addQW = String.format("insert into QUERIED_WORDS values ('%s','%s')" ,
                queriedWord,
                rootWord);

        Statement stmt = connection.createStatement();
        stmt.execute(addRW);
        stmt.execute(addQW);

        for (LexicalCategory category : totalDef.getLexicalCategories() ) {
            String cat = category.getCategory();
            String addLC = String.format("insert into LEXI_CAT (root_word, lexi_cat) " +
                    "values ('%s','%s')" , rootWord, cat);
            stmt.execute(addLC);
            String getID = "SELECT LAST_INSERT_ID()";
            System.out.println(getID);
            ResultSet rs = stmt.executeQuery(getID);
            rs.first();
            int cat_ID = rs.getInt("LAST_INSERT_ID()");
            System.out.println(cat_ID);
            for (PossibleDefinition pd :
                    category.getSenses()) {
                insertDefinitionIntoDB(connection, cat_ID, -1, pd);
            }

        }
    }


    private void insertDefinitionIntoDB(Connection connection, int cat_ID, int parent_ID, PossibleDefinition pd) throws SQLException{
        String addDef = "insert into DEFINITION (cat_id, parent_id, definition) values (?,?,?)";
        PreparedStatement pstmt = connection.prepareStatement(addDef);
        pstmt.setInt(1,cat_ID);
        pstmt.setInt(2,parent_ID);
        pstmt.setString(3,pd.getDefinition());
        if (parent_ID == -1){
            pstmt.setNull(2, Types.INTEGER);
        }
        pstmt.execute();
        String getID = "SELECT LAST_INSERT_ID()";
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(getID);
        rs.first();
        System.out.println(rs.toString());
        int parent = rs.getInt("LAST_INSERT_ID()");
        for (String example :
                pd.getExamples()) {
            String addEX = String.format("insert into EXAMPLE values ('%d', '%s')" , parent, example);
            stmt.execute(addEX);
        }
        for (PossibleDefinition subdef :
                pd.getSubsenses()) {
            insertDefinitionIntoDB(connection,cat_ID,parent,subdef);
        }
    }


    public void dropAllTables() throws SQLException {
        String drop = "drop table if exists EXAMPLE, DEFINITION, LEXI_CAT, QUERIED_WORDS, ROOT_WORDS";
        Statement stmt = connection.createStatement();
        stmt.execute(drop);
    }

    public Connection getConnection() { return connection; }

    public boolean DBContains(String rootWord){
        try {
            String quer = "select * from ROOT_WORDS where root_word = ?";
            PreparedStatement prepstat = connection.prepareStatement(quer);
            prepstat.setString(1, rootWord);
            ResultSet rs = prepstat.executeQuery();
            if (rs.next())
                return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public DefinitionInformation fetchDefinition(String rootWord) {
        String quer = "select quer_word,ROOT_WORDS.root_word,etymology,phonetic from QUERIED_WORDS,ROOT_WORDS where QUERIED_WORDS.root_word = ? and  ROOT_WORDS.root_word = ?";
        DefinitionInformation definition = null;
        try {
            PreparedStatement ps = connection.prepareStatement(quer);
            ps.setString(1, rootWord);
            ps.setString(2, rootWord);
            ResultSet rs = ps.executeQuery();

            //parse the results
            //TODO: THIS!
            return null;
        } catch (SQLException e) {
            return null;
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
       DriverManager.acquireConnection("jdbc:mysql://localhost/test?" +
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