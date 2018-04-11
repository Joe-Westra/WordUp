package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;


public class MySQLConnectorTest {
    static MySQLConnector connector;
    @BeforeAll
    static void start(){
        connector = new MySQLConnector();
    }


    @Test
    void canInitiateConnection(){
     assertNotNull(connector.acquireConnection());
    }

    @Test
    void createsAllTablesIfNotPresent(){
        assertTrue(MySQLConnector.createTables(connector.getConnection()));
    }

    @Test
    void canAddPossibleDefinitionIntoDatabase(){
        //create dummy PD
        String etymologies = "from alacer 'brisk'";
        String phoneticSpelling = "elakriti";
        String quer = "alacrity";
        String root = "alacrity";
        String cat = "noun";
        String definition = "Brisk and cheerful readiness.";
        String example = "she accepted the invitation with alacrity";
        PossibleDefinition def = new PossibleDefinition(definition, Arrays.asList(example));
        LexicalCategory category = new LexicalCategory(cat, Arrays.asList(def));
        DefinitionInformation totalDef = new DefinitionInformation(quer);
        totalDef.setRootWord(root);
        totalDef.setLexicalCategories(Arrays.asList(category));
        totalDef.setEtymologies(etymologies);
        totalDef.setPhoneticSpelling(phoneticSpelling);

        //add PD to database
        //Shouldn't throw an error...
        try {
            //connector.dropAllTables();

            connector.addDefinition(totalDef);
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
            System.out.println(e.getSQLState());
            System.out.println(e.getMessage());
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
