package WordUp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO: Implement spell correction
//TODO: If API connection attempt fails, try again x amount of
public class WordUp {
    private String queriedWord;
    private String rootWord;
    private DefinitionInformation definition;  //contains all the information of a retrieved definition.
    private static MySQLConnector mySQLConnector;
    private static OxfordJsonParser ojp;


    public WordUp(String word) {
        mySQLConnector = new MySQLConnector();
        ojp = new OxfordJsonParser();
        queriedWord = word;
    }



    public static void main(String[] args) {
        String word = "testing";
        if(args.length > 0){
            //TODO: check for phrases or multiple words
            word = args[0];
        }
        WordUp w = new WordUp(word);

        //create DB tables if they don't exist
        mySQLConnector.createTables(mySQLConnector.getConnection());

        w.setRootWord(ojp.fetchRootWordFromAPI(w.queriedWord));

        //check DB for entry of rootword
        if( mySQLConnector.DBContains(w.rootWord)){
            //fetch the definition
            w.definition = mySQLConnector.fetchDefinition(w.rootWord);

        } else {
            w.setDefinition(ojp.determineDefinition(w.queriedWord, w.rootWord));
            try {
                mySQLConnector.addDefinition(w.definition);
            } catch (SQLException e){
                System.out.printf("ERROR: cannot add %s's definition to database\n", w.rootWord);
                e.printStackTrace();
                try {
                    mySQLConnector.getConnection().close();
                } catch (SQLException ex) {
                    System.out.println("ERROR: cannot close connection");
                } finally {
                    System.exit(-1);
                }
            }
        }

        System.out.println(w.getDefinition().toString());
    }


    public void setRootWord(String rootWord) { this.rootWord = rootWord; }

    public void setDefinition(DefinitionInformation definition) { this.definition = definition; }

    public DefinitionInformation getDefinition() { return definition; }

    public String getWord() { return this.definition.getQueriedWord(); }

    public String getRootWord() { return this.definition.getRootWord(); }

    public List<PossibleDefinition> getSubsenses() {

        Iterator<LexicalCategory> lc = this.definition.getLexicalCategories().iterator();
        List<PossibleDefinition> pd = new ArrayList<>();
        while (lc.hasNext()) {
            List<PossibleDefinition> def = lc.next().getSenses();
            Iterator<PossibleDefinition> subsense = def.iterator();
            while (subsense.hasNext()) {
                pd.add(subsense.next());
            }
        }
        return pd;
    }

}






