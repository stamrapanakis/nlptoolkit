
package gr.atc.nlptoolkit.language;

import gr.atc.nlptoolkit.language.LanguageDetector;
import gr.atc.nlptoolkit.language.LingwayLanguageDetector;
import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SRapanakis
 */
public class LingwayLanguageDetectorTest {
    
    private static final LanguageDetector LANGUAGE_DETECTOR = new LingwayLanguageDetector();
    
    public LingwayLanguageDetectorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        LANGUAGE_DETECTOR.register(ConfigurationUtil.getModelsFilePath());
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /**
     * Test of isEnglishText method, of class LingwayLanguageDetector.
     */
    @Test
    public void testIsEnglishText() {
        
        List<String> tweets = new ArrayList<String>();
        tweets.add("Athens is the capital of Greece.");
        tweets.add("I love you.");
        tweets.add("I hate my mother.");
        tweets.add("123");
        tweets.add("wergfw w wrwew wr gtrwehg");
        tweets.add("\r  \n");
        tweets.add("!!");
        
        List<Boolean> expResult = new ArrayList<Boolean>();
        expResult.add(Boolean.TRUE);
        expResult.add(Boolean.TRUE);
        expResult.add(Boolean.TRUE);
        expResult.add(Boolean.FALSE);
        expResult.add(Boolean.FALSE);
        expResult.add(Boolean.FALSE);
        expResult.add(Boolean.FALSE);
        
        int testTweetsSize = (tweets.size() == expResult.size())? tweets.size(): -1;
        
        for (int i = 0; i < testTweetsSize; i++) {
            assertEquals(expResult.get(i), LANGUAGE_DETECTOR.isEnglishText(tweets.get(i)));
        }
        
    }
    
}
