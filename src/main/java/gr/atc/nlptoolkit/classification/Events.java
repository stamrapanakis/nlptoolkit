
package gr.atc.nlptoolkit.classification;

/**
 *
 * @author SRapanakis
 */
public enum Events {
    
    JUNK(0), TECHNOLOGY(1), BUSINESS(2), CULTURE(3), SCIENCE(4), POLITICS(5), SPORTS(6);
    
    private final int value;
    
    private Events(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        if (value == 0) {
            return "JUNK";
        } else if (value == 1) {
            return "TECHNOLOGY";
        } else if (value == 2) {
            return "BUSINESS";
        } else if (value == 3) {
            return "CULTURE";
        } else if (value == 4) {
            return "SCIENCE";
        } else if (value == 5) {
            return "POLITICS";
        } else if (value == 6) {
            return "SPORTS";
        }
        
        return null;
    }
}
