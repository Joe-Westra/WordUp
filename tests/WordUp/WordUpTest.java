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
    void getsResponseFromAPI(){
        assertEquals(200, w.getResponseCode());
    }

    @Test
    void fakeWordsGetAppropriateResponseCode() {
        WordUp wu = new WordUp("asdf");
        assertEquals(404, wu.getResponseCode());
    }

    @Test
    void properRootWordsAreIdentified(){
        WordUp wy = new WordUp("dumping");
        assertEquals("dump", wy.getBaseWord());
    }

    @Test
    void baseWordProvidesAValidDictionaryQuery(){
        assertEquals(200, w.getResponseCode());

    }

    @Test
    void definitionClassIsCorrect(){
        assertTrue(w.getDefinition().getClass() == DefinitionInformation.class);
        }
}