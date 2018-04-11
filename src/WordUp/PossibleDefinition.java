package WordUp;

import java.util.ArrayList;
import java.util.List;

public class PossibleDefinition {

    private String definition;
    private List<String> examples;
    private List<PossibleDefinition> subsenses;


    public PossibleDefinition(String definition, List<String> examples) {
        this.definition = definition;
        this.examples = examples;
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
        if (! examples.isEmpty())
            for (String example : examples)
                sb.append("\t'" + example + "'\n");

        if (!this.subsenses.isEmpty())
            for (PossibleDefinition pd : subsenses)
                sb.append(pd.toString());

        return sb.toString();
    }

    public String getDefinition() { return definition; }

    public List<String> getExamples() { return examples; }

    public List<PossibleDefinition> getSubsenses() { return subsenses; }

}