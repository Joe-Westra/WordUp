package WordUp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;


public class WordUp {
    private String word;
    private OxfordAPIInfo api;

    public WordUp(String word) {
        this.word = word;
        api = new OxfordAPIInfo();
        String type = "inflections";
        HttpsURLConnection connection = getConnection(type, word.toLowerCase());
        JsonObject JSONResponse = getURLResponse(connection);
        String baseWord;
        if (JSONResponse == null) {
            System.out.println("inappropriate word choice");
            connection.disconnect();
            System.exit(-1);
        }

        baseWord = getField(JSONResponse);
        //baseword of lookup achieved;
        type = "entries";
        connection = getConnection(type, baseWord);


    }

    private HttpsURLConnection getConnection(String type, String word) {
        try {
            URL url = new URL(api.baseURL + "/" + type + "/en/" + word);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("app_id", api.appID);
            connection.setRequestProperty("app_key", api.appKey);
            return connection;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Inappropriate word");
            System.exit(-1);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getField(JsonObject jo) {
        Gson gson = new Gson();
        System.out.println(jo.toString());
        JsonElement hoo = jo.get("results");
        JsonElement hoot = hoo.getAsJsonArray().get(0);
        JsonElement hooot = hoot.getAsJsonObject().get("lexicalEntries");
        JsonElement hoooot = hooot.getAsJsonArray().get(0).getAsJsonObject().get("inflectionOf");
        System.out.println(hoooot.getAsJsonArray().get(0).getAsJsonObject().get("text").toString());
        return hoooot.getAsJsonArray().get(0).getAsJsonObject().get("text").toString();
    }

    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(reader);
            return je.getAsJsonObject();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("Inappropriate word");
            System.exit(-1);
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getWord() {
        return word;
    }

    public static void main(String[] args) {
        WordUp w = new WordUp("sdfg");

    }
}


class OxfordAPIInfo {
    final String baseURL = "https://od-api.oxforddictionaries.com/api/v1";
    final String appID = "3f6b9965";
    final String appKey = "54a6ca12f6ef562c890038e2d051fc5c";
}