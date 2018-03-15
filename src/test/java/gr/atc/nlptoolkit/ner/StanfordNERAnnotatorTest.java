/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.ner;

import gr.atc.nlptoolkit.ner.StanfordNERAnnotator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
public class StanfordNERAnnotatorTest {
    
    private static StanfordNERAnnotator ANNOTATOR;
    private static Gson GSON;
    
    public StanfordNERAnnotatorTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        ANNOTATOR = new StanfordNERAnnotator(ConfigurationUtil.getModelsFilePath());
        GSON = new GsonBuilder().create();
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
     * Test of getNerIndices method, of class StanfordNERAnnotator.
     *//*
    @Test
    public void testGetNerIndices() {
        System.out.println("getNerIndices");
        String text = "";
        StanfordNERAnnotator instance = null;
        List<NerToken> expResult = null;
        List<NerToken> result = instance.getNerIndices(text);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }*/

    /**
     * Test of getNerText method, of class StanfordNERAnnotator.
     */
    @Test
    public void testGetNerText() {
        String text1 = "In 6 years Barack Obama blows more than 10,5 trillions USD debts";
        String expResult1 = "Barack Obama";
        Map result1 = GSON.fromJson(ANNOTATOR.getNerText(text1), Map.class);
        List<String> persons1 = (List)result1.get("person");
        assertEquals(expResult1, persons1.get(0));
        
        String text2 = "I love Jennifer Lopez Jennifer Lopez is so cute!";
        String expResult2 = "Jennifer Lopez";
        Map result2 = GSON.fromJson(ANNOTATOR.getNerText(text2), Map.class);
        List<String> persons2 = (List)result2.get("person");
        assertEquals(expResult2, persons2.get(0));
        
        // Replace the new lines \n character with '. ' 
        String text3 = "RT @TerriBauman: Getting Hired\nThrough Social Media\nFrom "
                + "Social Media\nFor Social Media\nVisit:http://t.co/NDDK4VTgIw \n\nPlease Retweet\n\nhttp…";
        Map result3 = GSON.fromJson(ANNOTATOR.getNerText(text3), Map.class);
        List<String> persons3 = (List)result3.get("person");
        assertEquals(0, persons3.size());
    }

    /**
     * Test of removeDuplicateEntities method, of class StanfordNERAnnotator.
     */
    @Test
    public void testRemoveDuplicateEntities() {
        // "British Colombia CAN Kelowna -> http://t.co/aPO2P5R9dz >> UPDATE >> 544 Jennifer Lopez Jennifer Lopez offers advice to Ryan Guzman during..."
        
        List<String> testCases = new ArrayList<String>();
        testCases.add("Jennifer Lopez Jennifer Lopez");
        testCases.add("A B");
//        testCases.add("A B A");
        testCases.add("A B A B");
//        testCases.add("A B A B A");
        testCases.add("A B C A B C");
        testCases.add("A A A");
        
        List<String> expeCases = new ArrayList<String>();
        expeCases.add("Jennifer Lopez");
        expeCases.add("A B");
//        expeCases.add("A B A");
        expeCases.add("A B");
//        expeCases.add("A B A");
        expeCases.add("A B C");
        expeCases.add("A");
        
        List<String> computedResult = ANNOTATOR.removeDuplicateEntities(testCases);
        
        int expeCasesIndex = 0;
        for (String computedEntity:computedResult) {
            assertEquals(expeCases.get(expeCasesIndex++), computedEntity);
        }
    }
    
}
