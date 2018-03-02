package WordUp;

import java.util.ArrayList;
import java.util.List;

public class DefinitionInformation {
    private String etymologies;
    private String queriedWord;
    private String rootWord;
    private String phoneticSpelling;
    private List<LexicalCategory> lexicalCategories;


    public String getQueriedWord() {
        return queriedWord;
    }

    public String getRootWord() {
        return rootWord;
    }

    public void setRootWord(String rootWord) {
        this.rootWord = rootWord;
    }

    public String getEtymologies() {
        return etymologies;
    }

    public void setEtymologies(String etymologies) {
        this.etymologies = etymologies;
    }

    public List<LexicalCategory> getLexicalCategories() {
        return lexicalCategories;
    }

    public void setLexicalCategories(List<LexicalCategory> lexicalCategories) {
        this.lexicalCategories = lexicalCategories;
    }

    public DefinitionInformation(String queriedWord) {
        this.queriedWord = queriedWord;
        lexicalCategories = new ArrayList<>();
    }

    public void setPhoneticSpelling(String phoneticSpelling) {
        this.phoneticSpelling = phoneticSpelling;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queried word: " + this.queriedWord + "\n");
        sb.append("Root word: " + this.rootWord + "\n");
        if (phoneticSpelling != null)
            sb.append("Phonetic Spelling: " + phoneticSpelling + "\n");
        if (etymologies != null && !etymologies.equals(""))
            sb.append("Origin: " + etymologies + "\n");
        for (LexicalCategory lc :
                lexicalCategories) {
            sb.append(lc.toString());
        }
        return sb.toString();
    }
}