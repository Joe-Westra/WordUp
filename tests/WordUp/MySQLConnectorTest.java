package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class MySQLConnectorTest {
    static MySQLConnector connector;
    @BeforeAll
    static void start(){
        connector = new MySQLConnector();
    }


    @Test
    void canInitiateConnection(){
     assertNotNull(connector.getConnection());
    }

    @Test
    void createsAllTablesIfNotPresent(){
        assertTrue(MySQLConnector.createTables(connector.getConnection()));
    }
}
