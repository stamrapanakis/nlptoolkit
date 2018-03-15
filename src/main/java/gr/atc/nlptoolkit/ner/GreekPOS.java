
package gr.atc.nlptoolkit.ner;

import gr.atc.nlptoolkit.language.LanguageDetector;
import gr.atc.nlptoolkit.language.LingwayLanguageDetector;
import gr.atc.nlptoolkit.stylometry.EmoticonsDetector;
import gr.atc.nlptoolkit.greekpos.SmallSetFunctions;
import gr.atc.nlptoolkit.greekpos.WordWithCategory;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class GreekPOS {
    
    private static final LanguageDetector LANGUAGE_DETECTOR = new LingwayLanguageDetector();
    private static String modelDataPath;
    private static SmallSetFunctions smallSetFunctions;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GreekPOS.class);
    
    public GreekPOS(String modelDataPath) {
        this.modelDataPath = modelDataPath;
        setModels();
    }
    
    /**
     * 
     */
    private void setModels(){
        // Instanciate language detector
        String languageDetectionFolderPath = modelDataPath;
        LOGGER.info("language detector files {}", languageDetectionFolderPath);
        LANGUAGE_DETECTOR.register(languageDetectionFolderPath);
        smallSetFunctions = new SmallSetFunctions(modelDataPath);
    }
    
    /**
     * 
     * @param tweet
     * @return 
     */
    public List getNouns(final String tweet) {
        
        List nounsList = null;
        if (!StringUtils.isBlank(tweet)) {
            // Only greek tweets
            if (LANGUAGE_DETECTOR.isGreekText(tweet)) {
                
                nounsList = new ArrayList();
                
                // Tokenize the string
                List<String> tokens = EmoticonsDetector.tokenizeRawTweetText(tweet);
                StringBuilder tokensWithSpace = new StringBuilder();
                for (String token:tokens){
                    tokensWithSpace.append(token);
                    tokensWithSpace.append(" ");
                }
                
                List<WordWithCategory> list = smallSetFunctions.smallSetClassifyString(tokensWithSpace.toString());
                for (WordWithCategory word:list){
                    if ("noun".equals(word.getCategory())){
                        nounsList.add(word);
                    }
                }
            }
        }
        return nounsList;
    }    
}