/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.sentiment;

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
public class SemevalSentimentAnalyzerTest {
    
    static SemevalSentimentAnalyzer sentimentAnalyzer;
    
    public SemevalSentimentAnalyzerTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        sentimentAnalyzer = new SemevalSentimentAnalyzer(ConfigurationUtil.getModelsFilePath());
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    /**
     * Test of findSentiment method, of class SemevalSentimentAnalyzer.
     */
    @Test
    public void testFindSentimentEN() {
        
        List<String> tweets = new ArrayList<String>();
        tweets.add("Athens is the capital of Greece.");
        tweets.add("I love you.");
        tweets.add("I hate my mother.");
        tweets.add("123");
        tweets.add("wergfw w wrwew wr gtrwehg");
        tweets.add("\r  \n");
        tweets.add("!!");
        
        List<Sentiment> expResult = new ArrayList<Sentiment>();
        expResult.add(Sentiment.NEUTRAL);
//        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEUTRAL);
        expResult.add(Sentiment.NEUTRAL);
        expResult.add(Sentiment.NEUTRAL);
        expResult.add(Sentiment.NEUTRAL);
        
        int testTweetsSize = (tweets.size() == expResult.size())? tweets.size(): -1;
        
        for (int i = 0; i < testTweetsSize; i++) {
            Sentiment result = sentimentAnalyzer.findSentiment(tweets.get(i), "en");
            
            assertEquals(expResult.get(i), result);
        }
        
    }
    
    /**
     * Test of findSentiment method, of class SemevalSentimentAnalyzer.
     */
    @Test
    public void testFindSentimentEL() {
        
        List<String> tweets = new ArrayList<String>();
        tweets.add("Γκουρία: Η Ελλάδα θα καταλήξει σε συμφωνία με τους πιστωτές της, in.gr http://t.co/aI29Caeq4P");
        tweets.add("Γκουρία: Η Ελλάδα θα φτάσει σε συμφωνία με τους πιστωτές της");
        tweets.add("Γκουρία: Η Ελλάδα θα φτάσει σε συμφωνία με τους πιστωτές - ΟΙΚΟΝΟΜΙΑ - euro2day.gr https://t.co/uYZS6jvjYe via @sharethis");
        tweets.add("Γκουρία: Η Ελλάδα θα φτάσει σε συμφωνία με τους πιστωτές-Πολύ υψηλό το τίμημα ενός Grexit #oikonomia http://t.co/hCTpXT4SAm");
        tweets.add("Ολάντ: Η Γαλλία θα κάνει ό,τι είναι δυνατόν για την επίτευξη συμφωνίας http://t.co/sIrM23Z0xx");
        tweets.add("Ολάντ: Η Γαλλία θα κάνει τα πάντα για την επίτευξη συμφωνίας απόψε http://t.co/djUiE07EIO via @News247gr");
        tweets.add("Ένα πιο ειλικρινές ερώτημα νομίζω θα ήταν θέλετε να πεινάσουμε για να σώσουμε τις τράπεζες ή θέλετε να πεινάσουμε... http://t.co/K5BVA1n4Sz");
        tweets.add("Πιο πολύ από τις ουρές στις τράπεζες και τον ΟΑΕΔ με τρομάζουν οι ουρές των δράκων της καλίσι");
        tweets.add("Ναι τον Αδωνι περιμένανε οι ξένοι για να κλείσουν τις τράπεζες. Τόσο χαϊβάνια ειναι.  https://t.co/DOdQsGqz4g");
        tweets.add("@AdonisGeorgiadi  αδωνη η αποτυχία της αντιπολιτευσης είναι καθολικη. Ο τσιπρας έκλεισε τις τράπεζες και τον αποθεωνουν.");
        tweets.add("ΨΕΜΜΑΤΑ ΚΑΤΑΦΑΤΣΑ......Βαρουφάκης: Είτε με «ναι» είτε με «όχι» οι τράπεζες θα ανοίξουν ξανά την Τρίτη http://t.co/Hfhj5XcLa0");
        tweets.add("Τη δραματική ομολογία ότι τα χρήματα στις τράπεζες για τον εφοδιασμό των ΑΤΜ επαρκούν μόνο μέχρι τη Δευτέρα έκανε... http://t.co/rmOkwHVL48");
        tweets.add("Channel 4: «Πραξικόπημα από τράπεζες και ιδιοκτήτες ΜΜΕ στην Ελλάδα κατά της νόμιμης κυβέρνησης» | DefenceNet.gr: http://t.co/kyUEm6efr2");
//        tweets.add("\r  \n");
//        tweets.add("!!");
        
        List<Sentiment> expResult = new ArrayList<Sentiment>();
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.POSITIVE);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEUTRAL);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEGATIVE);
        expResult.add(Sentiment.NEGATIVE);
//        
        int testTweetsSize = (tweets.size() == expResult.size())? tweets.size(): -1;
        
        for (int i = 0; i < testTweetsSize; i++) {
            Sentiment result = sentimentAnalyzer.findSentiment(tweets.get(i), "el");
            
            assertEquals(expResult.get(i), result);
        }
        
    }    
    
    @Test
    public void testGetNominalsListEN() {
        List result = sentimentAnalyzer.getNominalsListEN("Eurobank Ergasias CEO Fokion Karavias");
        assertEquals(5, result.size());
    }

}
