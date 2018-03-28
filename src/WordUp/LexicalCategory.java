package WordUp;

import java.util.List;


/*
To remove the 'null' definition from the output (caused by the empty definition stored in sense)
simply pass in the subsenses rather than the sense itself.

ie.  This class is being constructed with a passed in PossibleDefinition, which is only being used
as a collector of subsenses (stored in a List of PossibleDefinitions).  Change the constructor,
and subsequent calls to it, to just pass in and store the subsenses of the previously passed in PD.

 */
public class LexicalCategory {
    private String category;
    private List<PossibleDefinition> senses;
    //private PossibleDefinition sense;

    LexicalCategory(String category, List<PossibleDefinition> senses) {
        this.category = category;
        this.senses = senses;
    }

    public List<PossibleDefinition> getSenses() {
        return senses;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: " + category + "\n");
        for (PossibleDefinition sense : senses) {
            sb.append(sense.toString() + "\n");
        }
        return sb.toString();
    }
}