package WordUp;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class OxfordFetcherTest {

    @Test
    void getJSONResponse() {
        assertThrows(IOException.class, ()-> new OxfordFetcher("bs_word", "lemmatron"));
    }
}