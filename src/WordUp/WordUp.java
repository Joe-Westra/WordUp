package WordUp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
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
            List<String> path = new LinkedList<String>();
            path.add("entries");
            path.add("senses");
            path.add("subsenses");
            PossibleDefinition pd = FetchDeffies(jo,path);//getEachDefinition(jo);
            lc.add(new LexicalCategory(category,pd));
        }
        return lc;
    }
    //trying to improve this function
    private PossibleDefinition FetchDeffies(JsonObject jo, List<String> path){
        System.out.println("firing recursive fetch at " + path.get(0));
        PossibleDefinition pd = new PossibleDefinition();
        if(path.isEmpty())
            return digestDefinitionIfPresent(jo);
        while(! path.isEmpty()) { //suspect line....
            String nextSubElement = path.remove(0);
            if(jo.has(nextSubElement)) {
                JsonElement je = jo.get(nextSubElement);
                if( je.isJsonObject() ) {
                    JsonObject inner = je.getAsJsonObject();
                }
                pd.addSubSense(FetchDeffies(inner, path));//returns a PD
                Iterator<JsonElement> it = inner.getAsJsonArray().iterator();
                while (it.hasNext()) {
                    JsonObject j = it.next().getAsJsonObject();
                    pd.addSubSense(FetchDeffies(j, path));//returns a PD
                }
            }
        }

return pd;
        }

        private PossibleDefinition digestDefinitionIfPresent(JsonObject jo){
        String d = null;
        String e = null;
        if(jo.has("definitions"))
                d = jo.get("definitions").getAsJsonArray().get(0).getAsString();
        if(jo.has("examples"))
                e = jo.get("examples").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
        return new PossibleDefinition(d,e);
        }

    private PossibleDefinition getEachDefinition(JsonObject jo) {

        List<String> path = new LinkedList<String>();
        path.add("entries");
        path.add("senses");
        path.add("subsenses");
        PossibleDefinition sense = new PossibleDefinition();
        Iterator<JsonElement> it = jo.get("entries").getAsJsonArray().iterator();
        while (it.hasNext()){
            System.out.println("hitting 'entries' sublist");
            JsonObject j = it.next().getAsJsonObject();
            Iterator<JsonElement> jj = j.get("senses").getAsJsonArray().iterator();
            while (jj.hasNext()){
                System.out.println("hitting 'senses' sublist");
                JsonObject jjj = jj.next().getAsJsonObject();

                String def = jjj.get("definitions").getAsString();
                String example = "";
                if (jjj.has("examples")) {
                    System.out.println("hitting 'examples' sublist");

                    example = jjj.get("examples").getAsJsonArray().get(0).getAsJsonObject().get("text").getAsString();
                }
                sense = new PossibleDefinition(def,example);
                if (jjj.has("subsenses")){
                    Iterator<JsonElement> k = jjj.get("subsenses").getAsJsonArray().iterator();
                    while (k.hasNext()) {
                        JsonObject kk = k.next().getAsJsonObject();
                        System.out.println(kk.toString());
                        String d = "";
                        if(kk.has("definitions")) {
                            d = kk.get("definitions").getAsJsonArray().get(0).getAsString();
                        }
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
        WordUp w = new WordUp("filling");
        w.setBaseWord(w.determineBaseWord(w.getWord()));
        w.setDefinition(w.determineDefinition(w.getBaseWord()));
        System.out.println(w.toString());
    }

    @Override
    public String toString(){
        List<PossibleDefinition> pd = getSubsenses();
        Iterator<PossibleDefinition> definitions = pd.iterator();
        StringBuilder deflist = new StringBuilder();
        while (definitions.hasNext()){
            PossibleDefinition d = definitions.next();
            deflist.append("definition: " + d.getDefinition() + "\n");
            deflist.append("example: " + d.getExample() + "\n");
        }
        return deflist.toString();
    }

    public List<PossibleDefinition> getSubsenses() {
        Iterator<LexicalCategory> lc = this.definition.getLexicalCategories().iterator();
        List<PossibleDefinition> pd = new ArrayList<>();
        while(lc.hasNext()){
            PossibleDefinition def = lc.next().getSense();
            pd.add(def);//add the root definition (sense)
            if (! def.getSubsenses().isEmpty()){  //this might be redundant... does hasnext throw an exception if the list is empty?
                Iterator<PossibleDefinition> subsense = def.getSubsenses().iterator();
                while (subsense.hasNext()){
                    pd.add(subsense.next());
                }
            }
        }
        return pd;
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

    public PossibleDefinition getSense() {
        return sense;
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

    public PossibleDefinition(){
        this(null,null);
    }
    public PossibleDefinition(String definition, String example) {
        this.definition = definition;
        this.example = example;
        subsenses = new ArrayList<>();

    }

    public List<PossibleDefinition> getSubsenses() { return subsenses; }

    public void addSubSense(PossibleDefinition definition) { subsenses.add(definition); }
}

class DefinitionInformation {
    private String etymologies;
    private String phoneticSpelling;
    private List<LexicalCategory> lexicalCategories;

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


    public DefinitionInformation() {
        lexicalCategories = new ArrayList<>();
    }
}