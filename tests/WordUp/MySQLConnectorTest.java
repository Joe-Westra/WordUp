package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
        //create dummy DefinitionInformation object to add to DB
        DefinitionInformation totalDef = createDummyDIobject();

        //add dummy DI to database
        //Shouldn't throw an error...
        try {
            //connector.dropAllTables()
            // This is throwing an error saying "ROOT_WORDS" table doesn't exist, even though
            // the SQL says "drop IF EXISTS".  This is really only necessary for testing purposes, however.

            connector.addDefinition(totalDef);
        } catch (SQLException e) {
            SQLfreakout(e);
        }
    }


    @Test
    void canCheckDBforEntries(){
        String rootWord = "alacrity";
            assertTrue(connector.DBContains(rootWord));
    }

    @Test
    void wordsThatArentInDBCantBeFound(){
        String rootWord = "trump";
        assertFalse(connector.DBContains(rootWord));
    }



    @Test
    void entriesCanBeFetchedFromDB(){
        DefinitionInformation di = connector.fetchDefinition("alacrity");

        System.out.println(di.toString());
        assertEquals("from alacer 'brisk'", di.getEtymologies());
        assertEquals("elakriti",di.getPhoneticSpelling());
        assertEquals("alacrity", di.getQueriedWord());
        assertEquals("alacrity", di.getRootWord());

        List<LexicalCategory> lc = di.getLexicalCategories();
        assertEquals(1, lc.size());
        assertEquals("noun", lc.get(0).getCategory());

        List<PossibleDefinition> senses = lc.get(0).getSenses();
        assertEquals(1, senses.size());
        assertEquals("Brisk and cheerful readiness.", senses.get(0).getDefinition());

        assertEquals(1, senses.get(0).getExamples().size());
        assertEquals("she accepted the invitation with alacrity", senses.get(0).getExamples().get(0));
    }


    public void SQLfreakout(SQLException e){
        System.out.println("error code: " + e.getErrorCode());
        System.out.println("sql stat: " + e.getSQLState());
        e.printStackTrace();
        fail(e.getMessage());
    }

    private static DefinitionInformation createDummyDIobject() {
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
        return totalDef;
    }

}
