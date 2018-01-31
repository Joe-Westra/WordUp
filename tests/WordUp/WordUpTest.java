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


}