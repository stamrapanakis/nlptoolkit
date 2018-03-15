/**
 * 
 */
package gr.atc.nlptoolkit.sentiment;

import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import gr.atc.nlptoolkit.utils.Tools;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author SRapanakis
 */
public class GreekCustomLexicon {

    private String modelDataPath;
    
    public GreekCustomLexicon(String modelDataPath) {
        this.modelDataPath = modelDataPath;
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, Integer> getGreekCustomLexicon() {
        
        // Positive unigrams stemmed
        List<String> positiveUnigramsStemmedList = Tools.readFile(modelDataPath+
                "/greek/positive_unigrams_stemmed.txt");
        
        // Negative unigrams stemmed
        List<String> negativeUnigramsStemmedList = Tools.readFile(modelDataPath+
                "/greek/negatives_unigrams_stemmed.txt");
        
//        Set<CustomLexiconEntry> lexiconEntries = customLexicon.customLexicon;
        Map<String, Integer> lexicon = new HashMap<String, Integer>();
        
        for (String positiveUnigramStemmed:positiveUnigramsStemmedList) {
            lexicon.put(positiveUnigramStemmed, 1);
        }
        
        for (String negativeUnigramsStemmed:negativeUnigramsStemmedList) {
            lexicon.put(negativeUnigramsStemmed, -1);
        }
        
        return lexicon;
    }    
    
}
