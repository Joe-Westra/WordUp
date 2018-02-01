package WordUp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.*;

import javax.net.ssl.HttpsURLConnection;


public class WordUp {
    private String word;
    private static OxfordAPIInfo api;
    private String baseWord;
    private DefinitionInformation definition;
    private HttpsURLConnection inflectionConnection;
    private HttpsURLConnection definitionConnection;


    public WordUp(String word) {
        this.word = word;
        api = new OxfordAPIInfo();
    }

    public DefinitionInformation determineDefinition(String baseWord) {
        int responseCode;
        definitionConnection = getConnection("entries", baseWord);
        try {
            responseCode = definitionConnection.getResponseCode();
            if (responseCode == 200) {
                JsonObject JSONResponse = getURLResponse(definitionConnection);
                System.out.println("Json Response for defition" + JSONResponse.toString());//scaffolding
                definition = retrieveDefintion(JSONResponse);
            }
        } catch (IOException e) {
            shriekAndDie(definitionConnection, e);
        }

        return new DefinitionInformation();
    }

    public String determineBaseWord(String word) {
        int responseCode;

        String baseWord = "";
        inflectionConnection = getConnection("inflections", word.toLowerCase());
        try {
            responseCode = inflectionConnection.getResponseCode();
            if (responseCode == 200) {
                JsonObject JSONResponse = getURLResponse(inflectionConnection);
                baseWord = retrieveBaseWord(JSONResponse);
            } else {
                //Error of some sort
                System.out.println("error....\n" +
                        "Response code:" + responseCode);
            }
        } catch (FileNotFoundException e) {
            //This doesn't seem to be in the right spot, it's not catching anything.
            System.out.println("not an acceptable word");
            inflectionConnection.disconnect();
            System.exit(-1);
        } catch (IOException e) {
            shriekAndDie(inflectionConnection, e);
        }
        return baseWord;
    }

    private void shriekAndDie(HttpsURLConnection connection, IOException e) {
        e.printStackTrace();
        connection.disconnect();
        System.exit(-1);
    }

    private DefinitionInformation retrieveDefintion(JsonObject connection) {

        return new DefinitionInformation();
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
            System.exit(-1);
            return null;
        }
    }

    public String retrieveBaseWord(JsonObject jo) {
        System.out.println(jo.toString());
        String base = jo.get("results")
                .getAsJsonArray().get(0).getAsJsonObject().get("lexicalEntries")
                .getAsJsonArray().get(0).getAsJsonObject().get("inflectionOf")
                .getAsJsonArray().get(0).getAsJsonObject().get("text").toString();
        System.out.println(base);
        return base.substring(1, base.length() - 1);
    }

    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            System.out.println("response is: " + connection.getResponseCode());
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(reader);
            return je.getAsJsonObject();

        } catch (FileNotFoundException e) {
            //This should recommend alternative spellings.
            e.printStackTrace();
            System.out.println("Inappropriate word");
            connection.disconnect();
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

    public int getInflectionResponseCode() {
        int responseCode = -1;
        try {
            responseCode = inflectionConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
    }

    public int getDefinitionResponseCode() {
        int responseCode = -1;
        try {
            responseCode = definitionConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return responseCode;
    }

    public DefinitionInformation getDefinition() { return definition; }

    public void setDefinition(DefinitionInformation definition) { this.definition = definition; }

    public String getBaseWord() { return baseWord; }

    public void setBaseWord(String baseWord) { this.baseWord = baseWord; }

    public static void main(String[] args) {
        WordUp w = new WordUp("starstruck");
        w.setBaseWord(w.determineBaseWord(w.getWord()));
        w.setDefinition(w.determineDefinition(w.getBaseWord()));
    }

}


class OxfordAPIInfo {
    final String baseURL = "https://od-api.oxforddictionaries.com/api/v1";
    final String appID = "3f6b9965";
    final String appKey = "54a6ca12f6ef562c890038e2d051fc5c";
}

class PossibleDefinition {
    String definition;
    String example;

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }

    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
    }
}

class LexicalCategory {
    String category;
    List<PossibleDefinition> definitions;

    public LexicalCategory(String category) {
        this.category = category;
        definitions = new ArrayList<PossibleDefinition>();
    }

    public void addDefinition(String definition, String example) {
        definitions.add(new PossibleDefinition(definition, example));
    }
}

class DefinitionInformation {
    String etymologies;
    String phoneticSpelling;
    List<LexicalCategory> definitions;
}