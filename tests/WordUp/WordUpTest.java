package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WordUpTest {

    static WordUp w;

    @BeforeAll
    public static void setup(){
        w = new WordUp("testing");
    }

    @Test
    void getWord() {
        System.out.println("word: " + w.getWord());
        assertEquals("testing", w.getWord());
    }

    @Test
    void getsResponseFromInflectionAPI(){
        w.determineBaseWord(w.getWord());
        assertEquals(200, w.getInflectionResponseCode());
    }

    @Test
    void fakeWordsGetAppropriateResponseCode() {
        WordUp wu = new WordUp("asdf");
        wu.determineBaseWord(wu.getWord());
        assertEquals(404, wu.getInflectionResponseCode());
    }

    @Test
    void properRootWordsAreIdentified(){
        WordUp wy = new WordUp("dumping");
        wy.setBaseWord(wy.determineBaseWord(wy.getWord()));
        assertEquals("dump", wy.getBaseWord());
    }

    @Test
    void baseWordProvidesAValidDictionaryQuery(){
        w.setDefinition(w.determineDefinition(w.getBaseWord()));
        assertEquals(200, w.getDefinitionResponseCode());
    }

    @Test
    void definitionClassIsCorrect(){
        assertTrue(w.getDefinition().getClass() == DefinitionInformation.class);
        }
}