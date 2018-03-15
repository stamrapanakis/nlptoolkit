/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.sentiment;

/**
 * Represents a lexicon feature
 * 
 * @author SRapanakis
 */
public class LexiconFeature {

    private String feature;

    private int lexTotalPos;
    private int lexTotalScore;
    private int lexScoreMax;
    private int lexScoreLastPos;

    /**
     * 
     * @param feature
     * @param lexTotalPos
     * @param lexTotalScore
     * @param lexScoreMax
     * @param lexScoreLastPos 
     */
    public LexiconFeature(String feature, int lexTotalPos, int lexTotalScore, int lexScoreMax, int lexScoreLastPos) {
        this.feature = feature;
        this.lexTotalPos = lexTotalPos;
        this.lexTotalScore = lexTotalScore;
        this.lexScoreMax = lexScoreMax;
        this.lexScoreLastPos = lexScoreLastPos;
    }

    public String getFeature() {
        return feature;
    }

    public int getLexTotalPos() {
        return lexTotalPos;
    }

    public int getLexTotalScore() {
        return lexTotalScore;
    }

    public int getLexScoreMax() {
        return lexScoreMax;
    }

    public int getLexScoreLastPos() {
        return lexScoreLastPos;
    }
}
