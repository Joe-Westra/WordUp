package WordUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
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
        DefinitionInformation definition = null;
        definitionConnection = getConnection("entries", baseWord);
        try {
            responseCode = definitionConnection.getResponseCode();
            if (responseCode == 200) {
                JsonObject JSONResponse = getURLResponse(definitionConnection);
                System.out.println("Json Response for definition of '" + baseWord + "': " + JSONResponse.toString());//scaffolding
                definition = retrieveDefinition(JSONResponse);
            }
        } catch (IOException e) {
            shriekAndDie(definitionConnection, e);
        }

        return definition;
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
                System.out.println("error....\n" +
                        "Response code:" + responseCode);
            }
        } catch(UnknownHostException exception) {
            System.out.println("Please check your internet connection and try again.");
            shriekAndDie(inflectionConnection,exception);
        }catch (IOException e) {
            shriekAndDie(inflectionConnection, e);
        }
        return baseWord;
    }

    private void shriekAndDie(HttpsURLConnection connection, IOException e) {
        e.printStackTrace();
        connection.disconnect();
        System.exit(-1);
    }

    private DefinitionInformation retrieveDefinition(JsonObject rootJSONObject) {
        DefinitionInformation def = new DefinitionInformation();
        def.setEtymologies(fetchEtymologies(rootJSONObject));
        def.setLexicalCategories(fetchDefinitions(rootJSONObject));
        System.out.println("ety: " + def.getEtymologies());
        System.out.println("definis: " + def.getLexicalCategories());
        return def;
    }

    private List<LexicalCategory> fetchDefinitions(JsonObject rootJSONObject) {
        List<LexicalCategory> senses;
        String path = "results";
        JsonObject lexicalEntries = traverseAPI(rootJSONObject, path);
        //This is an array of Json objects that contain multiple entries.
        //Each entry is for a different lexical category
        senses = getEachLexicalCategory(lexicalEntries);
        return senses;
    }

    private List<LexicalCategory> getEachLexicalCategory(JsonObject lexicalEntries) {
        List<LexicalCategory> lc = new ArrayList<>();
        Iterator<JsonElement> it = lexicalEntries.get("lexicalEntries").getAsJsonArray().iterator();
        while (it.hasNext()){
            JsonObject jo = it.next().getAsJsonObject();
            String category = jo.get("lexicalCategory").getAsString();
            PossibleDefinition pd = getEachDefinition(jo);
            lc.add(new LexicalCategory(category,pd));
        }
        return lc;
    }

    private PossibleDefinition getEachDefinition(JsonObject jo) {
        PossibleDefinition sense= new PossibleDefinition(null,null);
        Iterator<JsonElement> it = jo.get("entries").getAsJsonArray().iterator();
        while (it.hasNext()){
            JsonObject j = it.next().getAsJsonObject();
            Iterator<JsonElement> jj = j.get("senses").getAsJsonArray().iterator();
            while (jj.hasNext()){
                JsonObject jjj = jj.next().getAsJsonObject();

                String def = jjj.get("definitions").getAsString();
                String example = "";
                if (jjj.has("examples"))
                    example = jjj.get("examples").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                sense = new PossibleDefinition(def,example);
                if (jjj.has("subsenses")){
                    Iterator<JsonElement> k = jjj.get("subsenses").getAsJsonArray().iterator();
                    while (k.hasNext()) {
                        JsonObject kk = k.next().getAsJsonObject();
                        String d = kk.get("definitions").getAsString();
                        String e = "";
                        if (kk.has("examples")) {
                            e = kk.get("examples").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                        }
                        sense.addSubSense(new PossibleDefinition(d,e));
                    }

                }
            }
        }

        return sense;
    }

    private JsonObject traverseAPI(JsonObject rootJSONObject, String... path) {
        JsonObject j = rootJSONObject;
        for (int i = 0; i < path.length; i++) {
            j = j.get(path[i]).getAsJsonArray().get(0).getAsJsonObject();
        }
        return j;
    }


    /**
     * Returns etymology (origin) of a word if one is supplied in the response.
     * Absent etymologies return an empty String.
     *
     * @param rootJSONObject
     * @return
     */
    private String fetchEtymologies(JsonObject rootJSONObject) {
        String ety = "";
        String[] path = {"results", "lexicalEntries", "entries"};
        JsonObject j = traverseAPI(rootJSONObject, path);
        if (j.has("etymologies")) {
            ety = j.get("etymologies").getAsJsonArray().get(0).toString();
            ety = ety.substring(1, ety.length() - 1);
        }
        return ety;
    }

    private HttpsURLConnection getConnection(String type, String word) {
        try {
            URL url = new URL(api.baseURL + "/" + type + "/en/" + word);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("app_id", api.appID);
            connection.setRequestProperty("app_key", api.appKey);
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }

    public String retrieveBaseWord(JsonObject jo) {
        System.out.println(jo.toString());
        String[] path = {"results","lexicalEntries","inflectionOf"};
        String base = traverseAPI(jo,path).get("text").toString();
        System.out.println(base);
        return base.substring(1, base.length() - 1); //remove quotations
    }

    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            System.out.println("response is: " + connection.getResponseCode());//scaffolding
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(reader);
            return je.getAsJsonObject();
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

    public DefinitionInformation getDefinition() {
        return definition;
    }

    public void setDefinition(DefinitionInformation definition) {
        this.definition = definition;
    }

    public String getBaseWord() {
        return baseWord;
    }

    public void setBaseWord(String baseWord) {
        this.baseWord = baseWord;
    }

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


class LexicalCategory {
    private String category;
    private PossibleDefinition sense;

    LexicalCategory(String category, PossibleDefinition sense) {
        this.category = category;
        this.sense =sense;
    }
}

class PossibleDefinition {
    private String definition;
    private String example;
    private List<PossibleDefinition> subsenses;

    public String getDefinition() {
        return definition;
    }

    public String getExample() {
        return example;
    }

    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
        subsenses = new ArrayList<>();

    }

    public void addSubSense(String definition, String example) {
        subsenses.add(new PossibleDefinition(definition,example));
    }
    public void addSubSense(PossibleDefinition definition) {
        subsenses.add(definition);
    }
}

class DefinitionInformation {
    private String etymologies;
    private String phoneticSpelling;

    public String getEtymologies() {
        return etymologies;
    }

    public void setEtymologies(String etymologies) {
        this.etymologies = etymologies;
    }

    public List<LexicalCategory> getLexicalCategories() {
        return lexicalCategories;
    }

    public void setLexicalCategories(List<LexicalCategory> lexicalCategories) {
        this.lexicalCategories = lexicalCategories;
    }

    private List<LexicalCategory> lexicalCategories;

    public DefinitionInformation() {
        lexicalCategories = new ArrayList<>();
    }
}