/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.sentiment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stamatis
 */
public class EvaluationMetrics {
    
    private List<String> truePosTweets = new ArrayList();    
    private List<String> trueNegTweets = new ArrayList();    
    private List<String> trueNeutralTweets = new ArrayList();
    
    private List<String> evalPosTweets = new ArrayList();
    private List<String> evalNegTweets = new ArrayList();    
    private List<String> evalNeutralTweets = new ArrayList();
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EvaluationMetrics.class);
    
    /**
     * 
     * @param trueTweetMap
     * @param evaluationTweetMap 
     */
    public EvaluationMetrics(Map<String, Sentiment> trueTweetMap, Map<String, Sentiment> evaluationTweetMap) {
        
        // Instanciate the lists
        if (trueTweetMap != null) {
            Set<String> keyMap = trueTweetMap.keySet();
            for (String uid:keyMap) {
                Sentiment annot = trueTweetMap.get(uid);
                switch(annot) {
                   case POSITIVE:
                       truePosTweets.add(uid);
                       break;
                   case NEGATIVE:
                       trueNegTweets.add(uid);
                       break;
                   case NEUTRAL:
                       trueNeutralTweets.add(uid);
                       break;
                       
                   default:LOGGER.error("No valid annotation category {} for tweet {}.", annot, uid);
               }
           }
        }
        
        if (evaluationTweetMap != null) {
            Set<String> keyMap = evaluationTweetMap.keySet();
            for (String uid:keyMap) {
                Sentiment annot = evaluationTweetMap.get(uid);
                switch(annot) {
                   case POSITIVE:
                       evalPosTweets.add(uid);
                       break;
                   case NEGATIVE:
                       evalNegTweets.add(uid);
                       break;
                   case NEUTRAL:
                       evalNeutralTweets.add(uid);
                       break;
                       
                   default:LOGGER.error("No valid annotation category {} for tweet {}.", annot, uid);
               }
           }
        }
    }

    /**
     * 
     * @param posCat
     * @return 
     */
    public double evaluatePrecisionPerCategory(Sentiment posCat) {
        int itemlsContainedInList = 0;
        int evalListSize = 0;
        int trueListSize = 0;
        if (posCat.equals(Sentiment.POSITIVE)) {
            int containedInList = 0;
            trueListSize = truePosTweets.size();
            evalListSize = evalPosTweets.size();
            for (String uid: evalPosTweets) {
                if (truePosTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        } else if (posCat.equals(Sentiment.NEGATIVE)) {
            int containedInList = 0;
            trueListSize = trueNegTweets.size();
            evalListSize = evalNegTweets.size();
            for (String uid: evalNegTweets) {
                if (trueNegTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        } else if (posCat.equals(Sentiment.NEUTRAL)) {
            int containedInList = 0;
            trueListSize = trueNeutralTweets.size();
            evalListSize = evalNeutralTweets.size();
            for (String uid: evalNeutralTweets) {
                if (trueNeutralTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        }
        
        // Calculate precision
        double precision = 0.0d;
        LOGGER.debug("Actual {} tweets: {}. Correctly evaluated tweets: {} (out of {} that were identified as such).", new Object[] { posCat, trueListSize, itemlsContainedInList, evalListSize});
        
        if (evalListSize != 0) {
            precision = (double)itemlsContainedInList/evalListSize;    
        }
        
        return precision;
    }
    
    /**
     * 
     * @param posCat
     * @return 
     */
    public double evaluateRecallPerCategory(Sentiment posCat) {
        int itemlsContainedInList = 0;
        int trueListSize = 0;
        if (posCat.equals(Sentiment.POSITIVE)) {
            int containedInList = 0;
            trueListSize = truePosTweets.size();
            for (String uid: truePosTweets) {
                if (evalPosTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        } else if (posCat.equals(Sentiment.NEGATIVE)) {
            int containedInList = 0;
            trueListSize = trueNegTweets.size();
            for (String uid: trueNegTweets) {
                if (evalNegTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        } else if (posCat.equals(Sentiment.NEUTRAL)) {
            int containedInList = 0;
            trueListSize = trueNeutralTweets.size();
            for (String uid: trueNeutralTweets) {
                if (evalNeutralTweets.contains(uid)) {
                    containedInList++;
                }
            }
            itemlsContainedInList = containedInList;
        }
        
        // Calculate precision
        double recall = 0.0d;
        LOGGER.debug("Contained items: {}, evaluation list size: {}", itemlsContainedInList, trueListSize);
        
        if (trueListSize != 0) {
            recall = (double)itemlsContainedInList/trueListSize;
        }
        
        return recall;        
    }
    
    /**
     * 
     * @param precision
     * @param recall
     * @return 
     */
    public double calculateFMeasure(double precision, double recall) {
        double fmeasure = 0.0d;
        if ((precision + recall) != 0) {
            fmeasure = (double) (2*precision*recall)/(precision + recall);
        }
        
        return fmeasure;
    }
    
}
