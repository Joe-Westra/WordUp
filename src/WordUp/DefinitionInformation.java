package WordUp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DefinitionInformation {
    private String etymologies;
    private String queriedWord;
    private String rootWord;
    private String phoneticSpelling;
    private int accessCount;
    private Date lastAccess;

    private List<LexicalCategory> lexicalCategories;


    public String getQueriedWord() {
        return queriedWord;
    }


    public void setQueriedWord(String queriedWord) {
        this.queriedWord = queriedWord;
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

    public DefinitionInformation(){
        this("");
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
        int q_count = getAccessCount();
        if (q_count > 1) {
            sb.append("You have looked up this word " + q_count + " times.  ");
            String last_access = getLastAccess().toString();
            sb.append("The last time was " + last_access + ".\n");
        }
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

    public String getPhoneticSpelling() {
        return phoneticSpelling;
    }


    public void setAccessCount(int accessCount) {
        this.accessCount = accessCount;
    }

    public Date getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Date lastAccess) {
        this.lastAccess = lastAccess;
    }
    public int getAccessCount() {
        return this.accessCount;
    }
}