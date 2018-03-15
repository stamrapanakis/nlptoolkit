/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.sentiment;

/**
 *
 * @author SRapanakis
 */
public interface Analyzer {
    
    /**
     * Get the sentiment of the current tweet
     * 
     * @param tweet
     * @return 
     */
    public Sentiment findSentiment(String tweet);
    
    /**
     * 
     * @param tweet
     * @param lang
     * @return 
     */
    public Sentiment findSentiment(String tweet, String lang);
}
