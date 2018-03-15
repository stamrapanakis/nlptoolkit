
package gr.atc.nlptoolkit.sentiment;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.instances.TweetInstance;
import gr.atc.nlptoolkit.utils.LDATools;
import gr.atc.nlptoolkit.utils.Tools;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.DOTALL;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
  * Semeval 2014 Subtask B: Message Polarity Classification: Given a message, 
  * decide whether the message is of positive, negative, or neutral sentiment.
  * 
  * For messages conveying both a positive and negative sentiment, whichever is 
  * the stronger sentiment should be chosen.
  * 
 * @author SRapanakis
 */
public class MessagePolarityClassification {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(MessagePolarityClassification.class);

    private static String svmModelFileName;
    private static String svmFeaturesFileName;
    private static String arkNLPModel;
    private static String unigramsPmiLexicon;
    private static String unigramsPmiLexiconSentim140;
    private static String bingLiuLexicoPos;
    private static String bingLiuLexicoNeg;
    
    private static Map<String, Integer> nhsUnigramsLexicon;
    private static Map<String, Integer> senti140UnigramsLexicon;
    private static Map<String, Integer> bingLiuUnigramsLexicon;
    
    private static final String NHS_LEXICON_TOTAL_SCORE = "NHS_LEX_TOTAL_SCORE";
    private static final String SENTI_140LEXICON_TOTAL_SCORE = "140_LEX_TOTAL_SCORE";
    private static final String BINGLIU_LEXICON_TOTAL_SCORE = "BING_LIU_LEX_TOTAL_SCORE";
    private static final String SWEAR_WORD_FEATURE = "SWEAR_WORD";
    private static final String NEG_EMOTICONS_FEATURE = "NEG_EMOTICON";
    private static final String POS_EMOTICONS_FEATURE = "POS_EMOTICON";
    private static final String CONTAINS_NEGATION_FEATURE = "NEGATION_PRESENCE";
    
    // Contains common Twitter swear words
    private static final List<String> SWEARWORDS = Tools.getTwitterSwearWords();
    
    private static final List<String> NEGATIVE_EMOTICONS = Tools.getNegativeEmoticons();
    
    private static final List<String> POSITIVE_EMOTICONS = Tools.getPositiveEmoticons();
    
    private static final List<String> NEGATION_LIST = new ArrayList();
    
    /*
    For training data use the twitter-train-cleansed-B.tsv file. It contains
    3 categories: positive, negative and neutral and a total 9684 tweets.

    For testing purposes use the files:

        twitter-test-input-B.tsv
        twitter-test-gold-B.tsv

    as described in the README_TEXT.txt provided in the relevant folder.        
    */
    
    /**
     * 
     * @param modelDataPath Path that contains the model files
     */
    public MessagePolarityClassification(String modelDataPath){
        setModels(modelDataPath);
    }

    private void setModels(String modelDataPath) {
        // Instanciate the lexicon
        svmModelFileName = modelDataPath+"/semeval-2014-model";
        svmFeaturesFileName = modelDataPath+"/semeval-2014-model-features";
        arkNLPModel = modelDataPath+"/arktweetnlp/model.20120919";
        unigramsPmiLexicon = modelDataPath+"/lexicons/unigrams-pmilexicon.txt";
        unigramsPmiLexiconSentim140 = modelDataPath+"/lexicons/unigrams-pmilexicon-sentim140.txt";
        bingLiuLexicoPos = modelDataPath+"/lexicons/positive-words.txt";
        bingLiuLexicoNeg = modelDataPath+"/lexicons/negative-words.txt";
        
        nhsUnigramsLexicon = instanciateUnigramsLexico(unigramsPmiLexicon);
        senti140UnigramsLexicon = instanciateUnigramsLexico(unigramsPmiLexiconSentim140);
        bingLiuUnigramsLexicon = instanciateBingLiuLexico(bingLiuLexicoPos, 1);
        bingLiuUnigramsLexicon.putAll(instanciateBingLiuLexico(bingLiuLexicoNeg, -1));
        NEGATION_LIST.add("not");
    }
    
    public static void main(String [] args) {
        
        // Instanciate Ark Tweet NLP tagger used for tweet tokenization and preprocessing
        Tagger arkTagger = new Tagger();
        try {
            arkTagger.loadModel(arkNLPModel);
        } catch (IOException ex) {
            LOGGER.error("Unable to load tagger model {}", ex);
        }
        
        // Read the file that contains the downloaded data (twitter-train-cleansed-B-downloaded.tsv)
        // Store them in a map<tweetId, tweetContent>
        String trainingDataTwitterFilePath = "E:/repository/semeval2014/training/twitter-train-cleansed-B-downloaded.tsv";
        List<SentimentTweetInstance> learningTweets = getTweetsFromFile(trainingDataTwitterFilePath, null, false);
        
        // Preprocess the tweets
        // Do not apply the language detection
        
        // Check if the model file exists
        boolean createLearningModelFile = false;
        // Generate a model that will use svm with unigrams as a single feature
        svm_model learningModel = null;
        try {
            learningModel = svm.svm_load_model(svmModelFileName);
        } catch (IOException ex) {
            LOGGER.error("Unable to load the learning model {}", ex);
            createLearningModelFile = true;
        }
        
        List<String> modelFeaturesList = Tools.readFile(svmFeaturesFileName);
        if (createLearningModelFile || (modelFeaturesList.isEmpty())) {
            learningModel = generateFeaturesList(learningTweets, arkTagger);
            modelFeaturesList = Tools.readFile(svmFeaturesFileName);
        }
        
        // Read the file twitter-test-gold-B-downloaded.tsv that contains tweets for testing purposes
        String testingDataTwitterFilePath = "E:/repository/semeval2014/training/twitter-test-gold-B-downloaded.tsv";
        List<SentimentTweetInstance> testingTweets = getTweetsFromFile(testingDataTwitterFilePath, arkTagger, false);
        
        MessagePolarityClassification msgPolClassiffication = new MessagePolarityClassification("E:/repository");
        msgPolClassiffication.printResults(testingTweets, learningModel, modelFeaturesList);
    }
    
    /**
     * 
     * @param testList
     * @param learningModel 
     * @param modelFeaturesList
     */
    private void printResults(List<SentimentTweetInstance> testList, svm_model learningModel, List<String> modelFeaturesList) {
        
        Map<String, Sentiment> trueTweetMap = new HashMap<String, Sentiment>();
        Map<String, Sentiment> evaluationTweetMap = new HashMap<String, Sentiment>();        
        
        LOGGER.debug("Predicted class results.");
        for (SentimentTweetInstance testInstance:testList) {
            
            trueTweetMap.put(testInstance.getTweetId(), testInstance.getSentiment());
            
            // Apply some hard-coded rules to a percentage of the negative tweets
            double predictedClass = applyRuleBasedPrediction(testInstance);
            if (predictedClass != 1) {
                // Use the trained model to predict the class of the test instance
                predictedClass = svm.svm_predict(learningModel, createTestInstance(testInstance, modelFeaturesList));
            }
            
            Sentiment evaluatedSentiment =  null;
            if (predictedClass == 0)  {
                evaluatedSentiment = Sentiment.POSITIVE;
            } else if (predictedClass == 1)  {
                evaluatedSentiment = Sentiment.NEGATIVE;
            } else if (predictedClass == 2)  {
                evaluatedSentiment = Sentiment.NEUTRAL;
            }
            evaluationTweetMap.put(testInstance.getTweetId(), evaluatedSentiment);
            
        }
        
        EvaluationMetrics evalMetrics = new EvaluationMetrics(trueTweetMap, evaluationTweetMap);
        
        double posPrecision = evalMetrics.evaluatePrecisionPerCategory(Sentiment.POSITIVE);
        double negPrecision = evalMetrics.evaluatePrecisionPerCategory(Sentiment.NEGATIVE);
        double neuPrecision = evalMetrics.evaluatePrecisionPerCategory(Sentiment.NEUTRAL);
        
        DecimalFormat df = new DecimalFormat("#.##");
        LOGGER.debug("Precision pos: {}%, neg: {}%, neutral: {}%", new Object[] {
            df.format(100*posPrecision), df.format(100*negPrecision), df.format(100*neuPrecision) });
        
        double posRecall = evalMetrics.evaluateRecallPerCategory(Sentiment.POSITIVE);
        double negRecall = evalMetrics.evaluateRecallPerCategory(Sentiment.NEGATIVE);
        double neuRecall = evalMetrics.evaluateRecallPerCategory(Sentiment.NEUTRAL);
        
        LOGGER.debug("Recall pos: {}%, neg: {}%, neutral: {}%", new Object[] {
            df.format(100*posRecall), df.format(100*negRecall), df.format(100*neuRecall) });
        
        double posFMeasure = evalMetrics.calculateFMeasure(posPrecision, posRecall);
        double negFMeasure = evalMetrics.calculateFMeasure(negPrecision, negRecall);
        double neuFMeasure = evalMetrics.calculateFMeasure(neuPrecision, neuRecall);
        
        LOGGER.debug("FMeasure pos: {}%, neg: {}%, neutral: {}%", new Object[] {
            df.format(100*posFMeasure), df.format(100*negFMeasure), df.format(100*neuFMeasure) });
    }
    
    /**
     * 
     * @param nhsLexico
     * @return 
     */
    private static Map<String, Integer> instanciateUnigramsLexico(String nhsLexico) {
        String nhsLexicoPath = nhsLexico;
        
        Map<String, Integer> lexico = new HashMap<String, Integer>();
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
        
        Map<String, Integer> lexico = new HashMap<String, Integer>();
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
     * The learning file has the following format:
     * 
     *  264183816548130816  15140428    positive    Gas by my house hit $3.39!!!! I'm going to Chapel Hill on Sat. :)
     *  263405084770172928  591166521	negative    Theo Walcott is still shit, watch Rafa and Johnny deal with him on Saturday.
     * 
     * @param filePath
     * @param arkTagger
     * @param addOnlyNegative
     * @return 
     */
    private static List<SentimentTweetInstance> getTweetsFromFile(String filePath, Tagger arkTagger, boolean addOnlyNegative) {
        
        Map<String, SentimentTweetInstance> resultMap = new TreeMap<String, SentimentTweetInstance>();
        Charset charset = Charset.forName("UTF-8");
        int lineCounter = 0;
        int activeTweets = 0;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), charset));
            
            String line = null;
            while((line = br.readLine()) != null) {
                lineCounter++;
                String[] splits = line.split("\t");
                // Add uid (split[0]), text (split[3])
                String uid = splits[0];
                String sentiment = splits[2];
                String text = splits[3];
                if ("Not Available".equals(text)) {
                    continue;
                }
                activeTweets++;
                if (!resultMap.containsKey(uid)) {
                    Sentiment sentimentLabel = null;
                    if ("positive".equals(sentiment)) {
                        sentimentLabel = Sentiment.POSITIVE;
                    } else if ("negative".equals(sentiment)) {
                        sentimentLabel = Sentiment.NEGATIVE;
                    } else if ("neutral".equals(sentiment)) {
                        sentimentLabel = Sentiment.NEUTRAL;
                    }
                    if (addOnlyNegative) {
                        if (sentimentLabel == Sentiment.NEGATIVE) {
                            resultMap.put(splits[0], new SentimentTweetInstance(uid, splits[3], sentimentLabel, arkTagger));
                        }
                    } else {
                        resultMap.put(splits[0], new SentimentTweetInstance(uid, splits[3], sentimentLabel, arkTagger));
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("Throwing FileNotFoundException ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Throwing UnsupportedEncodingException ", ex);
        } catch (IOException ex) {
            LOGGER.error("Throwing IOException ", ex);
        }
  
        LOGGER.debug("File lines (tweets): {}, active: {}, map size: {}",
                new Object[] { lineCounter, activeTweets, resultMap.size() });
        
        return new ArrayList(resultMap.values());
    }

    /**
     * 
     * @param tweetsMap
     * @param arkTagger 
     * @param createLearningModelFile
     */
    private static svm_model generateFeaturesList(List<SentimentTweetInstance> learningTweets, Tagger arkTagger) {
        
        List<SentimentTweetInstance> processedLearningTweets = new ArrayList<SentimentTweetInstance>();
        
        // Find the term frequency of all the corpus
        Map<String, Integer> termFrequency = new HashMap<String, Integer>();
        Map<String, Integer> bigramsFrequency = new HashMap<String, Integer>();
        
        Set<String> stopWords = LDATools.getStopWords();
        Pattern pattern = Pattern.compile("(.)\\1{1,}", DOTALL);
        
        for (SentimentTweetInstance learningTweet:learningTweets) {
            String tweetText = learningTweet.getTweetText();
            
            // Detect emoticons and urls much easier
            List<Tagger.TaggedToken> taggedTokens = arkTagger.tokenizeAndTag(tweetText);
            // Investigate the tags that we are interested in (e.g. emoticons)
            List tweetSentenceforNgrams = new ArrayList();
            List tweetSentenceforLexicon = new ArrayList();
            
            // 2. Remove URLs and references @
            // 3. Remove hashtags (do not replace them for this task)
            // 4. Remove special twitter words (e.g. RT)
            // 5. Remove words that start with a number
            // 6. Do not remove web encoding related characters (e.g. &lt, &gt, &amp;) 
            //    because their related to emoticons
            // 7. Remove stopwords
            for (Tagger.TaggedToken tweetToken:taggedTokens) {
                
                // Filter (ignore) certain tokens from the feature vector 
                boolean ignoreTweetTextToken = true;
                
                // 1. Lower Case - Convert the tweets to lower case.
                String tweetTextToken = tweetToken.token.toLowerCase();
                
                // The token is a at-mention
                if ("@".equals(tweetToken.tag) || tweetTextToken.startsWith("@")) {
                    tweetSentenceforLexicon.add(tweetToken.token);
                } // The token is a hashtag
                else if ("#".equals(tweetToken.tag) || tweetTextToken.startsWith("#")) {
                    tweetSentenceforLexicon.add(tweetToken.token);
                } else if (
                        // Ignore tokens of size
                        (tweetTextToken.trim().length() == 1) ||
                        // The token is a url or e-mail address
                        ("U".equals(tweetToken.tag)) ||
                        // The token is a discource marker, retweet
                        ("~".equals(tweetToken.tag)) ||
                        // The token is numeral
                        ("$".equals(tweetToken.tag)) || (Tools.isStringNumeric(tweetTextToken.substring(0, 1))) ||
                        // The token is punctuation
                        (",".equals(tweetToken.tag)) ||
                        // Other abbreviations, foreign words, possessive endings, symbols, garbage
                        ("G".equals(tweetToken.tag)) || 
                        // Remove stopwords
                        (stopWords.contains(tweetTextToken))                       
                        ) {
                    ignoreTweetTextToken = true;
                } else {
                    ignoreTweetTextToken = false;
                }
                
                if (!ignoreTweetTextToken){
                    
                    // Preprocess further the tokens
                    // 8. Replace characters that repeat more than 3 times with 1 occurance
                    tweetTextToken = Tools.replaceRepeatedAdjacentLetters(tweetTextToken, pattern);
                    tweetSentenceforNgrams.add(tweetTextToken);
                    
                    // Update the term frequency map
                    if (termFrequency.containsKey(tweetTextToken)) {
                        // Update the frequence
                        int frequence = termFrequency.get(tweetTextToken);
                        termFrequency.put(tweetTextToken, ++frequence);
                    } else {
                        // Add the token to the map
                        termFrequency.put(tweetTextToken, 1);
                    }
                }
            }
  
            setBigramsMap(tweetSentenceforNgrams, bigramsFrequency);
            
            tweetSentenceforLexicon.addAll(tweetSentenceforNgrams);
            
            // Since the sentiment is already known, there is no need to pass the tagger and perform extra calculations
            SentimentTweetInstance updatedTweetInstance = 
                    new SentimentTweetInstance(null, Tools.prettyPrintList(tweetSentenceforNgrams), learningTweet.getSentiment(), null);
            updatedTweetInstance.setTokensList(tweetSentenceforNgrams);
            
            NHSLexResult mhsLexicoResult = useUnigramsLexicon(tweetSentenceforLexicon, nhsUnigramsLexicon);
            GenericLexiconFeature nhsLexico = new GenericLexiconFeature();
            nhsLexico.setFeature(NHS_LEXICON_TOTAL_SCORE);
            nhsLexico.setTotalTokensScore(mhsLexicoResult.getTotalTokensScore());
            
            NHSLexResult senti140LexicoResult = useUnigramsLexicon(tweetSentenceforLexicon, senti140UnigramsLexicon);
            GenericLexiconFeature senti140Lexico = new GenericLexiconFeature();
            senti140Lexico.setFeature(SENTI_140LEXICON_TOTAL_SCORE);
            senti140Lexico.setTotalTokensScore(senti140LexicoResult.getTotalTokensScore());
            
            NHSLexResult bingLiuLexicoResult = useUnigramsLexicon(tweetSentenceforLexicon, bingLiuUnigramsLexicon);
            GenericLexiconFeature bingLiuLexico = new GenericLexiconFeature();
            bingLiuLexico.setFeature(BINGLIU_LEXICON_TOTAL_SCORE);
            bingLiuLexico.setTotalTokensScore(bingLiuLexicoResult.getTotalTokensScore());
            
            updatedTweetInstance.updateLexiconFeatures(nhsLexico);
            updatedTweetInstance.updateLexiconFeatures(senti140Lexico);
            updatedTweetInstance.updateLexiconFeatures(bingLiuLexico);
            
            processedLearningTweets.add(updatedTweetInstance);
        }
        
        Map<Integer, List<String>> frequencyTermsMap = getFrequenciesTermMap(termFrequency);
        final List<String> featuresList = setFeatureList(filterTweetTokens(frequencyTermsMap, true),
                filterTweetBigrams(getFrequenciesTermMap(bigramsFrequency), true));
        // TODO Store the feature list
        Tools.createFile(svmFeaturesFileName, featuresList, true);
        
        // When handling unigrams we are not interested on the frequency
        SVMFeature svmFeature = getSVMFeature(processedLearningTweets, featuresList);
        svm_model learningModel = createSVMModel(svmFeature, processedLearningTweets);
        return learningModel;
    }
    
    /**
     * 
     * @param termFrequencies 
     */
    public static Map<Integer, List<String>> getFrequenciesTermMap(Map<String, Integer> termFrequencies) {
        
        // All the tokens that have frequency 1 (key: 1)
        // All the tokens that have frequency 2 (key: 2)
        Map<Integer, List<String>> frequencyPerToken = new TreeMap<Integer, List<String>>();
        
        // Set the frequencies
        for (String token : termFrequencies.keySet()) {
            
            Integer frequency = termFrequencies.get(token);
            if (frequencyPerToken.containsKey(frequency)){
                // Update the frequence token list
                List<String> tokensList = frequencyPerToken.get(frequency);
                tokensList.add(token);
                frequencyPerToken.put(frequency, tokensList);
            } else {
                // Add the token to the map
                List<String> tokensList = new ArrayList<String>(0);
                tokensList.add(token);
                frequencyPerToken.put(frequency, tokensList);
            }
        }
        return frequencyPerToken;
    }

    /**
     * 
     * @param frequencyTermsMap
     * @param removeWordsOfFrequencyOne
     * @return 
     */
    public static Map<Integer, List<String>> filterTweetTokens(Map<Integer, List<String>> frequencyTermsMap, boolean removeWordsOfFrequencyOne) {
        if (removeWordsOfFrequencyOne) {
            // Remove the terms that occur only once. Note that useful words for 
            // sentiment analysis will be removed (e.g. swearing words).
            frequencyTermsMap.remove(1);            
        }
        
        // Remove the temporal tokens
        // Copy the map
        Map<Integer, List<String>> frequencyTermsMapCopy = new TreeMap<Integer, List<String>>();
        for (Entry<Integer, List<String>> entry:frequencyTermsMap.entrySet()) {
            frequencyTermsMapCopy.put(entry.getKey(), new ArrayList<String>(entry.getValue()));
        }
        
        List<String> commonTweetTokens = new ArrayList<String>();
        commonTweetTokens.add("it's");
        commonTweetTokens.add("its");
        commonTweetTokens.add("i'm");
        commonTweetTokens.add("im");
        commonTweetTokens.add("he's");
        commonTweetTokens.add("she's");
        
        commonTweetTokens.add("i'll");
        commonTweetTokens.add("you'll");
        commonTweetTokens.add("he'll");
        commonTweetTokens.add("she'll");
        commonTweetTokens.add("it'll");
        commonTweetTokens.add("we'll");
        commonTweetTokens.add("they'll");
        
        commonTweetTokens.add("i've");
        commonTweetTokens.add("you're");
        commonTweetTokens.add("we're");
        commonTweetTokens.add("they're");
        
        // Remove the temporal tokens from the lists
        for (Integer freq:frequencyTermsMap.keySet()) {
            List<String> tokensList = frequencyTermsMap.get(freq);
            List<String> tokensListCopy = frequencyTermsMapCopy.get(freq);
            for (String token:tokensList) {                
                if (TemporalDetection.isTemporalWord(token)) {
                    tokensListCopy.remove(token);
                }
                if (commonTweetTokens.contains(token)) {
                    tokensListCopy.remove(token);
                }
                
                if (token.length() == 1) {
                    tokensListCopy.remove(token);
                }
            }
            // Update the map
            if (tokensListCopy.isEmpty()) {
                frequencyTermsMapCopy.remove(freq);
            } else {
                frequencyTermsMapCopy.put(freq, tokensListCopy);
            }
        }
        
        return frequencyTermsMapCopy;
    }
    
    /**
     * 
     * @param bigramsMap
     * @param removeWordsOfFrequencyOne
     * @return 
     */
    public static Map<Integer, List<String>> filterTweetBigrams(Map<Integer, List<String>> bigramsMap, boolean removeWordsOfFrequencyOne) {
        
        if (removeWordsOfFrequencyOne) {
            // Remove the bigrams that occur only once.
            bigramsMap.remove(1);
        }
        
        return bigramsMap;
    }

    /**
     * 
     * @param svmFeature
     * @param processedLearningTweets 
     */
    private static svm_model createSVMModel(SVMFeature svmFeature, final List<SentimentTweetInstance> processedLearningTweets) {
        
        int numberOfInstances = processedLearningTweets.size();
        
        // Get the label (category) of each tweet
        List<Integer> labels = svmFeature.getLabels();
        List<List<Integer>> featureVector = svmFeature.getFeatureVector();
        
        //#SVM params
        svm_parameter param = new svm_parameter();
//        param.C = 1;// See http://doras.dcu.ie/15663/1/cikm1079-bermingham.pdf
        param.C = 0.3;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.001;
        
        //#instantiate the problem
        svm_problem problem = new svm_problem();
        problem.l = numberOfInstances;
        problem.y = new double[numberOfInstances];
        problem.x = new svm_node[numberOfInstances][];
        
        for (int i = 0; i < numberOfInstances; i++) {
            problem.y[i] = labels.get(i);
            
            List<svm_node> attributesList = new ArrayList<svm_node>();
            
            List<Integer> featureVectorList = new ArrayList<Integer>(featureVector.get(i));

            // Note that the attributes should count from 1 to total features size (e.g. 6338).
            for (Integer indexPositionInFeatureList:featureVectorList) {
                
                int attribute = indexPositionInFeatureList+1;
                svm_node node = new svm_node();
                node.index = attribute;
                node.value = 1;
                
                attributesList.add(node);
            }
            
            svm_node[] attributes = new svm_node[attributesList.size()];
            for (int j = 0; j < attributesList.size(); j++) {
                attributes[j] = attributesList.get(j);
            }
                        
            problem.x[i] = attributes;
        }
        
        if(svm.svm_check_parameter(problem, param) != null) {
            LOGGER.error("Wrong parameters in SVM model {}", svm.svm_check_parameter(problem, param));
        }
        svm_model learningModel = svm.svm_train(problem, param);
        try {
            //#save the model
            svm.svm_save_model(svmModelFileName, learningModel);
        } catch (IOException ex) {
            LOGGER.error("Throwing exception while saving the model {}", ex);
        }
        
        return learningModel;
    }


    /**
     * 
     * @param processedLearningTweets
     * @param featuresList
     * @return 
     */
    private static SVMFeature getSVMFeature(List<SentimentTweetInstance> processedLearningTweets, List<String> featuresList) {

        List featureVector = new ArrayList();
        List<Map> featureVectorHslex = new ArrayList<Map>();
        List<Integer> labels = new ArrayList<Integer>();
        
        int counter = 0;
        boolean containsSwearingWord = false;
        boolean containsNegativeEmoticon = false;
        boolean containsPositiveEmoticon = false;
        boolean containsNegations = false;
        for (SentimentTweetInstance tweetInstance:processedLearningTweets) {
            
            List<String> unigrams = tweetInstance.getTokensList();
            List<String> bigrams = getBigramsFromList(unigrams);
            
            // Append both lists
            List<String> ngrams = unigrams;
            ngrams.addAll(bigrams);
            
            // The index of the feature regarding the featuresList list, do not add dublicates
            Set<Integer> indexOfFeatureList = new LinkedHashSet<Integer>();
            //#Fill the map
            for (String word:ngrams) {
                if (featuresList.contains(word)){
                    // Set the index of the list
                    indexOfFeatureList.add(featuresList.indexOf(word));
                }
                
                
                for (String swearWord:SWEARWORDS) {
                    if (word.equals(swearWord)) {
                        containsSwearingWord = true;
                        break;
                    }
                }
                
                for (String negEmoticon:NEGATIVE_EMOTICONS) {
                    if (word.equals(negEmoticon)) {
                        containsNegativeEmoticon = true;
                        break;
                    }
                }
                
                for (String posEmoticon:POSITIVE_EMOTICONS) {
                    if (word.equals(posEmoticon)) {
                        containsPositiveEmoticon = true;
                        break;
                    }
                }
                
                for (String negatedWord:NEGATION_LIST) {
                    if (word.equals(negatedWord)) {
                        containsNegations = true;
                        break;
                    }
                }
            }
            
            Map<String, Integer> hashLexMap = new HashMap<String, Integer>();
            
            Map<String, GenericLexiconFeature> lexicalFeaturesMap = tweetInstance.getLexicalFeaturesMap();
            GenericLexiconFeature nhsLexicon = lexicalFeaturesMap.get(NHS_LEXICON_TOTAL_SCORE);
            if (nhsLexicon.getTotalTokensScore() > 0) {
                indexOfFeatureList.add(featuresList.indexOf(NHS_LEXICON_TOTAL_SCORE));
                hashLexMap.put(NHS_LEXICON_TOTAL_SCORE, nhsLexicon.getTotalTokensScore());
            }
            
            GenericLexiconFeature senti140Lexicon = lexicalFeaturesMap.get(SENTI_140LEXICON_TOTAL_SCORE);
            if (senti140Lexicon.getTotalTokensScore() > 0) {
                indexOfFeatureList.add(featuresList.indexOf(SENTI_140LEXICON_TOTAL_SCORE));
            }
            
            GenericLexiconFeature bingLiuLexicon = lexicalFeaturesMap.get(BINGLIU_LEXICON_TOTAL_SCORE);
            if (bingLiuLexicon.getTotalTokensScore() > 0) {
                indexOfFeatureList.add(featuresList.indexOf(BINGLIU_LEXICON_TOTAL_SCORE));
            }
            
            if (containsSwearingWord) {
                indexOfFeatureList.add(featuresList.indexOf(SWEAR_WORD_FEATURE));
            }
            
            if (containsNegativeEmoticon) {
                indexOfFeatureList.add(featuresList.indexOf(NEG_EMOTICONS_FEATURE));
            }
            
            if (containsPositiveEmoticon) {
                indexOfFeatureList.add(featuresList.indexOf(POS_EMOTICONS_FEATURE));
            }
            
            if (containsNegations) {
                indexOfFeatureList.add(featuresList.indexOf(CONTAINS_NEGATION_FEATURE));
            }
            
            // Note! The order of the tweet words must be that of the feature list
            List list = new ArrayList(indexOfFeatureList);
            Collections.sort(list);
            featureVector.add(list);
            featureVectorHslex.add(hashLexMap);
            labels.add(tweetInstance.getSentiment().getValue());
            LOGGER.debug("Processed {} of {}", ++counter, processedLearningTweets.size());
        }
        
        SVMFeature svmFeature = new SVMFeature(featureVector, labels, featureVectorHslex);
        return svmFeature;
    }
    
    /**
     * 
     * @param tweetInstance
     * @param modelFeaturesList
     * @return 
     */
    public svm_node[] createEventsTestInstance(TweetInstance tweetInstance, List<String> modelFeaturesList) {
        
        List<String> ngrams = new ArrayList<String>();
        // Add the unigrams
        List<String> unigrams = tweetInstance.getTokensList();
        ngrams.addAll(unigrams);
        
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
    public svm_node[] createTestInstance(SentimentTweetInstance tweetInstance, List<String> modelFeaturesList) {
        
        // The tokens of the tweets
        List<String> ngrams = new ArrayList<String>();
        // Add the unigrams
        List<String> unigrams = tweetInstance.getTokensList();
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
        
        NHSLexResult nhsUnigramsLex = useUnigramsLexicon(unigrams, nhsUnigramsLexicon);
        NHSLexResult senti140UnigramsLex = useUnigramsLexicon(unigrams, senti140UnigramsLexicon);
        NHSLexResult bingLiuUnigramsLex = useUnigramsLexicon(unigrams, bingLiuUnigramsLexicon);

        int nhsLexTotalScore = nhsUnigramsLex.getTotalTokensScore();
        if (nhsLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(NHS_LEXICON_TOTAL_SCORE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        }
        
        int senti140LexTotalScore = senti140UnigramsLex.getTotalTokensScore();
        if (senti140LexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(SENTI_140LEXICON_TOTAL_SCORE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;

            attributesList.add(node);            
        }
        
        int bingLiuLexTotalScore = bingLiuUnigramsLex.getTotalTokensScore();
        if (bingLiuLexTotalScore > 0) {
            int k = modelFeaturesList.indexOf(BINGLIU_LEXICON_TOTAL_SCORE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);
        }
        
        boolean containsSwearingWord = false;
        
        for (String unigram:unigrams){
            for (String swearWord:SWEARWORDS) {
                if (unigram.equals(swearWord)) {
                    containsSwearingWord = true;
                    break;
                }
            }
        }
        
        if (containsSwearingWord) {
            int k = modelFeaturesList.indexOf(SWEAR_WORD_FEATURE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);            
        }
        
        boolean containsNegEmoticon = false;
        
        for (String unigram:unigrams){
            if (NEGATIVE_EMOTICONS.contains(unigram)) {
                containsNegEmoticon = true;
                break;
            }
        }
        
        if (containsNegEmoticon) {
            int k = modelFeaturesList.indexOf(NEG_EMOTICONS_FEATURE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsPosEmoticon = false;
        
        for (String unigram:unigrams){
            if (POSITIVE_EMOTICONS.contains(unigram)) {
                containsPosEmoticon = true;
                break;
            }
        }
        
        if (containsPosEmoticon) {
            int k = modelFeaturesList.indexOf(POS_EMOTICONS_FEATURE);
            
            int attribute = k+1;
            svm_node node = new svm_node();
            node.index = attribute;
            node.value = 1;
            
            attributesList.add(node);             
        }
        
        boolean containsNegation = false;
        
        for (String unigram:unigrams){
            if (NEGATION_LIST.contains(unigram)) {
                containsNegation = true;
                break;
            }
        }
        
        if (containsNegation) {
            int k = modelFeaturesList.indexOf(CONTAINS_NEGATION_FEATURE);
            
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
     * @param tweetSentence
     * @param bigramsFrequency 
     */
    private static void setBigramsMap(List tweetSentence, Map<String, Integer> bigramsFrequency) {
        // Consider as bigrams 2 consecutive words in the list of tokens
        for (int i = 0; i+1 < tweetSentence.size(); i++) {
            String biGram = tweetSentence.get(i)+" "+tweetSentence.get(i+1);
            // Update the term frequency map
            if (bigramsFrequency.containsKey(biGram)) {
                // Update the frequence
                int frequence = bigramsFrequency.get(biGram);
                bigramsFrequency.put(biGram, ++frequence);
            } else {
                // Add the token to the map
                bigramsFrequency.put(biGram, 1);
            }
        }
    }
    
    /**
     * 
     * @param tweetSentence
     * @return 
     */
    public static List<String> getBigramsFromList(List tweetSentence) {
        List<String> bigramsList = new ArrayList<String>();
        
        // Consider as bigrams 2 consecutive words in the list of tokens
        for (int i = 0; i+1 < tweetSentence.size(); i++) {
            bigramsList.add(tweetSentence.get(i)+" "+tweetSentence.get(i+1));
        }
        
        return bigramsList;
    }
    
    /**
     * 
     * @param tweetSentence 
     */
    private static NHSLexResult useUnigramsLexicon(final List<String> tweetSentence, Map<String, Integer> lexicon) {
        
        // Total count of tokens in the tweet with score(w,p) > 0 
        int positiveTokensScoreCounter = 0;
        // Total score of the tokens
        int totalTokensScore = 0;
        // Maximal score of the tokens
        int maxTokensScore = 0;
        // The score of the last token in the tweet with score(w,p) > 0
        int lastPositiveTokenScore = 0;
        
        for (String token:tweetSentence) {
            if (lexicon.containsKey(token)) {
                int score = lexicon.get(token);
                totalTokensScore += score;
                // Get the maximum token
                if (score > maxTokensScore) {
                    maxTokensScore = score;
                }
                
                if (score > 0) {
                    lastPositiveTokenScore = score;
                    positiveTokensScoreCounter++;
                }
            }
        }
        
        NHSLexResult result = new NHSLexResult();
        result.setTotalTokensScore(totalTokensScore);
        
        return result;
    }
    
    /**
     * 
     * @param filteredUnigramsMapByFrequency
     * @param filteredBigramsMapByFrequency 
     */
    private static List<String> setFeatureList(Map<Integer, List<String>> filteredUnigramsMapByFrequency,
            Map<Integer, List<String>> filteredBigramsMapByFrequency) {
        
        List<String> featuresList = new ArrayList<String>();
        for (Integer freq:filteredUnigramsMapByFrequency.keySet()) {
            List tokensList = filteredUnigramsMapByFrequency.get(freq);
            featuresList.addAll(tokensList);
        }
        
        // Add the bigrams
        for (Integer freq:filteredBigramsMapByFrequency.keySet()) {
            List tokensList = filteredBigramsMapByFrequency.get(freq);
            featuresList.addAll(tokensList);
        }
        
        featuresList.add(NHS_LEXICON_TOTAL_SCORE);
        featuresList.add(SENTI_140LEXICON_TOTAL_SCORE);
        featuresList.add(BINGLIU_LEXICON_TOTAL_SCORE);
        featuresList.add(SWEAR_WORD_FEATURE);
        featuresList.add(NEG_EMOTICONS_FEATURE);
        featuresList.add(POS_EMOTICONS_FEATURE);
        featuresList.add(CONTAINS_NEGATION_FEATURE);
        
        LOGGER.debug("Feature vector size: {}", featuresList.size());
        return featuresList;
    }

    /**
     * 
     * @param testInstance
     * @return 
     */
    public static double applyRuleBasedPrediction(TweetInstance testInstance) {
        
        int negativePrediction = 1;
        
        // Rule 1. The tweet contains a swear word that has only negative meaning (e.g. crap), not fuck
        boolean swearingStrongWordCase = false;
        List<String> tokensList = testInstance.getTokensList();
        List<String> swearingListNegOnly = Tools.getTwitterSwearWordsNegOnly();
        for (String token:tokensList) {
            
            for (String swear:swearingListNegOnly) {
                if (token.equals(swear)) {
                    swearingStrongWordCase = true;
                    break;
                }
            }
            
            if (swearingStrongWordCase) {
                break;
            }
        }
        
        if (swearingStrongWordCase) {
            return negativePrediction;
        }
        
        // Rule 2. Examine the role of the "not" word
        
        return -1;
    }

    public static class TweetInstanceComparator implements Comparator<SentimentTweetInstance> {
        
        @Override
        public int compare(SentimentTweetInstance o1, SentimentTweetInstance o2) {
            
            Integer id1 = Integer.parseInt(o1.getTweetId());
            Integer id2 = Integer.parseInt(o2.getTweetId());
            
            return id1.compareTo(id2);
        }
        
    }
    
    private static class SVMFeature {
        
        // Contains only the 1s that correspond to the tweet's tokens
        private final List featureVector;
        
        // The label (sentiment) of the tweet
        private final List<Integer> labels;
        
        // Contains the scores for the Hashtag Sentiment Lexicon
        private final List<Map> featureVectorHashlex;
        
        /**
         * 
         * @param featureVector
         * @param labels
         * @param featureVectorHashlex
         */
        public SVMFeature(List featureVector, List<Integer> labels, List<Map> featureVectorHashlex) {
            this.featureVector = featureVector;
            this.labels = labels;
            this.featureVectorHashlex = featureVectorHashlex;
        }

        public List getFeatureVector() {
            return featureVector;
        }

        public List<Integer> getLabels() {
            return labels;
        }

        public List<Map> getFeatureVectorHashlex() {
            return featureVectorHashlex;
        }

        @Override
        public String toString() {
            return "SVMFeature{" + "labels=" + labels + ", feature_vector=" + featureVector + '}';
        }
    }
    
    private static class NHSLexResult {
        
        // Total score of the tokens
        private int totalTokensScore = 0;

        public NHSLexResult() {
        }

        public int getTotalTokensScore() {
            return totalTokensScore;
        }

        public void setTotalTokensScore(int totalTokensScore) {
            this.totalTokensScore = totalTokensScore;
        }
    }
}
