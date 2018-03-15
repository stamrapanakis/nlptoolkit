/**
 * 
 */
package gr.atc.nlptoolkit.sentiment;

/**
 *
 * @author SRapanakis
 */
public class GreekLexiconEntry implements Comparable<GreekLexiconEntry>{
    
    private String term;
    private String termToken;
    private String termTokenStemmed;

    public enum Subjectivity {
        NA, OBJ, SUBJ_N, SUBJ_P
    }
    
    public enum Polarity {
        NA, BOTH, NEG, POS
    }
    
    private int subjectivityScore;
    private int polarityScore;
    private double angerScore;
    private double disgustScore;
    private double fearScore;
    private double hapinessScore;
    private double sadnessScore;
    private double surpriseScore;
    private double feelingsScore;
    
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }
    
    public int getSubjectivityScore() {
        return subjectivityScore;
    }

    public void setSubjectivityScore(int subjectivityScore) {
        this.subjectivityScore = subjectivityScore;
    }

    public int getPolarityScore() {
        return polarityScore;
    }

    public void setPolarityScore(int polarityScore) {
        this.polarityScore = polarityScore;
    }

    public double getAngerScore() {
        return angerScore;
    }

    public void setAngerScore(double angerScore) {
        this.angerScore = angerScore;
    }

    public double getDisgustScore() {
        return disgustScore;
    }

    public void setDisgustScore(double disgustScore) {
        this.disgustScore = disgustScore;
    }

    public double getFearScore() {
        return fearScore;
    }

    public void setFearScore(double fearScore) {
        this.fearScore = fearScore;
    }

    public double getHapinessScore() {
        return hapinessScore;
    }

    public void setHapinessScore(double hapinessScore) {
        this.hapinessScore = hapinessScore;
    }

    public double getSadnessScore() {
        return sadnessScore;
    }

    public void setSadnessScore(double sadnessScore) {
        this.sadnessScore = sadnessScore;
    }

    public double getSurpriseScore() {
        return surpriseScore;
    }

    public void setSurpriseScore(double surpriseScore) {
        this.surpriseScore = surpriseScore;
    }

    public double getFeelingsScore() {
        return feelingsScore;
    }

    public void setFeelingsScore(double feelingsScore) {
        this.feelingsScore = feelingsScore;
    }
    
    @Override
    public int compareTo(GreekLexiconEntry other) {
        return Double.compare(other.feelingsScore, feelingsScore);
    }    

    public String getTermToken() {
        return termToken;
    }

    public void setTermToken(String termToken) {
        this.termToken = termToken;
    }

    public String getTermTokenStemmed() {
        return termTokenStemmed;
    }

    public void setTermTokenStemmed(String termTokenStemmed) {
        this.termTokenStemmed = termTokenStemmed;
    }

    @Override
    public String toString() {
        return "GreekLexiconEntry{" + "termTokenStemmed=" + termTokenStemmed + ", polarityScore=" + polarityScore + '}';
    }

}

