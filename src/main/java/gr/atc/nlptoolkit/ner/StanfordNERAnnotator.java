
package gr.atc.nlptoolkit.ner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class StanfordNERAnnotator {
    
    private static CRFClassifier<CoreLabel> nerClassifier;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(StanfordNERAnnotator.class);
    
    private static final String[] ISO_COUNTRIES = Locale.getISOCountries();
    
    private static final String WORD_SEPARATOR = " ";
    
    /**
     * 
     * @param modelDataPath Path that contains the model files
     */
    public StanfordNERAnnotator(String modelDataPath) {
        setModels(modelDataPath);
    }
    
    private void setModels(String modelDataPath) {
        String namedEntityModelFilePath = modelDataPath+"/classifiers/english.all.3class.distsim.crf.ser.gz";
        nerClassifier = CRFClassifier.getClassifierNoExceptions(namedEntityModelFilePath);
        LOGGER.trace("Stanford NER classifier has been initialized.");
    }
    
    /**
     * 
     * @param textList
     * @return 
     */
    public List getPersonAndOrganizationEntities(final List<String> textList) {
        
        List<String> entities = new ArrayList();
        if (CollectionUtils.isNotEmpty(textList)) {
            
            for (String text:textList) {
                
                if (StringUtils.isNotBlank(text)) {
                    // If the text contains /n character, replace it with '. '
                    text = text.replaceAll("\n", ". ");

                    String annotateWithInlineXML = nerClassifier.classifyWithInlineXML(text);
                    if (StringUtils.isBlank(annotateWithInlineXML)) {
                        annotateWithInlineXML = text;
                        LOGGER.error("Stanford NER classifier returned an empty string instead of {}", text);
                    }

//                    List<String> locationTokens = getEntities(annotateWithInlineXML, "<LOCATION>");
                    entities.addAll(getEntities(annotateWithInlineXML, "<PERSON>"));
                    entities.addAll(getEntities(annotateWithInlineXML, "<ORGANIZATION>"));
                }
            }
        }
        return entities;
    }
    
    /**
     * 
     * @param text
     * @return 
     */
    public List<NerToken> getNerIndices(final String text) {
        
        List<NerToken> result = new ArrayList<NerToken>();
        
        if (!StringUtils.isBlank(text)){
            String annotateWithInlineXML = nerClassifier.classifyWithInlineXML(text);
            if (StringUtils.isBlank(annotateWithInlineXML)) {
                annotateWithInlineXML = text;
                LOGGER.error("Stanford NER classifier returned an empty string instead of {}", text);
            }
            
            String spanStartTag = "<span class=\"locationNER\">";
            String spanEndTag = "</span>";
            
            annotateWithInlineXML = annotateWithInlineXML.replace("<LOCATION>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</LOCATION>", spanEndTag);
            
            spanStartTag = "<span class=\"personNER\">";
            annotateWithInlineXML = annotateWithInlineXML.replace("<PERSON>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</PERSON>", spanEndTag);
            
            spanStartTag = "<span class=\"organizationNER\">";
            annotateWithInlineXML = annotateWithInlineXML.replace("<ORGANIZATION>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</ORGANIZATION>", spanEndTag);
            
            NerToken nerToken = new NerToken();
            nerToken.setText(annotateWithInlineXML);
            
            result.add(nerToken);
        }
        
        return result;
    }    
    
    /**
     * 
     * @param text
     * @return 
     */
    public String getNerText(String text) {
        
        if (!StringUtils.isBlank(text)){
            
            Map<String, List<String>> resultMap = new HashMap<String, List<String>>();
            
            // If the text contains /n character, replace it with '. '
            text = text.replaceAll("\n", ". ");
            
            String annotateWithInlineXML = nerClassifier.classifyWithInlineXML(text);
            if (StringUtils.isBlank(annotateWithInlineXML)) {
                annotateWithInlineXML = text;
                LOGGER.error("Stanford NER classifier returned an empty string instead of {}", text);
            }
            
            List<String> locationTokens = getEntities(annotateWithInlineXML, "<LOCATION>");
            List<String> personTokens = getEntities(annotateWithInlineXML, "<PERSON>");
            personTokens = removeDuplicateEntities(personTokens);
            List<String> organizationTokens = getEntities(annotateWithInlineXML, "<ORGANIZATION>");
            
            if (locationTokens.isEmpty()) {
                // Check for locations from external database
                locationTokens = checkCommonLocations(text);
            }
            
            resultMap.put("location", locationTokens);
            resultMap.put("person", personTokens);
            resultMap.put("organization", organizationTokens);
            
            String spanStartTag = "<span class=\"locationNER\">";
            String spanEndTag = "</span>";
            
            annotateWithInlineXML = annotateWithInlineXML.replace("<LOCATION>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</LOCATION>", spanEndTag);
            
            spanStartTag = "<span class=\"personNER\">";
            annotateWithInlineXML = annotateWithInlineXML.replace("<PERSON>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</PERSON>", spanEndTag);
            
            spanStartTag = "<span class=\"organizationNER\">";
            annotateWithInlineXML = annotateWithInlineXML.replace("<ORGANIZATION>", spanStartTag);
            annotateWithInlineXML = annotateWithInlineXML.replace("</ORGANIZATION>", spanEndTag);
            
            Gson gsonMap = new GsonBuilder().create();
            
            return gsonMap.toJson(resultMap,Map.class);
        }
        
        return null;
    }
    
    private List<String> getEntities(final String str, final String findStr) {
        List<String> entitiesList = new ArrayList<String>();

        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = str.indexOf(findStr, lastIndex);
            if (lastIndex != -1) {
                int nerStartIndex = lastIndex;
                lastIndex += findStr.length();
                int nerEndIndex = lastIndex;
                
                int firstOccAfterToken = str.indexOf('<',nerStartIndex+1);
                        
                NerToken nerToken = new NerToken();
                nerToken.setStartIndex(nerStartIndex);
                nerToken.setEndIndex(nerEndIndex);
                entitiesList.add(str.substring(nerEndIndex, firstOccAfterToken));
            }
        }
        return entitiesList;
    }
    
    /**
     * Remove duplicate references 
     * See bug #927
     * 
     * @return 
     */
    public List<String> removeDuplicateEntities(List<String> entities) {
        
        List<String> entitiesNoDuplicates = new ArrayList<String>();
        
        if (CollectionUtils.isNotEmpty(entities)) {
            
            for (String entity:entities) {
                entitiesNoDuplicates.add(getLongestSubStringWithoutRepeatedString(entity));
            }
        }
        
        return entitiesNoDuplicates;
    }
    
    /**
     * Remove the repeated strings
     * 
     * @param phrase A string consisting of words separated by space characters
     * @return 
     */
    public String getLongestSubStringWithoutRepeatedString (String phrase) {
        
        // See also http://www.programcreek.com/2013/02/leetcode-longest-substring-without-repeating-characters-java/
        
        if (StringUtils.isBlank(phrase)) {
            return phrase;
        }
        
        List<String> list = new ArrayList<String>();
        String longestSoFar = phrase;
        String [] words = new String[0];
        int count = 0;
        int longestSize = 0;
        
        words = phrase.trim().split("[\\s,\\xA0]+");
        
        for (int i = 0; i < words.length; i++) {
            
            if (!list.contains(words[i])){
                list.add(words[i]);
                count++;
            } else {
                
                if (count > longestSize) {
                    longestSize = count;
                    longestSoFar = convertWordsToPhrase(list);
                }
                count = 0;
                list.clear();
            }
        }
        
        return longestSoFar;
    }
    
    /**
     * Convert the list of words into the respective phrase
     * 
     * @param list List of words
     * @return 
     */
    private String convertWordsToPhrase(List<String> list) {
        StringBuilder sb = new StringBuilder();
        
        int counter = 0;
        for (String word:list) {
            counter++;
            // Do not add a line separator in the last item
            if (counter != list.size()) {
                sb.append(word);
                sb.append(WORD_SEPARATOR);
            } else {
                sb.append(word);
            }
        }        
        return sb.toString();
    }    

    /**
     * 
     * @param text
     * @return 
     */
    private List<String> checkCommonLocations(String text) {
        List<String> resultList = new ArrayList<String>();
        
        for (String countryCode:ISO_COUNTRIES) {
            
            Locale locale = new Locale("", countryCode);
            if (text.toLowerCase().contains(locale.getDisplayCountry().toLowerCase())) {
                resultList.add(locale.getDisplayCountry());
            }
        }
        
        return resultList;
    }
}
