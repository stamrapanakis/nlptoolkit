
package gr.atc.nlptoolkit.classification;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.instances.TweetInstance;

/**
 *
 * @author SRapanakis
 */
public class CategoryTweetInstance extends TweetInstance {
    
    private Events category;

    /**
     * 
     * @param tweetId
     * @param tweetText
     * @param arkTagger 
     */
    public CategoryTweetInstance(String tweetId, String tweetText, Tagger arkTagger) {
        super(tweetId, tweetText, arkTagger);
    }
    
    protected Events getCategory() {
        return category;
    }

    protected void setCategory(Events category) {
        this.category = category;
    }
}
