/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.ner;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author SRapanakis
 */
public class NerTokenDeserializer implements JsonDeserializer<List<NerToken>>{

    @Override
    public List<NerToken> deserialize(JsonElement json, Type type, JsonDeserializationContext jdc) throws JsonParseException {
        
        JsonArray jsonArray = json.getAsJsonArray();        
        Iterator<JsonElement> iterator = jsonArray.iterator();
        
        List<NerToken> resultList = new ArrayList<NerToken>();
        
        while (iterator.hasNext()) {
            JsonObject mapAsJson = (JsonObject)iterator.next();
            String text = mapAsJson.get("text").getAsString();
            int startIndex = mapAsJson.get("startIndex").getAsInt();
            int endIndex = mapAsJson.get("endIndex").getAsInt();
            String nerCategory = mapAsJson.get("nerType").getAsString();
            
            NerToken nerToken = new NerToken(text, startIndex, endIndex, NER_CATEGORY.valueOf(nerCategory));
            resultList.add(nerToken);
        }
        
        return resultList;
    }
    
}

