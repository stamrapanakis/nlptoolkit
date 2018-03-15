/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.events;

import gr.atc.nlptoolkit.events.TemporalSimilarity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class TemporalSimilarityTest {
    
    private final TemporalSimilarity instance = new TemporalSimilarity();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalSimilarityTest.class);
    
    public TemporalSimilarityTest() {
    }
    
    @Before
    public void setUp() {
        
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of secondsBetweenDates method, of class TemporalSimilarity.
     */
    @Test
    public void testSecondsBetweenDates() {
        
        String dateStart = "01/14/2012 09:29:58.725";
        String dateStop = "01/15/2012 10:31:48.725";
 
	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SS");
        
        Date date1 = null;
        Date date2 = null;
        try {
            date1 = format.parse(dateStart);
            date2 = format.parse(dateStop);
        } catch (ParseException ex) {
            LOGGER.error("Date parse exception {}", ex);
        }
        
        assertEquals(2422910L, instance.secondsBetweenDates(date1, date2));
    }
    
}
