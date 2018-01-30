package WordUp;

import static org.junit.jupiter.api.Assertions.*;

class WordUpTest {

    @org.junit.jupiter.api.Test
    void getWord() {
        WordUp w = new WordUp("testing");
        assertEquals("testing", w.getWord());
    }

    @org.junit.jupiter.api.Test
    void getLemmatronURL() {
        WordUp w = new WordUp("testing");
        System.out.println(w.r);
//        assertEquals("sad", w.getLemmatronURL());
    }


}