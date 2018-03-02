package WordUp;

public class LexicalCategory {
    private String category;
    private PossibleDefinition sense;

    LexicalCategory(String category, PossibleDefinition sense) {
        this.category = category;
        this.sense = sense;
    }

    public PossibleDefinition getSense() {
        return sense;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: " + category + "\n");
        sb.append(sense.toString() + "\n");
        return sb.toString();
    }
}