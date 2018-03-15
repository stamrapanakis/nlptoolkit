
package gr.atc.nlptoolkit.sentiment;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.instances.TweetInstance;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author SRapanakis
 */
public class SentimentTweetInstance extends TweetInstance {
    
    private Sentiment sentiment;
    
    public SentimentTweetInstance(String tweetId, String tweetText, Tagger arkTagger) {
        super(tweetId, tweetText, arkTagger);
    }
    
    /**
     * 
     * @param tweetId
     * @param tweetText
     * @param sentiment
     * @param arkTagger 
     */
    public SentimentTweetInstance(String tweetId, String tweetText, Sentiment sentiment, Tagger arkTagger) {
        super(tweetId, tweetText, arkTagger);
        this.sentiment = sentiment;
    }
    
    public Sentiment getSentiment() {
        return sentiment;
    }    
    
    @Override
    public String toString() {
        return sentiment+"\t"+this.getTweetText();
    }
}
