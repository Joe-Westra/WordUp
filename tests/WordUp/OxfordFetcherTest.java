package WordUp;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OxfordFetcherTest {
    static WordUp w;

    @BeforeAll
    public static void setup() {
        w = new WordUp("testing");
    }


    @Test
    void getsResponseFromInflectionAPI() {
        assertEquals(200, w.getInflectionResponseCode());
    }

    @Test
    void fakeWordsGetAppropriateResponseCode() {
        assertThrows(NullPointerException.class, () -> new WordUp("bunk_word"));
    }

    @Test
    void baseWordProvidesAValidDictionaryQuery() {
        assertEquals(200, w.getDefinitionResponseCode());
    }

    /*    @Test
    void fourOfourErrorsPromptForANewSpelling(){ //or automatically offer spelling corrections

    }*/
}