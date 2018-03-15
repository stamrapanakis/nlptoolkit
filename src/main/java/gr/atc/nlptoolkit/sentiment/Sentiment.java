
package gr.atc.nlptoolkit.sentiment;

/**
 *
 * @author SRapanakis
 */
public enum Sentiment {
    POSITIVE(0), NEGATIVE(1), NEUTRAL(2);
    
    private final int value;
    
    private Sentiment(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == 0) {
            return "POSITIVE";
        } else if (value == 1) {
            return "NEGATIVE";
        } else if (value == 2) {
            return "NEUTRAL";
        }
        
        return null;
    }
}
