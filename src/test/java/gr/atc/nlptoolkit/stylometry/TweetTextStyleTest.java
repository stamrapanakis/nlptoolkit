/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.stylometry;

import gr.atc.nlptoolkit.stylometry.TweetTextStyle;
import gr.atc.nlptoolkit.stylometry.TextStyleResponse;
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
public class TweetTextStyleTest {
    
    private static TweetTextStyle textStyle;
    
    public TweetTextStyleTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        textStyle = new TweetTextStyle(ConfigurationUtil.getModelsFilePath());
    }
    
    @AfterClass
    public static void tearDownClass() {
    }

    /**
     * Test of getTextStyleEN method, of class TweetTextStyle.
     */
    @Test
    public void testGetTextStyle() {
        String tweetText = "Culture select committee report expected to back axing of BBC Trust. http://t.co/uaUM4aYERU http://t.co/13AOLCBAnz";
        TextStyleResponse expResult = new TextStyleResponse();
        TextStyleResponse result = textStyle.getTextStyleEN(tweetText);
        assertEquals(expResult, result);
    }

    /**
     * Test of characterizeTextStyle method, of class TweetTextStyle.
     */
    @Test
    public void testCharacterizeTextStyle() {
        
        List<String> testTweetsText = new ArrayList<String>();
        List<String> expResults = new ArrayList<String>();
        
        // Tweets from reporters Twitter account (atc_alethiometr) 
        testTweetsText.add("Mr @JamieRoss7 talks to the leader of an anti-feminism party standing in the general election. It is quite something- http://www.buzzfeed.com/jamieross/an-anti-feminist-party-is-standing-in-the-general-election …");
        expResults.add("Common");
        testTweetsText.add("Key paragraph in #ECJ advocate general's opinion on #OMT: @ecb must have \"braod discretion\" to set own policy.");
        expResults.add("Common");
        testTweetsText.add("Why didn't the chicken cross the road? Genius by @Dannythefink Times £ http://www.thetimes.co.uk/tto/opinion/columnists/article4322503.ece …");
        expResults.add("Common");
        testTweetsText.add("So galling for #EdMiliband that ppl still making this mistake 4 months before an election. #UKElection2015 #leaderdebate");
        expResults.add("Common");
        testTweetsText.add("#commodities rout, #metals edition: #Copper dented by growth worries; down 7% at one point http://on.ft.com/1z9P9av");
        expResults.add("Common");
        testTweetsText.add("Oh dear, just now on @BBCr4today: \"The proposal is for a debate btwn DAVID Miliband and David Cameron.\" #leaderdebate #UKElection2015");
        expResults.add("Common");
        testTweetsText.add("“#Venezuela cannot squeeze its #oil lemon any more\" http://www.ft.com/cms/s/0/0cc530b6-9b2f-11e4-882d-00144feabdc0.html?ftcamp=published_links%2Frss%2Fcompanies_oil-gas%2Ffeed%2F%2Fproduct#axzz3OgZd4dxO … @AndresSchipani @JP_Rathbone");
        expResults.add("Common");
        testTweetsText.add("#Oil price plunge replays 1986 bust, but US #shale a game changer http://on.wsj.com/1DWfacq  via @russellgold #OPEC");
        expResults.add("Common");
        
        // Tweets that refer to commercial services
        testTweetsText.add("Thinking of trying DigitalOcean? Get started now for as little as $5 a month: http://do.co/1poO5vi");
        expResults.add("Common");
        testTweetsText.add("SoftLayer virtual servers outperform those other servers. Try one free to see for yourself. ≡ http://sftlyr.com/5j5");
        expResults.add("Informal");
        testTweetsText.add("Tech-Heads in Big Data using #Hadoop? Our CloudOOP12000 server that consumes 50% Less Power & Are Double the Density");
        expResults.add("Common");
        testTweetsText.add("Culture select committee report expected to back axing of BBC Trust. http://t.co/uaUM4aYERU http://t.co/13AOLCBAnz");        
        expResults.add("Common");
        
        for (int i = 0; i < testTweetsText.size(); i++) {
            String tweetText = testTweetsText.get(i);
            String result = textStyle.characterizeTextStyle(textStyle.getTextStyleEN(tweetText));
            String expResult = expResults.get(i);
            
            assertEquals(expResult, result);
        }
        
    }

}
