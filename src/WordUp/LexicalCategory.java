package WordUp;

import java.util.List;

public class LexicalCategory {
    private String category;
    private List<PossibleDefinition> senses;

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

    public String getCategory() { return category; }

}