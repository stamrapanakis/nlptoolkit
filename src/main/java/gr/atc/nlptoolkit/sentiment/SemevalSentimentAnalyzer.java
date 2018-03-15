/**
 * 
 */
package gr.atc.nlptoolkit.sentiment;

import gr.atc.nlptoolkit.utils.Tools;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.greekpos.SmallSetFunctions;
import gr.atc.nlptoolkit.greekpos.WordWithCategory;
import gr.atc.nlptoolkit.instances.TweetInstance;
import gr.atc.nlptoolkit.stylometry.EmoticonsDetector;
import java.util.HashMap;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import org.apache.commons.lang.NumberUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author SRapanakis
 */
public class SemevalSentimentAnalyzer {
    
    // Ark Tweet NLP tagger
    private static final Tagger ARK_TAGGER = new Tagger();
    
    private static final String FEATURE_LEX_NHS = "NHS_LEX_TOTAL_SCORE";
    private static final String FEATURE_LEX_140 = "140_LEX_TOTAL_SCORE";
    private static final String FEATURE_LEX_LIU = "BING_LIU_LEX_TOTAL_SCORE";
    private static final String FEATURE_LEX_SWEAR_EN = "SWEAR_WORD_EN";
    private static final String FEATURE_LEX_EMOT_NEG = "NEG_EMOTICON";
    private static final String FEATURE_LEX_EMOT_POS = "POS_EMOTICON";
    private static final String FEATURE_LEX_NEG = "NEGATION_PRESENCE";
    
    // Greek features list
    private static Map<String, Integer> LEX_SOCIAL_SENSOR;
    private static final String FEATURE_LEX_SOCIAL_SENSOR_POS = "SOCIAL_SENSOR_LEX_TOTAL_SCORE_POS";
    private static final String FEATURE_LEX_SOCIAL_SENSOR_NEUTR = "SOCIAL_SENSOR_LEX_TOTAL_SCORE_NEUTR";
    private static final String FEATURE_LEX_SOCIAL_SENSOR_NEG = "SOCIAL_SENSOR_LEX_TOTAL_SCORE_NEG";
    private static final String FEATURE_LEX_CUSTOM_POS = "CUSTOM_LEX_TOTAL_SCORE_POS";
    private static final String FEATURE_LEX_CUSTOM_NEUTR = "CUSTOM_LEX_TOTAL_SCORE_NEUTR";
    private static final String FEATURE_LEX_CUSTOM_NEG = "CUSTOM_LEX_TOTAL_SCORE_NEG";
    private static final String FEATURE_LEX_SWEAR_EL = "SWEAR_WORD_EL";
    private static final String FEATURE_INTERROGATIVE_SENTENCE = "INTERROGATIVE_PRESENCE";
    
    private static SmallSetFunctions smallSetFunctions;
    private static final List<String> NEGATIONS_EL = Tools.getGreekNegations();
    private static GreekCustomLexicon greekCustomLexicon;
    private static Map<String, Integer> LEX_CUSTOM;
    
    private final Map<String, Integer> LEX_NHS;
    private final Map<String, Integer> LEX_140;
    private final Map<String, Integer> LEX_LIU;
    
    // Contains common Twitter swear words
    private static final List<String> SWEAR_WORDS_EN = Tools.getTwitterSwearWords();
    private static final List<String> SWEAR_WORDS_EL = Tools.getGreekSwearWords();
    
    private static final List<String> NEG_EMOTICONS = Tools.getNegativeEmoticons();
    
    private static final List<String> POS_EMOTICONS = Tools.getPositiveEmoticons();
    
    private static final List<String> NEGATION = new ArrayList();
    
    private svm_model englishModel;
    private svm_model greekModel;
    
    // The path to the models folder
    private String modelDataPath;
    
    // Greek lexicon validator
    private final GreekLexiconValidator greekLexiconValidator;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SemevalSentimentAnalyzer.class);
    
    private String sentimentModelFeaturesPath;
    
    private String sentimentModelFeaturesELPath;
    
    /**
     * 
     * @param modelDataPath 
     */
    public SemevalSentimentAnalyzer(String modelDataPath) {
        
        this.modelDataPath = modelDataPath;
        LOGGER.info("modelDataPath: {}", modelDataPath);
        
        this.sentimentModelFeaturesPath = modelDataPath+"/semeval-2014-model-features";
        LOGGER.info("sentimentModelFeaturesPath: {}", sentimentModelFeaturesPath);
        
        this.sentimentModelFeaturesELPath = modelDataPath+"/greek/greek-model-features";
        LOGGER.info("sentimentModelFeaturesELPath: {}", sentimentModelFeaturesELPath);
        
        // Load the models
        try {
            String arkTaggerModelPath = modelDataPath+"/arktweetnlp/model.20120919";
            LOGGER.info("Loading Ark Twitter NLP tagger model: {}", arkTaggerModelPath);
            ARK_TAGGER.loadModel(arkTaggerModelPath);
            
            String englishSentimentModelPath = modelDataPath+"/semeval-2014-model";
            LOGGER.info("Loading English Sentiment model: {}", englishSentimentModelPath);
            englishModel = svm.svm_load_model(englishSentimentModelPath);
            
            String greekModelPath = modelDataPath+"/greek/greek-model";
            LOGGER.info("Loading Greek model: {}", greekModelPath);
            greekModel = svm.svm_load_model(greekModelPath);
        } catch (IOException ex) {
            LOGGER.error("Unable to load the model {}", ex);
        }
        
        // Instanciate the lexicon
        String unigramsPmiLexiconPath = modelDataPath+"/lexicons/unigrams-pmilexicon.txt";
        LEX_NHS = instanciateUnigramsLexico(unigramsPmiLexiconPath);
        LOGGER.info("unigramsPmiLexiconPath: {}", unigramsPmiLexiconPath);
        
        String unigramsPmiLexiconSentim140Path = modelDataPath+"/lexicons/unigrams-pmilexicon-sentim140.txt";
        LEX_140 = instanciateUnigramsLexico(unigramsPmiLexiconSentim140Path);
        LOGGER.info("unigramsPmiLexiconSentim140Path: {}", unigramsPmiLexiconSentim140Path);
        
        String bingLiuLexicoPosPath = modelDataPath+"/lexicons/positive-words.txt";
        LEX_LIU = instanciateBingLiuLexico(bingLiuLexicoPosPath, 1);
        LOGGER.info("bingLiuLexicoPosPath: {}", bingLiuLexicoPosPath);
        
        String bingLiuLexicoNegPath = modelDataPath+"/lexicons/negative-words.txt";
        LEX_LIU.putAll(instanciateBingLiuLexico(bingLiuLexicoNegPath, -1));
        LOGGER.info("bingLiuLexicoNegPath: {}", bingLiuLexicoNegPath);
        
        NEGATION.add("not");
        smallSetFunctions = new SmallSetFunctions(modelDataPath);
        
        greekCustomLexicon = new GreekCustomLexicon(modelDataPath);
        LEX_CUSTOM = greekCustomLexicon.getGreekCustomLexicon();
        greekLexiconValidator = new GreekLexiconValidator(modelDataPath);
        LEX_SOCIAL_SENSOR = greekLexiconValidator.getGreekSocialSensorLexicon();
    }

    /**
     * 
     * @param text
     * @param lang
     * @return 
     */
    public Sentiment findSentiment(String text, String lang) {
        
        if (StringUtils.isNotBlank(text)) {
            if ("en".equals(lang)) {
                
                List<String> modelFeatures = Tools.readFile(sentimentModelFeaturesPath);

                TweetInstance testInstance = new TweetInstance("", text, ARK_TAGGER);
                double predictedClass = svm.svm_predict(englishModel, createTestInstance(testInstance, modelFeatures));

                Sentiment evaluatedSentiment =  null;
                if (predictedClass == 0)  {
                    evaluatedSentiment = Sentiment.POSITIVE;
                } else if (predictedClass == 1)  {
                    evaluatedSentiment = Sentiment.NEGATIVE;
                } else if (predictedClass == 2)  {
                    evaluatedSentiment = Sentiment.NEUTRAL;
                }
                return evaluatedSentiment;
            } else if ("el".equals(lang)) {
                
                List<String> modelFeatures = Tools.readFile(sentimentModelFeaturesELPath);
                
                // Tokenize the string
                List<String> testInstanceTokens = EmoticonsDetector.tokenizeRawTweetText(text);
                
                // Preprocess tokens
                testInstanceTokens = preprocessToken(testInstanceTokens);                
                
                TweetInstance instance = new TweetInstance(testInstanceTokens);
                
                // Apply some hard-coded rules to a percentage of the negative tweets
                double predictedClass = applyRuleBasedPrediction(instance);
                if (predictedClass != 1) {
                    // Use the trained model to predict the class of the test instance
                    predictedClass = svm.svm_predict(greekModel, createTestInstanceEL(instance, modelFeatures));
                    if (predictedClass == 1)  {
                        // Apply bag of words rule
                        Map<String, GenericLexiconFeature> lexicalFeaturesMap = instance.getLexicalFeaturesMap();
    //                    LexiconFeature socialSensorLexScore = lexicalFeaturesMap.get(FEATURE_LEX_SOCIAL_SENSOR_NEG);
    //                    int socialSensorLexTotalScore = 0;
    //                    if (socialSensorLexScore != null) {
    //                        socialSensorLexTotalScore = socialSensorLexScore.getLexTotalScore();
    //                    }

                        GenericLexiconFeature customLexScore = lexicalFeaturesMap.get(FEATURE_LEX_CUSTOM_NEG);
                        int customLexTotalScore = 0;
                        if (customLexScore != null) {
                            customLexTotalScore = customLexScore.getTotalTokensScore();
                        }

                        // Apply a rule: 
                        double sum = customLexTotalScore;
                        if (sum > 0) {
                            predictedClass = 0;
                        } else if (sum == 0) {
                            // Neutral case
                            predictedClass = 2;
                        } else {
                            // Negative case
                            predictedClass = 1;
                        }
                    }
                }
                
                Sentiment evaluatedSentiment =  null;
                if (predictedClass == 0)  {
                    evaluatedSentiment = Sentiment.POSITIVE;
                } else if (predictedClass == 1)  {
                    evaluatedSentiment = Sentiment.NEGATIVE;
                } else if (predictedClass == 2)  {
                    evaluatedSentiment = Sentiment.NEUTRAL;
                }
                return evaluatedSentiment;
            }
        } else {
            LOGGER.info("Please provide text to analyse.");
        }
        
        return null;
    }
    
    /**
     * 
     * @param testInstance
     * @return 
     */
    public static double applyRuleBasedPrediction(TweetInstance testInstance) {
        
        int negativePrediction = 1;
        
        // Rule 1. The tweet contains a swear word that has only negative meaning (e.g. crap), not fuck
//        boolean swearingStrongWordCase = false;
        List<String> tokensList = testInstance.getTokensList();
        for (String token:tokensList) {
            
            if (isSwearWordGreek(token)) {
                return negativePrediction;
            }
        }
        
//        if (swearingStrongWordCase) {
//            // TODO Remove test code
//            if (testInstance.sentiment.getValue() != 1) {
//                LOGGER.debug("Actual sentiment: {}, {}", testInstance.sentiment.getValue(), testInstance.tweetText);
//            }
//            return negativePrediction;
//        }
        
        // Rule 2. Examine the role of the "not" word
        
        return -1;
    }    
    
    /**
     * 
     * @param tweetText
     * @return 
     */
    public List getNominalsListEN(String tweetText){
        List result = null;
        if ((ARK_TAGGER != null) && (tweetText != null) && (StringUtils.isNotBlank(tweetText))) {
            List<Tagger.TaggedToken> taggedTokens = ARK_TAGGER.tokenizeAndTag(tweetText);
            result = new ArrayList<String>();
            
            for (Tagger.TaggedToken tweetToken:taggedTokens) {
                String tag = tweetToken.tag;
                
                // Common noun case: N
                // ^ proper noun
                // S nominal + possessive
                // Z proper noun + possessive
                if ("N".equals(tag) || "^".equals(tag) || "S".equals(tag) || "Z".equals(tag)) {
                    result.add(tweetToken.token);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Find the nominals of greek text (EL)
     * @param tweetText Text to examine
     * @return 
     */
    public List getNominalsListEL(String tweetText){
        List result = null;
        if (StringUtils.isNotBlank(tweetText)) {
            
            result = new ArrayList<String>();
            
            // Tokenize the string
            List<String> tokens = EmoticonsDetector.tokenizeRawTweetText(tweetText);
            StringBuilder tokensWithSpace = new StringBuilder();
            for (String token:tokens){
                tokensWithSpace.append(token);
                tokensWithSpace.append(" ");
            }

            List<WordWithCategory> list = smallSetFunctions.smallSetClassifyString(tokensWithSpace.toString());
            for (WordWithCategory word:list){
                if ("noun".equals(word.getCategory())){
                    result.add(word.getWord());
                }
            }
        }
        
        return result;
    }    
    
    /**
     * 
     * @param nhsLexico
     * @return 
     */
    private static Map<String, Integer> instanciateUnigramsLexico(String nhsLexico) {
        String nhsLexicoPath = nhsLexico;
        
        Map<String, Integer> lexico = new HashMap();
        Charset charset = Charset.forName("UTF-8");
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(nhsLexicoPath), charset));
            
            String line = null;
            while((line = br.readLine()) != null) {
                String[] splits = line.split("\t");
                // Add uid (split[0]), text (split[3])
                String word = splits[0];
                // Parse the string e.g. 5.116 to an integer
                Integer score = Integer.parseInt(splits[1].replace(".", ""));
                
                if (!lexico.containsKey(word)) {
                    lexico.put(word, score);
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("Throwing FileNotFoundException ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Throwing UnsupportedEncodingException ", ex);
        } catch (IOException ex) {
            LOGGER.error("Throwing IOException ", ex);
        }
        
        return lexico;
    }
    
    /**
     * 
     * @param nhsLexico
     * @param score
     * @return 
     */
    private static Map<String, Integer> instanciateBingLiuLexico(String lexicon, int score) {
        String nhsLexicoPath = lexicon;
        
        Map<String, Integer> lexico = new HashMap();
        Charset charset = Charset.forName("UTF-8");
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(nhsLexicoPath), charset));
            
            String line = null;
            while((line = br.readLine()) != null) {
                String word = line;
                if (!lexico.containsKey(word)) {
                    lexico.put(word, score);
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("Throwing FileNotFoundException ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Throwing UnsupportedEncodingException ", ex);
        } catch (IOException ex) {
            LOGGER.error("Throwing IOException ", ex);
        }
        
        return lexico;
    }    

    /**
     * 
     * @param tweetInstance
     * @param modelFeaturesList
     * @return 
     */
    public svm_node[] createTestInstance(TweetInstance tweetInstance, List<String> modelFeaturesList) {
        
        // The tokens of the tweets
        List<String> ngrams = new ArrayList();
        
        // Add the unigrams
        List<String> unigrams = tweetInstance.getTokensList();
        ngrams.addAll(unigrams);
        // Add the bigrams
        ngrams.addAll(getBigramsFromList(unigrams));
        
        List<svm_node> attributesList = new ArrayList();
        
        for (int k = 0; k < modelFeaturesList.size(); k++) {
            // If the k-th attribute is contained in the tweets token list
            // store the attribute index in a list
            if (ngrams.contains(modelFeaturesList.get(k))) {
                int attribute = k+1;
                svm_node node = new svm_node();
                node.index = attribute;
                node.value = 1;
                
                attributesList.add(node);
            }
        }
        
        GenericLexiconFeature lexicoNHSUnigrams = useUnigramsLexicon(unigrams, LEX_NHS);
        GenericLexiconFeature lexico140Unigrams = useUnigramsLexicon(unigrams, LEX_140);
        GenericLexiconFeature lexicoLiuUnigrams = useUnigramsLexicon(unigrams, LEX_LIU);
        
        int nhsLexTotalScore = lexicoNHSUnigrams.getTotalTokensScore();
        if (nhsLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_NHS);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        }
        
        int senti140LexTotalScore = lexico140Unigrams.getTotalTokensScore();
        if (senti140LexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_140);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        }
        
        int bingLiuLexTotalScore = lexicoLiuUnigrams.getTotalTokensScore();
        if (bingLiuLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_LIU);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);
        }
        
        boolean containsSwearingWord = false;
        
        for (String unigram:unigrams){
            for (String swearWord:SWEAR_WORDS_EN) {
                if (unigram.equals(swearWord)) {
                    containsSwearingWord = true;
                    break;
                }
            }
        }
        
        if (containsSwearingWord) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SWEAR_EN);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }
        
        boolean containsNegEmoticon = false;
        
        for (String unigram:unigrams){
            if (NEG_EMOTICONS.contains(unigram)) {
                containsNegEmoticon = true;
                break;
            }
        }
        
        if (containsNegEmoticon) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_EMOT_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsPosEmoticon = false;
        
        for (String unigram:unigrams){
            if (POS_EMOTICONS.contains(unigram)) {
                containsPosEmoticon = true;
                break;
            }
        }
        
        if (containsPosEmoticon) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_EMOT_POS);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsNegation = false;
        
        for (String unigram:unigrams){
            if (NEGATION.contains(unigram)) {
                containsNegation = true;
                break;
            }
        }
        
        if (containsNegation) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }
        
        svm_node[] attributes = new svm_node[attributesList.size()];
        for (int j = 0; j < attributesList.size(); j++) {
            attributes[j] = attributesList.get(j);
        }
        
        return attributes;
    }
    
    /**
     * 
     * @param tweetInstance
     * @param modelFeaturesList
     * @return 
     */
    public svm_node[] createTestInstanceEL(TweetInstance tweetInstance, List<String> modelFeaturesList) {
        
        // Preprocess the testing tweets
        String tweetText = tweetInstance.getTweetText();

        // Tokenize the string
        List<String> tokens = EmoticonsDetector.tokenizeRawTweetText(tweetText);

        // Contains both unigrams and bigrams
        List<String> ngrams = new ArrayList<String>();
        
        List<String> unigrams = preprocessToken(tokens);
        
        // Add the unigrams
        ngrams.addAll(unigrams);
        // Add the bigrams
        ngrams.addAll(getBigramsFromList(unigrams));
        
        List<svm_node> attributesList = new ArrayList<svm_node>();
        
        for (int k = 0; k < modelFeaturesList.size(); k++) {
            // If the k-th attribute is contained in the tweets token list
            // store the attribute index in a list
            if (ngrams.contains(modelFeaturesList.get(k))) {
                int attribute = k+1;
                svm_node node = new svm_node();
                node.index = attribute;
                node.value = 1;
                
                attributesList.add(node);
            }
        }
        
        GenericLexiconFeature socialSensorLex = useUnigramsLexiconEL(unigrams, LEX_SOCIAL_SENSOR);
        
        int socialSensorLexTotalScore = socialSensorLex.getTotalTokensScore();
        if (socialSensorLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SOCIAL_SENSOR_POS);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        } else if (socialSensorLexTotalScore == 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SOCIAL_SENSOR_NEUTR);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        } else {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SOCIAL_SENSOR_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);             
        }
        
        GenericLexiconFeature customPosUnigramsLex = useUnigramsLexiconEL(unigrams, LEX_CUSTOM);
        customPosUnigramsLex.setFeature(FEATURE_LEX_CUSTOM_NEG);
        tweetInstance.updateLexiconFeatures(customPosUnigramsLex);        
        
        int customLexTotalScore = customPosUnigramsLex.getTotalTokensScore();
        if (customLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_CUSTOM_POS);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        } else if (customLexTotalScore == 0) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_CUSTOM_NEUTR);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);
        } else {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_CUSTOM_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);
        }
        
        boolean containsSwearingWordEN = false;
        
        for (String unigram:unigrams){
            for (String swearWord:SWEAR_WORDS_EN) {
                if (unigram.equals(swearWord)) {
                    containsSwearingWordEN = true;
                    break;
                }
            }
        }
        
        if (containsSwearingWordEN) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SWEAR_EN);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);
        }
        
        boolean containsSwearingWordEL = false;
        
        for (String unigram:unigrams){
            
            if (isSwearWordGreek(unigram)) {
                containsSwearingWordEL = true;
            }            
        }
        
        if (containsSwearingWordEL) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_SWEAR_EL);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);
        }
        
        boolean containsNegEmoticon = false;
        
        for (String unigram:unigrams){
            if (NEG_EMOTICONS.contains(unigram)) {
                containsNegEmoticon = true;
                break;
            }
        }
        
        if (containsNegEmoticon) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_EMOT_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsPosEmoticon = false;
        
        for (String unigram:unigrams){
            if (POS_EMOTICONS.contains(unigram)) {
                containsPosEmoticon = true;
                break;
            }
        }
        
        if (containsPosEmoticon) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_EMOT_POS);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsNegation = false;
        
        for (String unigram:unigrams){
            if (NEGATIONS_EL.contains(unigram)) {
                containsNegation = true;
                break;
            }
        }
        
        if (containsNegation) {
            int k = modelFeaturesList.indexOf(FEATURE_LEX_NEG);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }
        
        boolean isInterrogative = false;
        
        for (String unigram:unigrams){
            if ("?".equals(unigram) || ";".equals(unigram)) {
                isInterrogative = true;
            }
        }
        
        if (isInterrogative) {
            int k = modelFeaturesList.indexOf(FEATURE_INTERROGATIVE_SENTENCE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }
        /*
        boolean hasExlamationMark = false;
        
        for (String unigram:unigrams){
            if ("!".equals(unigram) || "!!".equals(unigram) || "!!!".equals(unigram) || "!!!!".equals(unigram)) {
                hasExlamationMark = true;
            }
        }
        
        if (hasExlamationMark) {
            int k = modelFeaturesList.indexOf(FEATURE_EXLAMATION_MARK);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }*/
        
        svm_node[] attributes = new svm_node[attributesList.size()];
        for (int j = 0; j < attributesList.size(); j++) {
            attributes[j] = attributesList.get(j);
        }
        
        return attributes;
    }
    
    private static List<String> getTokensToRemove() {
        List<String> commonTweetTokens = new ArrayList<String>();
        
        commonTweetTokens.add("της");
        commonTweetTokens.add("του");
        commonTweetTokens.add("τους");
        commonTweetTokens.add("τα");
        commonTweetTokens.add("το");
        commonTweetTokens.add("τον");
        commonTweetTokens.add("τι");
        commonTweetTokens.add("τις");
        commonTweetTokens.add("τη");
        commonTweetTokens.add("την");
        commonTweetTokens.add("των");
        
        commonTweetTokens.add("στην");
        commonTweetTokens.add("στη");
        commonTweetTokens.add("στο");
        commonTweetTokens.add("στον");
        commonTweetTokens.add("στα");
        commonTweetTokens.add("στις");
        
        commonTweetTokens.add("με");
        commonTweetTokens.add("σε");
        commonTweetTokens.add("θα");
        
        commonTweetTokens.add("και");
        commonTweetTokens.add("να");
        commonTweetTokens.add("για");
        
        commonTweetTokens.add("οι");
        commonTweetTokens.add("από");
        commonTweetTokens.add("που");
        
        commonTweetTokens.add("ότι");
        commonTweetTokens.add("οτι");
        
        commonTweetTokens.add("χτές");
        commonTweetTokens.add("χτες");
        commonTweetTokens.add("σήμερα");
        commonTweetTokens.add("σημερα");
        commonTweetTokens.add("αύριο");
        commonTweetTokens.add("αυριο");
        commonTweetTokens.add("μεθαύριο");
        commonTweetTokens.add("μεθαυριο");
        
        return commonTweetTokens;
    }    

    /**
     * 
     * @param tokens 
     */
    private List<String> preprocessToken(List<String> tokens) {
        // Investigate the tags that we are interested in (e.g. emoticons)
        List<String> unigrams = new ArrayList<String>();
        
        // Ignore common tokens
        List<String> commonTweetTokens = getTokensToRemove();
        
        for (String token:tokens){
            
            // Filter (ignore) certain tokens from the feature vector
            boolean ignoreTweetTextToken = false;
            
            String tweetTextToken = token;
            
            // Remove the links, @ mentions
            if (tweetTextToken.startsWith("http://") || tweetTextToken.startsWith("https://") ||
                    tweetTextToken.startsWith("@")) {
                ignoreTweetTextToken = true;
            }/*
            else if (tweetTextToken.startsWith("#")) {
            if (tweetTextToken.length() > 1) {
            tweetTextToken = tweetTextToken.substring(1, tweetTextToken.length());
            }
            }*/
            // Remove tokens of size 1
            else if ((tweetTextToken.length() == 1) && (!";".equals(tweetTextToken))
                    && (!"?".equals(tweetTextToken))) {
                ignoreTweetTextToken = true;
            }
            // Ignore numeric tokens
            else if (NumberUtils.isNumber(tweetTextToken)){
                ignoreTweetTextToken = true;
            }
            // TODO Create a regex and moe this code
            else if (tweetTextToken.matches(".*\\d.*")) {
                
                // Try again
                if (NumberUtils.isNumber(tweetTextToken.replace(",", "."))) {
                    ignoreTweetTextToken = true;
                } else if (tweetTextToken.contains("%") || tweetTextToken.contains("€")) {
                    ignoreTweetTextToken = true;
                } else {
                    if (tweetTextToken.startsWith("+") || tweetTextToken.startsWith("-")) {
                        ignoreTweetTextToken = true;
                    }
                }
            }
            // Ignore punctuation
            else if ("..".equals(tweetTextToken) || "...".equals(tweetTextToken) ||
                    "....".equals(tweetTextToken) ||".....".equals(tweetTextToken) ||
                    "......".equals(tweetTextToken) || ",...".equals(tweetTextToken)){
                ignoreTweetTextToken = true;
            }
            // Ignore special characters for Weka
            else if (tweetTextToken.contains("{") || tweetTextToken.contains(",")) {
                ignoreTweetTextToken = true;
            }
            
            // Fix tokenization
            if (tweetTextToken.startsWith("«") && (tweetTextToken.length() > 1)) {
                tweetTextToken = tweetTextToken.substring(1, tweetTextToken.length());
            }
            if (tweetTextToken.endsWith("»") && (tweetTextToken.length() > 1)) {
                tweetTextToken = tweetTextToken.substring(tweetTextToken.length()-1, tweetTextToken.length());
            }            
            
            if (!ignoreTweetTextToken) {
                
                // Lower Case - Convert the tweets to lower case
                String lowerCaseToken = tweetTextToken.toLowerCase();
                if (!commonTweetTokens.contains(lowerCaseToken)) {
                    unigrams.add(lowerCaseToken);
                }
            }
        }
        
        return unigrams;
    }
    
    /**
     * 
     * @param tweetSentence
     * @return 
     */
    private static List<String> getBigramsFromList(List tweetSentence) {
        List<String> bigramsList = new ArrayList();
        
        // Consider as bigrams 2 consecutive words in the list of tokens
        for (int i = 0; i+1 < tweetSentence.size(); i++) {
            bigramsList.add(tweetSentence.get(i)+" "+tweetSentence.get(i+1));
        }
        
        return bigramsList;
    }
    
    /**
     * 
     * @param token
     * @return 
     */
    private static boolean isSwearWordGreek(String token) {
        
        String wordWithoutAccent = token.replace("ά", "α");
        wordWithoutAccent = wordWithoutAccent.replace("έ", "ε");
        wordWithoutAccent = wordWithoutAccent.replace("ί", "ι");
        wordWithoutAccent = wordWithoutAccent.replace("ή", "η");
        wordWithoutAccent = wordWithoutAccent.replace("ύ", "υ");
        wordWithoutAccent = wordWithoutAccent.replace("ό", "ο");
        wordWithoutAccent = wordWithoutAccent.replace("ώ", "ω");
        
        wordWithoutAccent = wordWithoutAccent.replace("ϊ", "ι");
        wordWithoutAccent = wordWithoutAccent.replace("ϋ", "υ");
        
        for (String grekSwearWord:SWEAR_WORDS_EL) {
            if (StringUtils.containsIgnoreCase(wordWithoutAccent, grekSwearWord)) {
                return true;
            }
        }
        
        return false;
    }    

    /**
     * 
     * @param tweetSentence
     * @param lexicon
     * @return 
     */
    private static GenericLexiconFeature useUnigramsLexicon(final List<String> tweetSentence, Map<String, Integer> lexicon) {
        
        // Total score of the tokens
        int totalTokensScore = 0;
        
        for (String token:tweetSentence) {
            if (lexicon.containsKey(token)) {
                int score = lexicon.get(token);
                totalTokensScore += score;
            }
        }
        
        GenericLexiconFeature result = new GenericLexiconFeature();
        result.setTotalTokensScore(totalTokensScore);
        
        return result;
    }
    
    /**
     * 
     * @param tweetSentence 
     * @param lexicon 
     * @return  
     */
    public static GenericLexiconFeature useUnigramsLexiconEL(final List<String> tweetSentence, 
            final Map<String, Integer> lexicon) {
        
        // Total score of the tokens
        int totalTokensScore = 0;
        // Maximal score of the tokens
        int maxTokensScore = 0;
        
        for (String token:tweetSentence) {
            
            String wordWithoutAccent = token.replace("ά", "α");
            wordWithoutAccent = wordWithoutAccent.replace("έ", "ε");
            wordWithoutAccent = wordWithoutAccent.replace("ί", "ι");
            wordWithoutAccent = wordWithoutAccent.replace("ή", "η");
            wordWithoutAccent = wordWithoutAccent.replace("ύ", "υ");
            wordWithoutAccent = wordWithoutAccent.replace("ό", "ο");
            wordWithoutAccent = wordWithoutAccent.replace("ώ", "ω");

            wordWithoutAccent = wordWithoutAccent.replace("ϊ", "ι");
            wordWithoutAccent = wordWithoutAccent.replace("ϋ", "υ");             
            
            for (String lexiconEntry:lexicon.keySet()) {
                if (wordWithoutAccent.startsWith(lexiconEntry)) {
                    int score = lexicon.get(lexiconEntry);
                    totalTokensScore += score;
                    // Get the maximum token
                    if (score > maxTokensScore) {
                        maxTokensScore = score;
                    }
                }
            }
            
        }
        
        GenericLexiconFeature result = new GenericLexiconFeature();
        result.setTotalTokensScore(totalTokensScore);
        
        return result;
    }
    
}

