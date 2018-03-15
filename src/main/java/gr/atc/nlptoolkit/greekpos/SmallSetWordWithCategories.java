/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.atc.nlptoolkit.greekpos;

import java.util.LinkedHashMap;
import java.util.Map;

public class SmallSetWordWithCategories {

    private String word;
    private double isArticle;
    private double isVerb;
    private double isPunctuation;
    private double isAdjective;
    private double isAdverb;
    private double isConjunction;
    private double isNoun;
    private double isNumeral;
    private double isParticle;
    private double isPreposition;
    private double isPronoun;
    private double isOther;

    protected SmallSetWordWithCategories() {
        isArticle = 0.0;
        isVerb = 0.0;
        isPunctuation = 0.0;
        isAdjective = 0.0;
        isAdverb = 0.0;
        isConjunction = 0.0;
        isNoun = 0.0;
        isNumeral = 0.0;
        isParticle = 0.0;
        isPreposition = 0.0;
        isPronoun = 0.0;
        isOther = 0.0;
    }

    protected SmallSetWordWithCategories(String w) {
        word = w;
        isArticle = 0.0;
        isVerb = 0.0;
        isPunctuation = 0.0;
        isAdjective = 0.0;
        isAdverb = 0.0;
        isConjunction = 0.0;
        isNoun = 0.0;
        isNumeral = 0.0;
        isParticle = 0.0;
        isPreposition = 0.0;
        isPronoun = 0.0;
        isOther = 0.0;
    }

    protected SmallSetWordWithCategories(SmallSetWordWithCategories w) {
        word = w.word;
        isArticle = w.isArticle;
        isVerb = w.isVerb;
        isPunctuation = w.isPunctuation;
        isAdjective = w.isAdjective;
        isAdverb = w.isAdverb;
        isConjunction = w.isConjunction;
        isNoun = w.isNoun;
        isNumeral = w.isNumeral;
        isParticle = w.isParticle;
        isPreposition = w.isPreposition;
        isPronoun = w.isPronoun;
        isOther = w.isOther;
    }

    protected void setWord(String w) {
        word = w;
    }

    protected void setArticle(double b) {
        isArticle = b;
    }

    protected void setVerb(double b) {
        isVerb = b;
    }

    protected void setPunctuation(double b) {
        isPunctuation = b;
    }

    protected void setAdjective(double b) {
        isAdjective = b;
    }

    protected void setAdverb(double b) {
        isAdverb = b;
    }

    protected void setConjunction(double b) {
        isConjunction = b;
    }

    protected void setNoun(double b) {
        isNoun = b;
    }

    protected void setNumeral(double b) {
        isNumeral = b;
    }

    protected void setParticle(double b) {
        isParticle = b;
    }

    protected void setPreposition(double b) {
        isPreposition = b;
    }

    protected void setPronoun(double b) {
        isPronoun = b;
    }

    protected void setOther(double b) {
        isOther = b;
    }

    protected SmallSetWordWithCategories getWordWithCategories() {
        return this;
    }

    protected String getWord() {
        return word;
    }

    protected double getArticle() {
        return isArticle;
    }

    protected double getVerb() {
        return isVerb;
    }

    protected double getPunctuation() {
        return isPunctuation;
    }

    protected double getAdjective() {
        return isAdjective;
    }

    protected double getAdverb() {
        return isAdverb;
    }

    protected double getConjunction() {
        return isConjunction;
    }

    protected double getNoun() {
        return isNoun;
    }

    protected double getNumeral() {
        return isNumeral;
    }

    protected double getParticle() {
        return isParticle;
    }

    protected double getPreposition() {
        return isPreposition;
    }

    protected double getPronoun() {
        return isPronoun;
    }

    protected double getOther() {
        return isOther;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.word != null ? this.word.hashCode() : 0);
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmallSetWordWithCategories other = (SmallSetWordWithCategories) obj;
        if ((this.word == null) ? (other.word != null) : !this.word.equals(other.word)) {
            return false;
        }
        return true;
    }
    
    protected void setProperties(int index, double value) {
        switch (index) {
            case 0:
                this.setArticle(value);
            case 1:
                this.setVerb(value);
            case 2:
                this.setPunctuation(value);
            case 3:
                this.setAdjective(value);
            case 4:
                this.setAdverb(value);
            case 5:
                this.setConjunction(value);
            case 6:
                this.setNoun(value);
            case 7:
                this.setNumeral(value);
            case 8:
                this.setParticle(value);
            case 9:
                this.setPreposition(value);
            case 10:
                this.setPronoun(value);
            case 11:
                this.setOther(value);

        }
    }

    @Override
    public String toString() {
        String s = "";
        s = s.concat(isArticle + " ");
        s = s.concat(isVerb + " ");
        s = s.concat(isPunctuation + " ");
        s = s.concat(isAdjective + " ");
        s = s.concat(isAdverb + " ");
        s = s.concat(isConjunction + " ");
        s = s.concat(isNoun + " ");
        s = s.concat(isNumeral + " ");
        s = s.concat(isParticle + " ");
        s = s.concat(isPreposition + " ");
        s = s.concat(isPronoun + " ");
        s = s.concat(isOther + " ");

        //s = s.concat(Integer.toString(ambiguity));
        return s;
    }
    
    public Map<String, Double> getListOfProperties() {
        
        Map<String, Double> properties = new LinkedHashMap();
        properties.put("isArticle", isArticle);
        properties.put("isVerb", isVerb);
        properties.put("isPunctuation", isPunctuation);
        properties.put("isAdjective", isAdjective);
        properties.put("isAdverb", isAdverb);
        properties.put("isConjunction", isConjunction);
        properties.put("isNoun", isNoun);
        properties.put("isNumeral", isNumeral);
        properties.put("isParticle", isParticle);
        properties.put("isPreposition", isPreposition);
        properties.put("isPronoun", isPronoun);
        properties.put("isOther", isOther);
        
        return properties;
    }
}
