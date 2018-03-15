/**
 * 
 */
package gr.atc.nlptoolkit.sentiment;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;
import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class GreekLexiconValidator {
    
    // The path to the models folder
    private static String modelDataPath;
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(GreekLexiconValidator.class);

    /**
     * 
     * @param modelDataPath 
     */
    public GreekLexiconValidator(String modelDataPath) {
        GreekLexiconValidator.modelDataPath = modelDataPath;
    }
    
    /**
     * 
     * @param permittedValues
     * @param annotations
     * @param i 
     * @return true if the value contains an error
     */
    private static boolean checkColumnValues(String label, List permittedValues, String annotations[], int i) {

        String researcher1Annotation = annotations[0];
        String researcher2Annotation = annotations[1];
        String researcher3Annotation = annotations[2];
        String researcher4Annotation = annotations[3];

        boolean hasErroneousValue = false;
        String wrongValue = null;
        if (!permittedValues.contains(researcher1Annotation)){
            hasErroneousValue = true;
            wrongValue = researcher1Annotation;
        } else if (!permittedValues.contains(researcher2Annotation)){
            hasErroneousValue = true;
            wrongValue = researcher2Annotation;
        } else if (!permittedValues.contains(researcher3Annotation)){
            hasErroneousValue = true;
            wrongValue = researcher3Annotation;
        } else if (!permittedValues.contains(researcher4Annotation)){
            hasErroneousValue = true;
            wrongValue = researcher4Annotation;
        }

        if (hasErroneousValue) {
            LOGGER.error("The category {} has problem while parsing line: {} with value {}",
                    new Object[] {label, i, wrongValue});
        }
        
        return hasErroneousValue;
    }
    
    /**
     * 
     * @param lexiconRows 
     * @return  
     */
    public static Set<GreekLexiconEntry> generateSentimentScores(List<String[]> lexiconRows) {
        
        if (lexiconRows != null) {
            
            List<GreekLexiconEntry> lexicon = new ArrayList<GreekLexiconEntry>();
            
            List<GreekLexiconEntry> sortByFeelings = new ArrayList<GreekLexiconEntry>();
            
            // Loop the words, ignore the captions line
            for (int i = 1; i < lexiconRows.size(); i++) {
                
                String[] rowsLine = lexiconRows.get(i);
                
                GreekLexiconEntry entry = new GreekLexiconEntry();
                
                String term = rowsLine[0];
                entry.setTerm(term);
                
                // Subjectivity score
                int subjectivityScore = calculateSubjectivityScore(term, Arrays.copyOfRange(rowsLine, 1, 5));
                if (subjectivityScore > 0) {
                    entry.setSubjectivityScore(subjectivityScore);
                    
                    int polatiryScore = calculatePolarityScore(term, Arrays.copyOfRange(rowsLine, 5, 9));
                    if (polatiryScore != 0) {
                        entry.setPolarityScore(polatiryScore);
                        lexicon.add(entry);
                    } else {
                        
                        // Assign the polarity score based on the feelings
                        
                        // Anger score
                        double angerScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 9, 13));
                        entry.setAngerScore(angerScore);
                        
                        // Disgust score
                        double disgustsScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 13, 17));
                        entry.setDisgustScore(disgustsScore);
                        
                        // Fear score
                        double fearScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 17, 21));
                        entry.setFearScore(fearScore);
                        
                        // Happiness score
                        double hapinessScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 21, 25));
                        entry.setHapinessScore(hapinessScore);
                        
                        // Sadness score
                        double sadnessScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 25, 29));
                        entry.setSadnessScore(sadnessScore);
                        
                        // Surprise score
                        double surpriseScore = calculateFeelingsScore(Arrays.copyOfRange(rowsLine, 29, 33));
                        entry.setSurpriseScore(surpriseScore);
                        
                        double feelingsScore = entry.getAngerScore()+entry.getDisgustScore()+entry.getFearScore()+
                                entry.getHapinessScore()+entry.getSadnessScore()+entry.getSurpriseScore();
                        
                        entry.setFeelingsScore(feelingsScore);
                        sortByFeelings.add(entry);
                    }
                }
            }
            
            Collections.sort(sortByFeelings);
//            for (GreekLexiconEntry entry:sortByFeelings) {
//                System.out.println(entry);
//            }
            
            Set<GreekLexiconEntry> unique = new HashSet<GreekLexiconEntry>();
            for (GreekLexiconEntry entry:lexicon) {
                
                String term = entry.getTerm();
                String tokens[] = term.split(" ");
                String token = tokens[0];
                
                String wordWithoutAccent = token.replace("ά", "α");
                wordWithoutAccent = wordWithoutAccent.replace("έ", "ε");
                wordWithoutAccent = wordWithoutAccent.replace("ί", "ι");
                wordWithoutAccent = wordWithoutAccent.replace("ή", "η");
                wordWithoutAccent = wordWithoutAccent.replace("ύ", "υ");
                wordWithoutAccent = wordWithoutAccent.replace("ό", "ο");
                wordWithoutAccent = wordWithoutAccent.replace("ώ", "ω");
                
                String stemmedWord = wordWithoutAccent;
                if (
                        wordWithoutAccent.endsWith("ος") ||
                        wordWithoutAccent.endsWith("ας") ||
                        wordWithoutAccent.endsWith("ης") ||
                        wordWithoutAccent.endsWith("ες"))
                    {
                        stemmedWord = stemmedWord.substring(0, wordWithoutAccent.length()-2);
                } else if (
                        wordWithoutAccent.endsWith("ε") ||
                        wordWithoutAccent.endsWith("ι") ||
                        wordWithoutAccent.endsWith("η") ||
                        wordWithoutAccent.endsWith("υ") ||
                        wordWithoutAccent.endsWith("ο") ||
                        wordWithoutAccent.endsWith("ω")) {
                    stemmedWord = stemmedWord.substring(0, wordWithoutAccent.length()-1);
                } else if (wordWithoutAccent.endsWith("εια")){
                    stemmedWord = stemmedWord.substring(0, wordWithoutAccent.length()-3);
                } else if (wordWithoutAccent.endsWith("ια")){
                    stemmedWord = stemmedWord.substring(0, wordWithoutAccent.length()-2);
                } else if (wordWithoutAccent.endsWith("α")){
                    stemmedWord = stemmedWord.substring(0, wordWithoutAccent.length()-1);
                }
                
                entry.setTermToken(token);
                entry.setTermTokenStemmed(stemmedWord);
                
                if (stemmedWord.length() > 2) {
                    unique.add(entry);
                }
            }
            return unique;
        }
        return null;
    }
    
    
    
    /**
     * 
     * @param term
     * @param subjectivities
     * @return A value {-1, 1, 2, 3} that corresponds to strong(1) - low (3) agreement 
     * on the subjectivity score. -1 for words of no sentiment
     */
    private static int calculateSubjectivityScore(String term, String[] subjectivities) {
        
        Set<String> noDuplicates = new HashSet<String>();
        for (int i = 0; i < subjectivities.length; i++) {
            if (!"N/A".equals(subjectivities[i])) {
                
                noDuplicates.add(subjectivities[i]);
            }
        }
        
        // If all agree that it is objective, return -1
        if ((noDuplicates.size() == 1) && (noDuplicates.contains("OBJ"))) {
            return -1;
        }
        
        if (noDuplicates.size() > 0) {
            // How much do the subjectivity values agree?
            return noDuplicates.size();            
        } else {
            return -1; // All values are N/A
        }
    }
    
    private static int calculatePolarityScore(String term, String[] polarities) {
        
        Set<String> noDuplicates = new HashSet<String>();
        for (int i = 0; i < polarities.length; i++) {
            if (!"N/A".equals(polarities[i])) {
                
                noDuplicates.add(polarities[i]);
            }
        }
        
        int sentiment = 0;
        
        // Strong positive words
        if ((noDuplicates.size() == 1) && (noDuplicates.contains("POS"))) {
            sentiment = 2;
        } else if ((noDuplicates.size() == 1) && (noDuplicates.contains("NEG"))) {
            // Strong negative words
            sentiment = -2;
        } else if ((noDuplicates.size() == 2) && (noDuplicates.contains("POS") && noDuplicates.contains("BOTH"))) {
            sentiment = 1;
        } else if ((noDuplicates.size() == 2) && (noDuplicates.contains("NEG") && noDuplicates.contains("BOTH"))) {
            sentiment = -1;
        }
        
        return sentiment;        
    }
    
    /**
     * 
     * @param feelings
     * @return 
     */
    private static double calculateFeelingsScore(String[] feelings) {
        
        double counter = 0;
        double sum = 0;
        for (int i = 0; i < feelings.length; i++) {
            if (!"N/A".equals(feelings[i])) {
                counter++;
                Integer score = Integer.valueOf(feelings[i]);
                sum = sum + score;
            }
        }
        
        // Calculate the average value
        if (counter > 0) {
            return sum/counter;
        }
        
        return -1;
    }    
    
    /**
     * 
     * @param modelDataPath
     * @return 
     */
    public static List<String[]> getLexiconRowsAsList() {
        
        TsvParserSettings settings = new TsvParserSettings();
        
        //the file used in the example uses '\n' as the line separator sequence.
        //the line separator sequence is defined here to ensure systems such as MacOS and Windows
        //are able to process this file correctly (MacOS uses '\r'; and Windows uses '\r\n').
        settings.getFormat().setLineSeparator("\n");
        
        // Here we select only the columns "Price", "Year" and "Make".
        // The parser just skips the other fields
        settings.selectFields("Term",
                "Subjectivity1", "Subjectivity2", "Subjectivity3", "Subjectivity4",
                "Polarity1", "Polarity2", "Polarity3", "Polarity4",
                "Anger1", "Anger2", "Anger3", "Anger4",
                "Disgust1", "Disgust2", "Disgust3", "Disgust4",
                "Fear1", "Fear2", "Fear3", "Fear4",
                "Happiness1", "Happiness2", "Happiness3", "Happiness4",
                "Sadness1", "Sadness2", "Sadness3", "Sadness4",
                "Surprise1", "Surprise2", "Surprise3", "Surprise4");
        
        String greekSentimentLexiconPath = modelDataPath+"/greek/greek_sentiment_lexicon.tsv";
        LOGGER.info("greekSentimentLexiconPath: {}", greekSentimentLexiconPath);
        
        TsvParser parser = new TsvParser(settings);
        List<String[]> allRows = null;
        try {
            // parses all rows in one go.
            allRows = parser.parseAll(
                    new InputStreamReader(
                            new FileInputStream(greekSentimentLexiconPath), "UTF-8"));
        } catch (FileNotFoundException ex) {
            LOGGER.error("File not found ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Unsupported exception ", ex);
        }
        
        return allRows;        
    }
    
    public static boolean validateLexicon() {
        
        List<String[]> allRows = getLexiconRowsAsList();
        boolean isValid = true;        
        if (allRows != null) {
            // Loop the words, ignore the captions line
            for (int i = 1; i < allRows.size(); i++) {
                
                String[] rowsLine = allRows.get(i);
                
                // The values are strong/weak/none
                List subjectivityValues = new ArrayList<String>();
                subjectivityValues.add("N/A");
                subjectivityValues.add("OBJ");
                subjectivityValues.add("SUBJ-");
                subjectivityValues.add("SUBJ+");
                
                isValid = checkColumnValues("Subjectivity", subjectivityValues, Arrays.copyOfRange(rowsLine, 1, 5), i);
                
                // The values are positive/negative/both
                List polarityValues = new ArrayList<String>();
                polarityValues.add("N/A");
                polarityValues.add("BOTH");
                polarityValues.add("NEG");
                polarityValues.add("POS");
                
                isValid = checkColumnValues("Polarity", polarityValues, Arrays.copyOfRange(rowsLine, 5, 9), i);
                
                // The values are weak/strong scaled from 1 to 5
                List feelingsValues = new ArrayList<String>();
                feelingsValues.add("N/A");
                feelingsValues.add("1");
                feelingsValues.add("2");
                feelingsValues.add("3");
                feelingsValues.add("4");
                feelingsValues.add("5");
                
                // Anger
                isValid = checkColumnValues("Anger", feelingsValues, Arrays.copyOfRange(rowsLine, 9, 13), i);
                // Disgust
                isValid = checkColumnValues("Disgust", feelingsValues, Arrays.copyOfRange(rowsLine, 13, 17), i);
                // Fear
                isValid = checkColumnValues("Fear", feelingsValues, Arrays.copyOfRange(rowsLine, 17, 21), i);
                // Happiness
                isValid = checkColumnValues("Happiness", feelingsValues, Arrays.copyOfRange(rowsLine, 21, 25), i);
                // Sadness
                isValid = checkColumnValues("Sadness", feelingsValues, Arrays.copyOfRange(rowsLine, 25, 29), i);
                // Surprise
                isValid = checkColumnValues("Surprise", feelingsValues, Arrays.copyOfRange(rowsLine, 29, 33), i);
            }
        }        
        
        return isValid;
    }
    
    /**
     * 
     * @return 
     */
    public Map<String, Integer> getGreekSocialSensorLexicon() {
        
        Set<GreekLexiconEntry> lexiconEntries = generateSentimentScores(getLexiconRowsAsList());
        Map<String, Integer> lexicon = new HashMap<String, Integer>();
        
        for (GreekLexiconEntry entry:lexiconEntries) {
            lexicon.put(entry.getTermTokenStemmed(), entry.getPolarityScore());
        }
        
        return lexicon;
    }
    
    public static void main (String [] args) {
        
        //validateLexicon();
        Set<GreekLexiconEntry> lexicon = generateSentimentScores(getLexiconRowsAsList());
        
        for (GreekLexiconEntry entry:lexicon) {
            System.out.println(entry);
        }
    }    
}

