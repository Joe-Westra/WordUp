package WordUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
                //Error of some sort
                System.out.println("error....\n" +
                        "Response code:" + responseCode);
            }
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

    private DefinitionInformation retrieveDefinition(JsonObject rootJSONObject) {
        DefinitionInformation def = new DefinitionInformation();
        def.etymologies = fetchEtymologies(rootJSONObject);
        def.lexicalCategories = fetchDefinitions(rootJSONObject);
        System.out.println("ety: " + def.etymologies);
        System.out.println("definis: " + def.lexicalCategories);
        return def;
    }

    private List<LexicalCategories> fetchDefinitions(JsonObject rootJSONObject) {
        List<LexicalCategories> senses = new ArrayList<>();
        String[] path = {"results", "lexicalEntries",/* GET LEXICALCATEGORIES FROM THIS POINT*/ "entries", "senses"};
        JsonObject j = traverseAPI(rootJSONObject, path);
        String definition = getStringFromElement(j, "definitions");
        System.out.println("definition is: " + definition);
        //fetch the root definition
        String example = "";
        if (j.has("examples")){
            example = getStringFromElement(j,"examples");
        }
        LexicalCategories sense = new LexicalCategories(definition,example);

        //senses may have subsenses with definitions and optional example sentences.
        if (j.has("subsenses")) {
            JsonArray ja = j.get("subsenses").getAsJsonArray();
            Iterator<JsonElement> ji = ja.iterator();
            String def = "";
            String subExample = null;
            while (ji.hasNext()) {
                JsonObject jo = ji.next().getAsJsonObject();
                def = getStringFromElement(jo, "definitions");
                if(jo.has("examples")){
                    subExample = getStringFromElement(jo, "examples");
                }
            }
            System.out.println("def, example" + def + " " + subExample);//scaffolding
            sense.addDefinition(def, subExample);
        }
        senses.add(sense);

        return senses;
    }

    private JsonObject traverseAPI(JsonObject rootJSONObject, String... path) {
        JsonObject j = rootJSONObject;
        for (int i = 0; i < path.length; i++) {
            j = j.get(path[i]).getAsJsonArray().get(0).getAsJsonObject();
        }
        return j;
    }

    private String getStringFromElement(JsonObject jo, String element){
        return jo.get(element).getAsJsonArray().get(0).toString();
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

//w.setDefinition(w.determineDefinition(w.getBaseWord()));
class OxfordAPIInfo {
    final String baseURL = "https://od-api.oxforddictionaries.com/api/v1";
    final String appID = "3f6b9965";
    final String appKey = "54a6ca12f6ef562c890038e2d051fc5c";
}


class LexicalCategories {
    private String category;
    PossibleDefinition sense;


    LexicalCategories(String sense, String example) {
        this.sense = new PossibleDefinition(sense,example);
    }

    LexicalCategories(String category, String sense, String example) {
        this.category = category;
        this.sense = new PossibleDefinition(sense,example);
    }

    public void addDefinition(String definition, String example) {
        sense.addSubSense(definition, example);
    }


    public void setCategory(String category) { this.category = category; }
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
    }

    public void addSubSense(String definition, String example) {
        subsenses = new ArrayList<>();
        subsenses.add(new PossibleDefinition(definition,example));
    }
}

class DefinitionInformation {
    String etymologies;
    String phoneticSpelling;
    List<LexicalCategories> lexicalCategories;

    public DefinitionInformation() {
        lexicalCategories = new ArrayList<>();
    }
}