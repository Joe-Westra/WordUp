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

//TODO: Change the way that examples in definitions are stored.  An array would be more appropriate.
//TODO: DefinitionInformation class should hold 'word' and 'baseword' variables.
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
     */
    private DefinitionInformation retrieveDefinition(DefinitionInformation def, JsonObject rootJSONObject) {
        def.setEtymologies(fetchEtymologies(rootJSONObject));
        //TODO:get phonetic spelling
        def.setLexicalCategories(fetchDefinitions(rootJSONObject));
        return def;
    }

    private List<LexicalCategory> fetchDefinitions(JsonObject rootJSONObject) {
        List<LexicalCategory> senses;
        JsonObject lexicalEntries = traverseAPI(rootJSONObject, "results");
        //This is an array of Json objects that contain multiple entries.
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
     * extracts a definition and example from a JsonObject if the member elements exist, or usese 'null' values otherwise.
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
            examples.append("'" + je.getAsJsonObject().get("text").getAsString() + "'\n");
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

    public String getBaseWord() {
        return this.definition.getRootWord();
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
            //   if (!def.getSubsenses().isEmpty()) {  //this might be redundant... does hasnext throw an exception if the list is empty?
            Iterator<PossibleDefinition> subsense = def.getSubsenses().iterator();
            while (subsense.hasNext()) {
                pd.add(subsense.next());
            }
            //    }
        }
        return pd;
    }


    public String getWord() {
        return this.definition.getQueriedWord();
    }
}

class OxfordAPIInfo {
    final String BASE_URL = "https://od-api.oxforddictionaries.com/api/v1";
    final String APP_ID = "3f6b9965";
    final String APP_KEY = "54a6ca12f6ef562c890038e2d051fc5c";
}


class LexicalCategory {
    private String category;
    private PossibleDefinition sense;

    LexicalCategory(String category, PossibleDefinition sense) {
        this.category = category;
        this.sense = sense;
    }

    public PossibleDefinition getSense() {
        return sense;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Category: " + category + "\n");
        sb.append(sense.toString() + "\n");
        return sb.toString();
    }
}

class PossibleDefinition {
    private String definition;
    private String example;
    private List<PossibleDefinition> subsenses;

    public PossibleDefinition() {
        this(null, null);
    }

    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
        subsenses = new ArrayList<>();
    }

    public List<PossibleDefinition> getSubsenses() {
        return subsenses;
    }

    public void addSubSense(PossibleDefinition definition) {
        subsenses.add(definition);
    }

    public void addSubSenses(List<PossibleDefinition> subsenses) {
        for (PossibleDefinition pd :
                subsenses) {
            this.addSubSense(pd);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Definition: " + this.definition + "\n");
        if (example != null)
            sb.append("\u0009" + example);
        if (!this.subsenses.isEmpty())
            for (PossibleDefinition pd :
                    subsenses) {
                sb.append(pd.toString());
            }
        sb.append("");//newline
        return sb.toString();
    }

}

class DefinitionInformation {
    private String etymologies;
    private String queriedWord;
    private String rootWord;
    private String phoneticSpelling;
    private List<LexicalCategory> lexicalCategories;


    public String getQueriedWord() {
        return queriedWord;
    }

    public String getRootWord() {
        return rootWord;
    }

    public void setRootWord(String rootWord) {
        this.rootWord = rootWord;
    }

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


    public DefinitionInformation(String queriedWord) {
        this.queriedWord = queriedWord;
        lexicalCategories = new ArrayList<>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Queried word: " + this.queriedWord + "\n");
        sb.append("Root word: " + this.rootWord + "\n");
        if (phoneticSpelling != null)
            sb.append(phoneticSpelling + "\n");
        if (etymologies != null && ! etymologies.equals(""))
            sb.append("Origin: " + etymologies + "\n");
        for (LexicalCategory lc :
                lexicalCategories) {
            sb.append(lc.toString());
        }
        return sb.toString();
    }
}