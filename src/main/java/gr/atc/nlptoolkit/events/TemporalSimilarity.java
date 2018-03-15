
package gr.atc.nlptoolkit.events;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

/**
 *
 * @author SRapanakis
 */
public class TemporalSimilarity {
    
    
    // Get the older date
    // Assign the dates to a timeline
    // Calculate the time intervals (greatest -> dissimilar)
    // Sort them by ascending order (similarity)
    
    /**
     * 
     * @param date1
     * @param date2 
     * @return  
     */
    public long secondsBetweenDates(Date date1, Date date2) {
        if ((date1 != null) && (date2 != null)) {
            DateTime dt1 = new DateTime(date1);
            DateTime dt2 = new DateTime(date2);
            
            // duration in ms between two instants
            
            Interval interval = new Interval(dt1.getMillis(), dt2.getMillis());
            
            Duration duration = interval.toDuration();
            
            return duration.getStandardSeconds();
        }
        
        return -1;
    }
}
