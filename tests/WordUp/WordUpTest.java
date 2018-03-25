package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordUpTest {

    static WordUp w;

    @BeforeAll
    public static void setup() {
        w = new WordUp("testing");
    }

    @Test
    void getWord() {
        System.out.println("word: " + w.getWord());
        assertEquals("testing", w.getWord());
    }

    //TODO: refactor to have an error thrown instead.
    @Test
    void getsResponseFromInflectionAPI() {
        assertEquals(200, w.getInflectionResponseCode());
    }

    //TODO: refactor to have an error thrown instead.
    @Test
    void fakeWordsGetAppropriateResponseCode() {
        //Refactoring now makes this throw an exception prior to the response code being evaluated.
        WordUp wu = new WordUp("asdf");
        assertEquals(404, wu.getInflectionResponseCode());
    }

    @Test
    void properRootWordsAreIdentified() {
        WordUp wy = new WordUp("dumping");
        //wy.deriveBaseWord();
        assertEquals("dump", wy.getBaseWord());
    }

    //TODO: refactor to have an error thrown instead.
    @Test
    void baseWordProvidesAValidDictionaryQuery() {
        assertEquals(200, w.getDefinitionResponseCode());
    }

    @Test
    void definitionIsNotNull() {
        assertNotNull(w.getDefinition());
    }

    @Test
    void definitionOfWordWithoutListedOriginIsEmptyString() {
        //"testing" has no etymology
        assertTrue(w.getDefinition().getEtymologies() == "");
    }

    @Test
    void definitionOfWordWithListedOriginIsString() {
        String testingWord = "dumping";
        String expectedEtymology =
                "Middle English: perhaps from Old Norse; related to Danish dumpe" +
                        " and Norwegian dumpa ‘fall suddenly’ (the original sense" +
                        " in English); in later use partly imitative; compare with thump";

        WordUp wu = new WordUp(testingWord);
        assertTrue(wu.getDefinition().getEtymologies().equals(expectedEtymology));
    }

    @Test
    void definitionsContainAtLeastOneEntry() {
        System.out.println("size of definitions list: " + w.getDefinition().getLexicalCategories().size());
        assertFalse(w.getDefinition().getLexicalCategories().isEmpty());
    }


    @Test
    void definitionsHaveAtLeastOneLexicalCategory() {
        assertFalse(w.getDefinition().getLexicalCategories().isEmpty());
    }

    @Test
    void someWordsHaveMultipleSubsensenses() {
        WordUp wu = new WordUp("dumping");
        assertFalse(wu.getSubsenses().isEmpty());
    }

    @Test
    void definitionsHaveAReadableStringRepresentation(){
        WordUp wu = new WordUp("dumping");
        assertEquals("Queried word: dumping\n" +
                        "Root word: dump\n" +
                        "Phonetic Spelling: dʌmp\n" +
                        "Origin: Middle English: perhaps from Old Norse; related to Danish dumpe and Norwegian dumpa ‘fall suddenly’ (the original sense in English); in later use partly imitative; compare with thump\n" +
                        "Category: Noun\n" +
                        "Definition: a site for depositing rubbish.\n" +
                        "Definition: a heap of rubbish left at a dump.\n" +
                        "Definition: a place where a particular kind of waste, especially dangerous waste, is left\n" +
                        "\t'a nuclear waste dump'\n" +
                        "Definition: a place where weapons and other military equipment is stored\n" +
                        "\t'an ammunitions dump'\n" +
                        "Definition: an unpleasant or dreary place\n" +
                        "\t'why are you living in a dump like this?'\n" +
                        "Definition: an act of copying stored data to a different location, performed typically as a protection against loss.\n" +
                        "Definition: a printout or list of the contents of a computer's memory, occurring typically after a system failure.\n" +
                        "Definition: an act of defecation.\n" +
                        "\n" +
                        "Category: Verb\n" +
                        "Definition: deposit or dispose of (rubbish, waste, or unwanted material), typically in a careless or hurried way\n" +
                        "\t'trucks dumped 1,900 tons of refuse here'\n" +
                        "Definition: abandon (something) hurriedly in order to make an escape\n" +
                        "\t'the couple dumped the car and fled'\n" +
                        "Definition: put (something) down heavily or carelessly\n" +
                        "\t'she dumped her knapsack on the floor'\n" +
                        "Definition: abandon or desert (someone)\n" +
                        "\t'you'll get tired of me and dump me'\n" +
                        "Definition: send (goods unsaleable in the home market) to a foreign market for sale at a low price\n" +
                        "\t'these countries have been dumping cheap fertilizers on the UK market'\n" +
                        "Definition: sell off (assets) rapidly\n" +
                        "\t'investors dumped shares in scores of other consumer-goods firms'\n" +
                        "Definition: copy (stored data) to a different location, especially so as to protect against loss.\n" +
                        "Definition: print out or list the contents of (a store), especially after a system failure.\n" +
                        "\n",
                wu.getDefinition().toString());
    }

/*    @Test
    void fourOfourErrorsPromptForANewSpelling(){ //or automatically offer spelling corrections

    }*/
}