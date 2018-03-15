/**
 * 
 */
package gr.atc.nlptoolkit.sentiment;

/**
 *
 * @author SRapanakis
 */
public class GenericLexiconFeature {
    
    private String feature;
    
    // Total score of the tokens
    private int totalTokensScore = 0;

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public int getTotalTokensScore() {
        return totalTokensScore;
    }

    public void setTotalTokensScore(int totalTokensScore) {
        this.totalTokensScore = totalTokensScore;
    }
}
