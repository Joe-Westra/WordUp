package WordUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.invoke.WrongMethodTypeException;
import java.net.URL;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import javax.net.ssl.HttpsURLConnection;


public class WordUp {
    private String word;
    private OxfordAPIInfo api;
    private Gson gson = new Gson();
    String r;

    public WordUp(String word) {
        this.word = word;
        api = new OxfordAPIInfo();
        String type = "inflections";
        HttpsURLConnection connection = getConnection(type,word);
        JsonObject JSONResponse = getURLResponse(connection);
        String baseWord = getField(JSONResponse, "inflectionOf");
        r = baseWord;
    }

    private HttpsURLConnection getConnection(String type, String word) {
        try {
            URL url = new URL(api.baseURL + "/" + type + "/en/" + word);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("app_id", api.appID);
            connection.setRequestProperty("app_key", api.appKey);
            return connection;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public String getField(JsonObject jo, String field) {
        Gson gson = new Gson();
        System.out.println(jo.toString());
        //results.inflectionOf
//        JsonObject jo = je.getAsJsonObject();
        return jo.toString();
    }

    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(reader);
            return je.getAsJsonObject();
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public String getWord() { return word; }




    public static void main(String[] args) {
        WordUp w = new WordUp("testing");

        System.out.println(w.r);
    }
}



//return "https://od-api.oxforddictionaries.com:443/api/v1/inflections/" + language + "/" + word_id;
class OxfordAPIInfo {
    final String baseURL = "https://od-api.oxforddictionaries.com/api/v1";
    final String appID = "3f6b9965";
    final String appKey = "54a6ca12f6ef562c890038e2d051fc5c";
}