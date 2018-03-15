/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.instances;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.sentiment.GenericLexiconFeature;
import gr.atc.nlptoolkit.utils.Tools;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class TweetInstance {
        
    private String tweetId;
    private String tweetText;
    private List<String> tokensList = new ArrayList<String>();
    // Represents a subset of the tokens that are used for the analysis of the tweet
    private List<String> analyzedTokensList = new ArrayList<String>();
    // Holds a Tagger.TaggedToken list of the tokens
    private List<Tagger.TaggedToken> taggedTokenList = new ArrayList<Tagger.TaggedToken>();
    // List of nominals (e.g. nouns) unigrams
    private List<String> nominalTokensUnigrams =  new ArrayList<String>();
    // List of nominals (e.g. nouns) bigrams
    private List<String> nominalTokensBigrams =  new ArrayList<String>();
    
    // Applies to learning tweets only
    Map<String, GenericLexiconFeature> lexicalFeaturesMap = new HashMap<String, GenericLexiconFeature>();
    
    /**
     * 
     * @param tweetId
     * @param tweetText
     * @param arkTagger 
     */
    public TweetInstance(String tweetId, String tweetText, Tagger arkTagger) {
        this.tweetId = tweetId;
        this.tweetText = tweetText;

        if ((arkTagger != null) && (tweetText != null) && (StringUtils.isNotBlank(tweetText))) {
            List<Tagger.TaggedToken> taggedTokens = arkTagger.tokenizeAndTag(tweetText);
            for (Tagger.TaggedToken tweetToken:taggedTokens) {
                String tweetTextToken = tweetToken.token.toLowerCase();
                this.tokensList.add(tweetTextToken);
            }
            this.taggedTokenList = taggedTokens;
        } else {
            this.tokensList = new ArrayList<String>();
        }
    }
    
    /**
     * 
     * @param tweetsList
     */
    public TweetInstance(List<String> tweetsList) {
        this.tokensList = tweetsList;
        this.tweetText= Tools.prettyPrintList(tweetsList);
    }
    
    public String getTweetId() {
        return tweetId;
    }
    
    public void setTweetId(String tweetId) {
        this.tweetId = tweetId;
    }

    public String getTweetText() {
        return tweetText;
    }
    
    public void setTweetText(String tweetText) {
        this.tweetText = tweetText;
    }
    
    public List<String> getTokensList() {
        return tokensList;
    }

    public List<String> getAnalyzedTokensList() {
        return analyzedTokensList;
    }

    public void setAnalyzedTokensList(List<String> analyzedTokensList) {
        this.analyzedTokensList = analyzedTokensList;
    }

    public void setTokensList(List<String> tokensList) {
        this.tokensList = tokensList;
    }

    public List<Tagger.TaggedToken> getTaggedTokenList() {
        return taggedTokenList;
    }

    public void setTaggedTokenList(List<Tagger.TaggedToken> taggedTokenList) {
        this.taggedTokenList = taggedTokenList;
    }
    
    /**
     * Get the nominal tokens (nominal unigrams)
     * 
     * @return 
     */
    public List<String> getNominalTokensUnigrams() {
        return nominalTokensUnigrams;
    }

    public void setNominalTokensUnigrams(List<String> nominalTokensUnigrams) {
        this.nominalTokensUnigrams = nominalTokensUnigrams;
    }

    /**
     * Get the nominal bigrams
     * 
     * @return 
     */
    public List<String> getNominalTokensBigrams() {
        return nominalTokensBigrams;
    }

    public void setNominalTokensBigrams(List<String> nominalTokensBigrams) {
        this.nominalTokensBigrams = nominalTokensBigrams;
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, GenericLexiconFeature> getLexicalFeaturesMap() {
        return lexicalFeaturesMap;
    }

    /**
     * 
     * @param lexFeature 
     */
    public void updateLexiconFeatures(GenericLexiconFeature lexFeature) {
        lexicalFeaturesMap.put(lexFeature.getFeature(), lexFeature);
    }    
    
    @Override
    public String toString() {
        return tweetText;
    }
    
}
    