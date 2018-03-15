/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.TimeExpression;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TemporalDetection {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(TemporalDetection.class);
    
    private static final List TEMPORAL_WORDS_LIST = new ArrayList();

    private TemporalDetection() {
    }
    
    static {
        
        // Days of the week
        TEMPORAL_WORDS_LIST.add("monday");
        TEMPORAL_WORDS_LIST.add("monday's");
        TEMPORAL_WORDS_LIST.add("tuesday");
        TEMPORAL_WORDS_LIST.add("tuesday's");
        TEMPORAL_WORDS_LIST.add("wednesday");
        TEMPORAL_WORDS_LIST.add("wednesday's");
        TEMPORAL_WORDS_LIST.add("thursday");
        TEMPORAL_WORDS_LIST.add("thursday's");
        TEMPORAL_WORDS_LIST.add("friday");
        TEMPORAL_WORDS_LIST.add("friday's");
        TEMPORAL_WORDS_LIST.add("saturday");
        TEMPORAL_WORDS_LIST.add("saturday's");
        TEMPORAL_WORDS_LIST.add("sunday");
        TEMPORAL_WORDS_LIST.add("sunday's");
        
        // Months of the year
        TEMPORAL_WORDS_LIST.add("january");
        TEMPORAL_WORDS_LIST.add("february");
        TEMPORAL_WORDS_LIST.add("march");
        TEMPORAL_WORDS_LIST.add("april");
        TEMPORAL_WORDS_LIST.add("may");
        TEMPORAL_WORDS_LIST.add("june");
        TEMPORAL_WORDS_LIST.add("july");
        TEMPORAL_WORDS_LIST.add("august");
        TEMPORAL_WORDS_LIST.add("september");
        TEMPORAL_WORDS_LIST.add("october");
        TEMPORAL_WORDS_LIST.add("november");
        TEMPORAL_WORDS_LIST.add("december");
        
        // Months of the year, shortened
        TEMPORAL_WORDS_LIST.add("jan");
        TEMPORAL_WORDS_LIST.add("feb");
        TEMPORAL_WORDS_LIST.add("mar");
        TEMPORAL_WORDS_LIST.add("apr");
        TEMPORAL_WORDS_LIST.add("may");
        TEMPORAL_WORDS_LIST.add("jun");
        TEMPORAL_WORDS_LIST.add("jul");
        TEMPORAL_WORDS_LIST.add("aug");
        TEMPORAL_WORDS_LIST.add("sep");
        TEMPORAL_WORDS_LIST.add("oct");
        TEMPORAL_WORDS_LIST.add("nov");
        TEMPORAL_WORDS_LIST.add("dec");
        
        // Days, weeks years
        TEMPORAL_WORDS_LIST.add("day");
        TEMPORAL_WORDS_LIST.add("days");
        TEMPORAL_WORDS_LIST.add("week");
        TEMPORAL_WORDS_LIST.add("weeks");
        TEMPORAL_WORDS_LIST.add("year");
        TEMPORAL_WORDS_LIST.add("years");
        
        // today, tomorrow, tonight, yesterday
        TEMPORAL_WORDS_LIST.add("today");
        TEMPORAL_WORDS_LIST.add("today's");
        TEMPORAL_WORDS_LIST.add("tomorrow");
        TEMPORAL_WORDS_LIST.add("tomorrow's");
        TEMPORAL_WORDS_LIST.add("tonight");
        TEMPORAL_WORDS_LIST.add("tonight's");
        TEMPORAL_WORDS_LIST.add("yesterday");
        TEMPORAL_WORDS_LIST.add("yesterday's");
        
        // morning, noon, afternoon, evening, night, midnight
        TEMPORAL_WORDS_LIST.add("morning");
        TEMPORAL_WORDS_LIST.add("noon");
        TEMPORAL_WORDS_LIST.add("afternoon");
        TEMPORAL_WORDS_LIST.add("evening");
        TEMPORAL_WORDS_LIST.add("night");
        TEMPORAL_WORDS_LIST.add("midnight");
        
        // pm, am
        TEMPORAL_WORDS_LIST.add("pm");
        TEMPORAL_WORDS_LIST.add("p.m.");
        TEMPORAL_WORDS_LIST.add("am");
        TEMPORAL_WORDS_LIST.add("a.m.");
    }
    
        
    /**
     * True if a word is temporal
     * 
     * @param word
     * @return 
     */
    public static boolean isTemporalWord(String word) {
        
        if ((word != null) && (TEMPORAL_WORDS_LIST.contains(word.toLowerCase()))) {
            return true;
        }
        
        return false;
    }

  /** Example usage:
   *  java TemporalDetection "Three interesting dates are 18 Feb 1997, the 20th
   of july and 4 days from today."
   *
   *  @param args Strings to interpret
   */
  public static void main(String[] args) {
      
    String text = "The interesting date is 4 days from today and it is 20th july"
      + " of this year, another date is 18th Feb 1997";
      
    Properties props = new Properties();
    AnnotationPipeline pipeline = new AnnotationPipeline();
    
    // TODO MALLET should support this annotator, perhaps implement a similar one
    
    // TODO Note that "current" is parsed as the current date/ time, which is not 
    // true since the tweets publish time should be used as current. Similar case 
    // exists for all the other time references.
    pipeline.addAnnotator(new PTBTokenizerAnnotator(false));
    pipeline.addAnnotator(new WordsToSentencesAnnotator(false));
    pipeline.addAnnotator(new POSTaggerAnnotator(false));
    
    pipeline.addAnnotator(new TimeAnnotator("sutime", props));
    
    Annotation doc = new Annotation(text);
    
    doc.set(CoreAnnotations.DocDateAnnotation.class, getCurrentTimeStamp());
    pipeline.annotate(doc);
      
      List<CoreMap> timexAnnotations = doc.get(TimeAnnotations.TimexAnnotations.class);
      for (CoreMap timexAnn : timexAnnotations) {
          List tokens = timexAnn.get(CoreAnnotations.TokensAnnotation.class);
          
          // Get the string index position in the original string
          // Result:
          // 4 days from today [from char offset 24 to 41] --> 2013-12-16
          // --
          // 20th july of this year [from char offset 52 to 74] --> 2013-07-20
          // --
          // 18th Feb 1997 [from char offset 92 to 105] --> 1997-2-18
          LOGGER.debug(timexAnn + " [from char offset " +
          ((ArrayCoreMap)tokens.get(0)).get(CoreAnnotations.CharacterOffsetBeginAnnotation.class) +
          " to " + ((ArrayCoreMap)tokens.get(tokens.size() - 1)).get(CoreAnnotations.CharacterOffsetEndAnnotation.class) + ']' +
          " --> " + timexAnn.get(TimeExpression.Annotation.class).getTemporal());
          
          LOGGER.debug("--");
      }
      
  }// main
  
  public static String getCurrentTimeStamp() {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      return sdf.format(new Date());
  }
  
}// class

