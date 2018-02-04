package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordUpTest {

    static WordUp w;

    @BeforeAll
    public static void setup() {
        w = new WordUp("testing");
        w.setBaseWord(w.determineBaseWord(w.getWord()));
        w.setDefinition(w.determineDefinition(w.getBaseWord()));
    }

    @Test
    void getWord() {
        System.out.println("word: " + w.getWord());
        assertEquals("testing", w.getWord());
    }

    @Test
    void getsResponseFromInflectionAPI() {
        assertEquals(200, w.getInflectionResponseCode());
    }

    @Test
    void fakeWordsGetAppropriateResponseCode() {
        WordUp wu = new WordUp("asdf");
        wu.determineBaseWord(wu.getWord());
        assertEquals(404, wu.getInflectionResponseCode());
    }

    @Test
    void properRootWordsAreIdentified() {
        WordUp wy = new WordUp("dumping");
        wy.setBaseWord(wy.determineBaseWord(wy.getWord()));
        assertEquals("dump", wy.getBaseWord());
    }

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
        wu.setBaseWord(wu.determineBaseWord(wu.getWord()));
        wu.setDefinition(wu.determineDefinition(wu.getBaseWord()));

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
/*    @Test
    void someWordsHaveMultipleSubsensenses() {
        WordUp wu = new WordUp("dumping");
        wu.setBaseWord(wu.determineBaseWord(wu.getWord()));
        wu.setDefinition(wu.determineDefinition(wu.getBaseWord()));


    }*/
}