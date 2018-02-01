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
    private OxfordAPIInfo api;
    private int responseCode;
    private String baseWord;
    private DefinitionInformation definition;

    public WordUp(String word) {
        this.word = word;
        api = new OxfordAPIInfo();
        String type = "inflections";
        HttpsURLConnection connection = getConnection(type, word.toLowerCase());
        try {
            responseCode = connection.getResponseCode();
            System.out.println("Response code at line 26: " + responseCode);
            if (responseCode == 200){
                JsonObject JSONResponse = getURLResponse(connection);
                baseWord = retrieveBaseWord(JSONResponse);
            }
        }catch (FileNotFoundException e){
            //This doesn't seem to be in the right spot, it's not catching anything.
            System.out.println("not an acceptable word");
            connection.disconnect();
            System.exit(-1);
        }catch (IOException e){
            shriekAndDie(connection, e);
        }



        //baseword of lookup achieved;
        type = "entries";
        connection = getConnection(type, baseWord);
        try {
            responseCode = connection.getResponseCode();
            System.out.println("Response code at line 26: " + responseCode);
            if (responseCode == 200){
                JsonObject JSONResponse = getURLResponse(connection);
                System.out.println(JSONResponse.toString());
                definition = retrieveDefintion(JSONResponse);
            }
        } catch(IOException e){
            shriekAndDie(connection, e);
        }





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
        return base.substring(1, base.length()-1);
    }

    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            System.out.println("response is: " +connection.getResponseCode());
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

    public int getResponseCode() { return responseCode; }

    public DefinitionInformation getDefinition() { return definition; }

    public String getBaseWord() { return baseWord; }

    public static void main(String[] args) {
        WordUp w = new WordUp("starstruck");
    }

}


class OxfordAPIInfo {
    final String baseURL = "https://od-api.oxforddictionaries.com/api/v1";
    final String appID = "3f6b9965";
    final String appKey = "54a6ca12f6ef562c890038e2d051fc5c";
}

class PossibleDefinition{
    String definition;
    String example;

    public String getDefinition() { return definition; }

    public String getExample() { return example; }

    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
    }
}

class LexicalCategory{
    String category;
    List<PossibleDefinition> definitions;

    public LexicalCategory(String category) {
        this.category = category;
        definitions = new ArrayList<PossibleDefinition>();
    }

    public void addDefinition(String definition, String example){
        definitions.add(new PossibleDefinition(definition,example));
    }
}

class DefinitionInformation{
    String etymologies;
    String phoneticSpelling;
    List<LexicalCategory> definitions;
}