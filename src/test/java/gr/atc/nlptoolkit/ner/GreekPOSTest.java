
package gr.atc.nlptoolkit.ner;

import gr.atc.nlptoolkit.ner.GreekPOS;
import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import gr.atc.nlptoolkit.utils.Tools;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author SRapanakis
 */
public class GreekPOSTest {
    
    public GreekPOSTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getNouns method, of class GreekPOS.
     */
    @Test
    public void testGetNouns() {
        
        GreekPOS greekPOS = new GreekPOS(ConfigurationUtil.getModelsFilePath());
        
        String tweet = "Αντικείμενο της συνάντησης ήταν η κατάσταση στη χώρα μας, όπως διαμορφώθηκε μετά την ψήφιση του Μεσοπρόθεσμου, οι αποφάσεις της Συνόδου Κορυφής της ΕΕ, αλλά και ο εναλλακτικός δρόμος που προτείνει η Αριστερά για έξοδο από την κρίση όλων των χωρών που αντιμετωπίζουν ανάλογα προβλήματα.";
        tweet = tweet.toLowerCase();
        List result = greekPOS.getNouns(tweet);
        //System.out.println(Tools.prettyPrintList(result));
        assertEquals(12, result.size());
    }

}
