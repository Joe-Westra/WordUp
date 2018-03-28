package WordUp;

import java.util.ArrayList;
import java.util.List;

public class PossibleDefinition {
    private String definition;
    private String example;
    private List<PossibleDefinition> subsenses;

    public PossibleDefinition() {
        this(null, null);
    }

    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
        subsenses = new ArrayList<>();
    }

    public void addSubSense(PossibleDefinition definition) {
        subsenses.add(definition);
    }

    public void addSubSenses(List<PossibleDefinition> subsenses) {
        for (PossibleDefinition pd :
                subsenses) {
            this.addSubSense(pd);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Definition: " + this.definition + "\n");
        if (example != null)
            sb.append(example);
        if (!this.subsenses.isEmpty())
            for (PossibleDefinition pd :
                    subsenses) {
                sb.append(pd.toString());
            }
        //sb.append("");//newline
        return sb.toString();
    }
}