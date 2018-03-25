package WordUp;

/**
 * This class is used to fetch a JSON response from the Oxford Dictionary API.
 * Given a supplied query term and an API endpoint (lemmatron, spell checker, definer, etc.)
 * It returns the raw JSON response as a Gson.JsonObject.
 */

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;



public class OxfordFetcher {
    final static String BASE_URL = "https://od-api.oxforddictionaries.com/api/v1";
    final static String APP_ID = "3f6b9965";
    final static String APP_KEY = "54a6ca12f6ef562c890038e2d051fc5c";
    private HttpsURLConnection connection;

    public HttpsURLConnection getConnection() {
        return connection;
    }

    public JsonObject getRootJsonObject() {
        return rootJsonObject;
    }

    private JsonObject rootJsonObject;


    public OxfordFetcher(String type, String word) throws IOException {
        connection = getConnection(type, word);
        rootJsonObject = getRootJsonObject(connection);
    }

    /**
     * Parses a gson.JsonObject from a given HttpsUrlConnection response
     * @param connection
     * @return
     */
    private JsonObject getRootJsonObject(HttpsURLConnection connection) throws IOException{
        if (connection.getResponseCode() == 202){
            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = (jp.parse(br));
            return je.getAsJsonObject();
        }else{
            throw new IOException("Non-202 response: " + connection.getResponseCode());
        }
    }


    /**
     * Attempts to connect to the Oxford API with the supplied app_id and app_key.
     * This method is used for determining both the definition and the lemma of the queried word.
     * @param type
     * @param word
     * @return
     */
    private static HttpsURLConnection getConnection(String type, String word) throws IOException {
        try {
            URL url = new URL(BASE_URL + "/" + type + "/en/" + word);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("app_id", APP_ID);
            connection.setRequestProperty("app_key", APP_KEY);
            return connection;
        } catch (IOException e) {
            //TODO: try again... spell check, dns error?  bad gateway?
            e.printStackTrace();
            System.exit(-1);
            throw new IOException();
        }
    }
}