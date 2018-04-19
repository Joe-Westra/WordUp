package WordUp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//TODO: Implement spell correction
//TODO: If API connection attempt fails, try again x amount of
//TODO: Implement a database!
public class WordUp {
    private String queriedWord;
    private String rootWord;
    private DefinitionInformation definition;  //contains all the information of a retrieved definition.
    private MySQLConnector mySQLConnector;
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
        w.setRootWord(ojp.determineBaseWord(w.queriedWord));
        if( w.mySQLConnector.DBContains(w.rootWord)){

        }
        w.setDefinition(ojp.determineDefinition(w.queriedWord, w.rootWord));

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






