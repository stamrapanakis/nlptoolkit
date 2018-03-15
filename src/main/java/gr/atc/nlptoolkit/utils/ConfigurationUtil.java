/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.utils;

import java.util.ResourceBundle;

/**
 *
 * @author SRapanakis
 */
public class ConfigurationUtil {
    
    private static final ResourceBundle RESOURCE_BUNDLE;

    private ConfigurationUtil() {
    }
    
    static {
        RESOURCE_BUNDLE = ResourceBundle.getBundle("nlptoolkit");
    }
    
    public static String getModelsFilePath() {

        return RESOURCE_BUNDLE.getString("data-path");
    }
    
    public static String getSentimentModel() {
        return RESOURCE_BUNDLE.getString("data-path")+"/semeval-2014-model";
    }
    
    public static String getSentimentModelEL() {
        return RESOURCE_BUNDLE.getString("data-path")+"/greek/greek-model";
    }    
    
    public static String getSentimentModelFeatures() {
        return RESOURCE_BUNDLE.getString("data-path")+"/semeval-2014-model-features";
    }
    
    public static String getSentimentModelFeaturesEL() {
        return RESOURCE_BUNDLE.getString("data-path")+"/greek/greek-model-features";
    }    
    
    public static String getArkTaggerModel() {
        return RESOURCE_BUNDLE.getString("data-path")+"/arktweetnlp/model.20120919";
    }
    
    public static String getTestingDataTwitterFilePath() {
        return RESOURCE_BUNDLE.getString("data-path")+"/semeval2014/testing/twitter-test-gold-B-downloaded.tsv";
    }
    
    public static String getBingLiuLexicoPos() {
        return RESOURCE_BUNDLE.getString("data-path")+"/lexicons/positive-words.txt";
    }
    
    public static String getBingLiuLexicoNeg() {
        return RESOURCE_BUNDLE.getString("data-path")+"/lexicons/negative-words.txt";
    }
    
    public static String getUnigramsPmiLexicon() {
        return RESOURCE_BUNDLE.getString("data-path")+"/lexicons/unigrams-pmilexicon.txt";
    }
    
    public static String getUnigramsPmiLexiconSentim140() {
        return RESOURCE_BUNDLE.getString("data-path")+"/lexicons/unigrams-pmilexicon-sentim140.txt";
    }
    
    public static String getGreekSentimentLexicon() {
        return RESOURCE_BUNDLE.getString("data-path")+"/greek/greek_sentiment_lexicon.tsv";
    }
    
    public static String getGreekResourcesFolder() {
        return RESOURCE_BUNDLE.getString("data-path")+"/greek";
    }    
}
