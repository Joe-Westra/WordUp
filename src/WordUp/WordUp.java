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
//TODO: Implement spell correction
//TODO: If API connection attempt fails, try again x amount of
//TODO: OPTIONAL: Change the way that examples in definitions are stored.  An array would be more appropriate.
public class WordUp {
    private static OxfordAPIInfo api;
    private DefinitionInformation definition;
    private HttpsURLConnection inflectionConnection;
    private HttpsURLConnection definitionConnection;


    public WordUp(String word) {
        definition = new DefinitionInformation(word);
        api = new OxfordAPIInfo();
        this.deriveBaseWord();
        this.deriveDefinition();
    }



    public static void main(String[] args) {
        String word = "testing";
        if(args.length > 0){
            //TODO: check for phrases or multiple words
            word = args[0];
        }
        WordUp w = new WordUp(word);
        System.out.println(w.getDefinition().toString());
    }



    public DefinitionInformation determineDefinition(DefinitionInformation def) {
        int responseCode;
        DefinitionInformation definition = null;
        definitionConnection = getConnection("entries",def.getRootWord());
        try {
            responseCode = definitionConnection.getResponseCode();
            if (responseCode == 200) {
                JsonObject JSONResponse = getURLResponse(definitionConnection);
                definition = retrieveDefinition(def, JSONResponse);
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
                        "Response code: " + responseCode);
            }
        } catch (UnknownHostException exception) {
            System.out.println("Please check your internet connection and try again.");
            shriekAndDie(inflectionConnection, exception);
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



    /*
    THIS IS SHIT CODE
     It is not modular at all.  It's just changing the def passed in.TODO: FIX IT!
     */
    private DefinitionInformation retrieveDefinition(DefinitionInformation def, JsonObject rootJSONObject) {
        def.setEtymologies(fetchEtymologies(rootJSONObject));
        def.setPhoneticSpelling(fetchPhoneticSpelling(rootJSONObject));
        def.setLexicalCategories(fetchDefinitions(rootJSONObject));
        return def;
    }



    private List<LexicalCategory> fetchDefinitions(JsonObject rootJSONObject) {
        List<LexicalCategory> senses;
        JsonObject lexicalEntries = traverseAPI(rootJSONObject, "results");
        //This is an array of Json objects that contains multiple entries.
        //Each entry is for a different lexical category
        senses = getEachLexicalCategory(lexicalEntries);
        return senses;
    }



    private List<LexicalCategory> getEachLexicalCategory(JsonObject lexicalEntries) {
        List<LexicalCategory> lc = new ArrayList<>();
        Iterator<JsonElement> it = lexicalEntries.get("lexicalEntries").getAsJsonArray().iterator();
        while (it.hasNext()) {
            JsonObject jo = it.next().getAsJsonObject();
            String category = jo.get("lexicalCategory").getAsString();
            PossibleDefinition pd = getEachEntry(jo);
            lc.add(new LexicalCategory(category, pd));
        }
        return lc;
    }



    private PossibleDefinition getEachEntry(JsonObject jo) {
        PossibleDefinition sense = new PossibleDefinition();
        Iterator<JsonElement> it = jo.get("entries").getAsJsonArray().iterator();
        while (it.hasNext()) {
            JsonObject j = it.next().getAsJsonObject();
            sense.addSubSenses(getEachSense(j));
        }
        return sense;
    }



    private List<PossibleDefinition> getEachSense(JsonObject jo) {
        List<PossibleDefinition> senses = new ArrayList<>();
        Iterator<JsonElement> senseList = jo.get("senses").getAsJsonArray().iterator();
        while (senseList.hasNext()) {
            JsonObject joSense = senseList.next().getAsJsonObject();
            PossibleDefinition thisSense = digestDefinitionIfPresent(joSense);
            thisSense.addSubSenses(getEachSubsense(joSense));
            senses.add(thisSense);
        }
        return senses;
    }


    public List<PossibleDefinition> getEachSubsense(JsonObject jo) {
        List<PossibleDefinition> subsenses = new ArrayList<>();
        if (jo.has("subsenses")) {
            Iterator<JsonElement> k = jo.get("subsenses").getAsJsonArray().iterator();
            while (k.hasNext()) {
                JsonObject kk = k.next().getAsJsonObject();
                subsenses.add(digestDefinitionIfPresent(kk));
            }
        }
        return subsenses;
    }



    /**
     * extracts a definition and example from a JsonObject if the member elements exist, or uses 'null' values otherwise.
     * @param definitionJO
     * @return a PossibleDefinition with the extracted values
     */
    private PossibleDefinition digestDefinitionIfPresent(JsonObject definitionJO) {
        String d = null;
        String e = null;
        if (definitionJO.has("definitions"))
            d = definitionJO.get("definitions").getAsJsonArray().get(0).getAsString(); //always seems to be a lone definition.
        if (definitionJO.has("examples"))
            e = getExamples(definitionJO);
        return new PossibleDefinition(d, e);
    }



    /**
     * Extracts all 'examples' from a JsonObject, concatenating them into one string.
     * @param jo
     * @return a formatted concatenation of all examples
     */
    private String getExamples(JsonObject jo) {
        StringBuilder examples = new StringBuilder();
        JsonArray ja = jo.get("examples").getAsJsonArray();
        for (JsonElement je : ja) {
            examples.append("\t'" + je.getAsJsonObject().get("text").getAsString() + "'\n");
        }
        return examples.toString();
    }



    /**
     * This method walks through an unspecified amount of levels of the API's JSON response.
     * @param rootJSONObject
     * @param path
     * @return
     */
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



    private String fetchPhoneticSpelling(JsonObject rootJSONObject) {
        String phoneSpell = "";
        String[] path = {"results", "lexicalEntries", "pronunciations"};
        JsonObject j = traverseAPI(rootJSONObject, path);
        if (j.has("phoneticSpelling")) {
            phoneSpell = j.get("phoneticSpelling").getAsString();
        }
        return phoneSpell;
    }



    /**
     * Attempts to connect to the Oxford API with the supplied app_id and app_key.
     * This method is used for determining both the definition and the lemma of the queried word.
     * @param type
     * @param word
     * @return
     */
    private HttpsURLConnection getConnection(String type, String word) {
        try {
            URL url = new URL(api.BASE_URL + "/" + type + "/en/" + word);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("app_id", api.APP_ID);
            connection.setRequestProperty("app_key", api.APP_KEY);
            return connection;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
            return null;
        }
    }



    public String retrieveBaseWord(JsonObject jo) {
        String[] path = {"results", "lexicalEntries", "inflectionOf"};
        String base = traverseAPI(jo, path).get("text").toString();
        return base.substring(1, base.length() - 1); //remove quotations
    }



    private JsonObject getURLResponse(HttpsURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            JsonParser jp = new JsonParser();
            JsonElement je = jp.parse(reader);
            return je.getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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


    public String getWord() {
        return this.definition.getQueriedWord();
    }


    public String getBaseWord() { return this.definition.getRootWord(); }


    public void deriveDefinition() {
        this.definition = determineDefinition(this.definition);
    }


    public void deriveBaseWord() {
        this.definition.setRootWord(determineBaseWord(this.definition.getQueriedWord()));
    }


    public List<PossibleDefinition> getSubsenses() {

        Iterator<LexicalCategory> lc = this.definition.getLexicalCategories().iterator();
        List<PossibleDefinition> pd = new ArrayList<>();
        while (lc.hasNext()) {
            PossibleDefinition def = lc.next().getSense();
            pd.add(def);//add the root definition (sense)
            Iterator<PossibleDefinition> subsense = def.getSubsenses().iterator();
            while (subsense.hasNext()) {
                pd.add(subsense.next());
            }
        }
        return pd;
    }

}

class OxfordAPIInfo {
    final String BASE_URL = "https://od-api.oxforddictionaries.com/api/v1";
    final String APP_ID = "3f6b9965";
    final String APP_KEY = "54a6ca12f6ef562c890038e2d051fc5c";
}






