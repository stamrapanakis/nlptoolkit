/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template outputFile, choose Tools | Templates
 * and open the template in the editor.
 */

package gr.atc.nlptoolkit.utils;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.collections.CollectionUtils;
import java.text.DecimalFormatSymbols;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class Tools {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(Tools.class);
    
    public static final String LINE_SEPARATOR = "\r\n";

    // Sonar: Utility classes should not have a public constructor
    private Tools() {
    }
    
    /**
     * 
     * @param filePath 
     */
    public static void removeDuplicateTweetsFromFile(String filePath){
        
        Map<String, String> tweetIds = new TreeMap<String, String>(); 
    	BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line = br.readLine();
            
            // Calculate the merged tweets
            int counter = 0;
            while (line != null) {
                if (StringUtils.isNotBlank(line)) {
                    counter++;
                    String[] splits = line.split("\t");
                    if (splits.length == 3) {
                        String tweetId = splits[0];
                        // Add a line separator
                        tweetIds.put(tweetId, line);
                    } else {
                        LOGGER.error("Could not split line {} in file {}", line, filePath);
                    }
                }
                line = br.readLine();
            }
            
            LOGGER.debug("Merged {} tweets. The file now contains {} tweets.", counter-tweetIds.size(), tweetIds.size());
            if (tweetIds.isEmpty()) {
                LOGGER.debug("No tweets retrieved. Please check the search query.");
            }
            
            // Write the new outputFile contents
            List<String> list = new ArrayList<String>(tweetIds.values());
            List<String> resultList = new ArrayList<String>();
            counter = 0;
            for (String value:list) {
                counter++;
                // Do not add a line separator in the last item
                if (counter != list.size()) {
                    resultList.add(value+LINE_SEPARATOR);
                } else {
                    resultList.add(value);
                }
            }
            createFile(filePath, resultList, false);
            
        } catch (FileNotFoundException ex) {
            LOGGER.error("Exception thrown {}", ex);
        } catch (IOException ex) {
            LOGGER.error("Exception thrown {}", ex);
        } finally {
            try {
                if (br != null){
                    br.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Exception thrown {}", ex);
            }
        }
    }    
    /**
     * Create a sentence that consists of tokens
     * 
     * @param tokens The tokens that will constitute the sentence
     * @param delimiter The character that will separate the tokens in the sentence
     * @return 
     */
    public static String getTokenArrayAsString(Object tokens[], String delimiter) {
        StringBuilder sentence = new StringBuilder();
        
        if (tokens != null) {
            // Do not append a delimiter character at the end of the sentence
            int delimeterCounter = 0;
            for (int i=0; i < tokens.length; i++) {
                
                sentence.append(tokens[i]);
                if(delimeterCounter != (tokens.length-1)){
                    sentence.append(delimiter);
                }
                delimeterCounter++;
            }
            return sentence.toString();
        }
        return null;
    }
    
    /**
     * Creates a outputFile and its intermediate folders if they don't exist
     * 
     * @param filename
     * @return 
     */
    public static boolean createFileIfNotExists(final String filename){
        boolean success = true;
        if (StringUtils.isNotBlank(filename)) {
            if (filename.contains(File.separator)) {
                
                File file = new File(filename);
                if (!file.exists()) {
                    // Check if the intermediate folders exist
                    String foldersPath = filename.substring(0, filename.lastIndexOf(File.separator));
                    File folders = new File(foldersPath);
                    folders.mkdirs();
                    try {
                        success = file.createNewFile();
                    } catch (IOException ex) {
                        LOGGER.error("Unable to create file {}", ex);
                        success = false;
                    }
                }
            } else {
                success = false;
            }
        } else {
            success = false;
        }
        
        return success;
    }    
    
    /**
     * Create a outputFile with provided contents in the provided filePath
     * 
     * @param filePath
     * @param content
     */
    public static void createFile(String filePath, String content) {
        
        if (StringUtils.isBlank(content)) {
            LOGGER.debug("Empty content, no file will be created.");
            return;
        }
        
        File outputFile = new File(filePath);
        OutputStreamWriter osWriter = null;
        try {
            // Add a line separator if the outputFile has already content
            String appendedContent = LINE_SEPARATOR;
            // if outputFile doesnt exists, then create it
            if (!outputFile.exists()) {
                createFileIfNotExists(filePath);
                appendedContent = content;
            } else {
                appendedContent = appendedContent + content;
            }
            
            osWriter = new OutputStreamWriter(
                    new FileOutputStream(outputFile), Charset.forName("UTF-8").newEncoder());
            
            osWriter.write(appendedContent);
            osWriter.close();
        } catch (IOException ex) {
            LOGGER.error("Throwing exception {}", ex);
        } finally {
            try {
                if (osWriter != null) {
                    osWriter.flush();
                    osWriter.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Closing objects {}", ex);
            }
        }
    }
    
    /**
     * 
     * @param filename
     * @param tweetLineList
     * @param addLineSeparator
     * @return 
     */
    public static boolean createFile(final String filename, List<String> tweetLineList, boolean addLineSeparator) {
        boolean success = true;
        PrintWriter writer;
        FileOutputStream fop = null;
        File file;
        try {
            file = new File(filename);
            // If the outputFile does not exist, create it
            if (!file.exists()) {
                success = createFileIfNotExists(filename);
            }
            fop = new FileOutputStream(file);

            writer = new PrintWriter(fop);
            for (String tweet:tweetLineList) {
                if (addLineSeparator) {
                    writer.println(tweet);
                } else {
                    writer.print(tweet);
                }                
            }
            writer.close();
            
        } catch (FileNotFoundException ex) {
            LOGGER.error("Throwing exception {}", ex);
            success = false;
        } finally {
            try {
                if (fop != null) {
                    fop.close();
                }
            } catch (IOException ex) {
                LOGGER.error("Throwing exception {}", ex);
                success = false;
            }
        }
        return success;
    }
    
    /**
     * Checks if a string is a sequence of digits
     * 
     * @param str
     * @return 
     */
    public static boolean isStringNumeric( String str ) {
        
        if ((str == null) || (str.trim().length() == 0)) {
            return false;
        }
        
        String strWithoutSpace = str.replaceAll(" ", "");
        
        DecimalFormatSymbols currentLocaleSymbols = DecimalFormatSymbols.getInstance();
        char localeMinusSign = currentLocaleSymbols.getMinusSign();

        if ( !Character.isDigit( strWithoutSpace.charAt( 0 ) ) && strWithoutSpace.charAt( 0 ) != localeMinusSign ) {
            return false;
        }

        boolean isDecimalSeparatorFound = false;
        char localeDecimalSeparator = currentLocaleSymbols.getDecimalSeparator();

        for ( char c : strWithoutSpace.substring( 1 ).toCharArray() ) {
            if ( !Character.isDigit( c ) ) {
                if ( ((c == localeDecimalSeparator) || (c == '.')) && !isDecimalSeparatorFound ) {
                    isDecimalSeparatorFound = true;
                    continue;
                }
                return false;
            }
        }
        return true;
    }    
    
    /**
     * 
     * @param list
     * @return 
     */
    public static String prettyPrintList(List list) {
        
       if (!CollectionUtils.isEmpty(list)) {
           return getTokenArrayAsString(list.toArray(new Object[list.size()]), ", ");
       }
       
       return null;
    }
    
    /**
     * 
     * @param input
     * @param replacementString
     * @return 
     */
    public static String removeLineBreaks(String input, final String replacementString){
        return input.replace(LINE_SEPARATOR, replacementString).
                    replace("\n", replacementString).replace("\r", replacementString);
    }
    
    /**
     * Replaces 3 or more occurrences of the same character with the character itself
     * 
     * @param input
     * @param pattern
     * @return 
     */
    public static String replaceRepeatedAdjacentLetters(String input, Pattern pattern) {
        if (StringUtils.isNotBlank(input) && (pattern != null)) {
            final String originalInput = input;
            Matcher matcher = pattern.matcher(input);
            
            while(matcher.find()) {
                // The reason is that words with 2 consecutive characters (e.g. common, occurrences) should not be affected
                if ((matcher.end() - matcher.start())> 2) {
                    String replacement = originalInput.substring(matcher.start(), matcher.start()+1);
                    String repetitionCharactersSubstring = originalInput.substring(matcher.start(), matcher.end());
                    input = input.replace(repetitionCharactersSubstring, replacement);
                }
            }
        }
        return input;
    }
    
    /**
     * List of common twitter swearing words
     * @return 
     */
    public static List getTwitterSwearWordsNegOnly() {
        List<String> swearWords = new ArrayList<String>();
        
        swearWords.add("shitty");
        swearWords.add("shitting");
        swearWords.add("shits");
        swearWords.add("fatass");
        swearWords.add("bitch");
        swearWords.add("b*tch");
        swearWords.add("b!tch");
        swearWords.add("bi+ch");
        swearWords.add("l3itch");
        
        swearWords.add("crap");
        swearWords.add("cr@p");
        swearWords.add("cr*p");
        
        swearWords.add("piss");
        swearWords.add("cockhead");
        swearWords.add("cock-head");
        
        swearWords.add("pr1ck");
        swearWords.add("prick");
        swearWords.add("pr1k");
        swearWords.add("pr1c");
        
        swearWords.add("puta");
        
        swearWords.add("fag");
        swearWords.add("f*g");
        swearWords.add("faggot");
        swearWords.add("faggit");
        
        swearWords.add("bastard");
        swearWords.add("b#stard");
        swearWords.add("b*stard");
        swearWords.add("bassterds");
        swearWords.add("bastards");
        swearWords.add("bastardz");
        swearWords.add("basterds");
        
        swearWords.add("slut");
        swearWords.add("s!ut");
        swearWords.add("sluts");
        swearWords.add("slutty");
        
        swearWords.add("douche");
        swearWords.add("cunt");
        swearWords.add("butthole");
        swearWords.add("buttwipe");
        
        swearWords.add("bugger");
        swearWords.add("b*gger");
        
        swearWords.add("bollock");
        swearWords.add("bollocks");
        swearWords.add("arsehol");
        swearWords.add("asshole");
        swearWords.add("@sshole");
        swearWords.add("a$$h0!e");
        swearWords.add("a55hole");
        swearWords.add("ahole");
        swearWords.add("ash0le");
        swearWords.add("ash0lez");
        swearWords.add("azzhole");
        swearWords.add("asswipe");
        
        swearWords.add("dumbass");
        swearWords.add("dumbasses");
        
        swearWords.add("stupid");
        swearWords.add("stpid");
        swearWords.add("st*pid");
        swearWords.add("st-pid");
        
        swearWords.add("a$$");
        swearWords.add("ass");
        swearWords.add("@$$");
        
        swearWords.add("pimp");
        
        swearWords.add("suck");
        swearWords.add("s*ck");
        
        swearWords.add("whore");
        swearWords.add("whoar");
        swearWords.add("wh0re");
        swearWords.add("hoar");
        swearWords.add("hoor");
        swearWords.add("hoore");
        
        swearWords.add("jackoff");
        
        swearWords.add("son-of-a-bitch");
        
        swearWords.add("retard");
        swearWords.add("r*tard");
        
        return swearWords;
    }    
    
    /**
     * List of common twitter swearing words
     * @return 
     */
    public static List getTwitterSwearWords() {
        List<String> swearWords = new ArrayList<String>();
        
        swearWords.add("shit");
        swearWords.add("sh*t");
        swearWords.add("sh!t");
        swearWords.add("$h1t");
        swearWords.add("shitty");
        swearWords.add("sh!t*");
        swearWords.add("sh!+");
        swearWords.add("shi+");
        swearWords.add("sh1t");
        
        swearWords.add("motherfuck");
        swearWords.add("mofuck");
        
        swearWords.add("fatass");
        
        swearWords.add("fuck");
        swearWords.add("f*ck");
        swearWords.add("f**k");
        swearWords.add("fuckk");
        swearWords.add("f%ck");
        swearWords.add("f.ck");
        swearWords.add("f#ck");
        swearWords.add("f'ck");
        swearWords.add("fu(*");
        
        swearWords.add("Phukker");
        swearWords.add("Phuker");
        swearWords.add("Phuk");
        swearWords.add("Phuck");
        swearWords.add("Phuc");
        
        swearWords.add("damn");
        
        swearWords.add("bitch");
        swearWords.add("b*tch");
        swearWords.add("b!tch");
        swearWords.add("bi+ch");
        swearWords.add("l3itch");
        
        swearWords.add("crap");
        swearWords.add("cr@p");
        swearWords.add("cr*p");
        
        swearWords.add("piss");
        swearWords.add("dick");
        swearWords.add("d*ck");
        
        swearWords.add("darn");
        swearWords.add("cock");
        swearWords.add("c0ck");
        swearWords.add("cockhead");
        swearWords.add("cock-head");
        swearWords.add("cocks");
        
        swearWords.add("pussy");
        swearWords.add("p*ssy");
        swearWords.add("pussee");
        swearWords.add("pusse");
        
        swearWords.add("pr1ck");
        swearWords.add("prick");
        swearWords.add("pr1k");
        swearWords.add("pr1c");
        
        swearWords.add("puta");
        
        swearWords.add("fag");
        swearWords.add("f*g");
        swearWords.add("faggot");
        swearWords.add("faggit");
        
        swearWords.add("bastard");
        swearWords.add("b#stard");
        swearWords.add("b*stard");
        swearWords.add("bassterds");
        swearWords.add("bastards");
        swearWords.add("bastardz");
        swearWords.add("basterds");
        
        swearWords.add("slut");
        swearWords.add("s!ut");
        swearWords.add("sluts");
        swearWords.add("slutty");
        
        swearWords.add("douche");
        swearWords.add("cunt");
        swearWords.add("cum");
        swearWords.add("butthole");
        swearWords.add("buttwipe");
        
        swearWords.add("bugger");
        swearWords.add("b*gger");
        
        swearWords.add("bollock");
        swearWords.add("bollocks");
        swearWords.add("arsehol");
        swearWords.add("asshole");
        swearWords.add("@sshole");
        swearWords.add("a$$h0!e");
        swearWords.add("a55hole");
        swearWords.add("ahole");
        swearWords.add("ash0le");
        swearWords.add("ash0lez");
        swearWords.add("azzhole");
        swearWords.add("asswipe");
        
        swearWords.add("dyke");
        
        swearWords.add("dumbass");
        swearWords.add("dumbasses");
        
        swearWords.add("damn");
        swearWords.add("d*mn");
        
        swearWords.add("stupid");
        swearWords.add("stpid");
        swearWords.add("st*pid");
        swearWords.add("st-pid");
        
        swearWords.add("a$$");
        swearWords.add("ass");
        swearWords.add("@$$");
        
        swearWords.add("pimp");
        
        swearWords.add("blowjob");
        
        swearWords.add("crap");
        
        swearWords.add("suck");
        swearWords.add("s*ck");
        
        swearWords.add("piss");
        swearWords.add("whore");
        swearWords.add("whoar");
        swearWords.add("wh0re");
        swearWords.add("hoar");
        swearWords.add("hoor");
        swearWords.add("hoore");
        
        swearWords.add("jackoff");
        
        swearWords.add("son-of-a-bitch");
        
        swearWords.add("retard");
        swearWords.add("r*tard");
        
        return swearWords;
    }
    
    /**
     * Negation list used for Sentiment Analysis purposes as described in the papers
     * "Sentiment Symposium Tutorial: Linguistic structure" and "A Survey on the Role of Negation in Sentiment Analysis"
     * 
     * @return 
     */
    public static List getNegationList() {
        List<String> negationsList = new ArrayList<String>();
        
        negationsList.add("never");
        negationsList.add("no");
        negationsList.add("nothing");
        negationsList.add("nowhere");
        negationsList.add("noone");
        negationsList.add("none");
        negationsList.add("not");
        
        negationsList.add("havent");
        negationsList.add("haven't");
        negationsList.add("hasnt");
        negationsList.add("hasn't");
        negationsList.add("hadnt");
        negationsList.add("hadn't");
        negationsList.add("cant");
        negationsList.add("can't");
        negationsList.add("couldnt");
        negationsList.add("couldn't");
        negationsList.add("shouldnt");
        negationsList.add("shouldn't");
        
        negationsList.add("wont");
        negationsList.add("won't");
        negationsList.add("wouldnt");
        negationsList.add("wouldn't");
        negationsList.add("dont");
        negationsList.add("don't");
        negationsList.add("doesnt");
        negationsList.add("doesn't");
        negationsList.add("didnt");
        negationsList.add("didn't");
        negationsList.add("isnt");
        negationsList.add("isn't");
        negationsList.add("arent");
        negationsList.add("aren't");
        negationsList.add("aint");
        negationsList.add("ain't");
        
        return negationsList;
    }
    
    /**
     * Punctuation marks list used as a termination condition in the negation feature
     * 
     * @return 
     */
    public static List getPunctuationMarks() {
        List<String> punctuationMarksList = new ArrayList<String>();
        punctuationMarksList.add(".");
        punctuationMarksList.add("..");
        punctuationMarksList.add("...");
        punctuationMarksList.add(":");
        punctuationMarksList.add(";");
        punctuationMarksList.add("!");
        punctuationMarksList.add("?");
        
        return punctuationMarksList;
    }

    /**
     * 
     * @return 
     */
    public static List<String> getNegativeEmoticons() {
        List<String> negativeEmoticonsList = new ArrayList<String>();
        negativeEmoticonsList.add(">:[");
        negativeEmoticonsList.add(":-(");
        negativeEmoticonsList.add(":(");
        negativeEmoticonsList.add(":-c");
        negativeEmoticonsList.add(":c");
        negativeEmoticonsList.add(":-<");
        negativeEmoticonsList.add(":<");
        negativeEmoticonsList.add(":-[");
        negativeEmoticonsList.add(":[");
        negativeEmoticonsList.add(":{");
        negativeEmoticonsList.add(">.>");
        negativeEmoticonsList.add("<.<");
        negativeEmoticonsList.add(">.<");
        negativeEmoticonsList.add(":-||");
        negativeEmoticonsList.add("D:<");
        negativeEmoticonsList.add("D:");
        negativeEmoticonsList.add("D8");
        negativeEmoticonsList.add("D;");
        negativeEmoticonsList.add("D=");
        negativeEmoticonsList.add("DX");
        negativeEmoticonsList.add("v.v");
        negativeEmoticonsList.add("D-\':");
        negativeEmoticonsList.add("</3");
        negativeEmoticonsList.add("(-_-\')");
        negativeEmoticonsList.add("(-_-;)");
        negativeEmoticonsList.add("(o_O)");
        negativeEmoticonsList.add(":@");
        negativeEmoticonsList.add("o-o");
        negativeEmoticonsList.add("-_-");        
        
        return negativeEmoticonsList;
    }

    /**
     * 
     * @return 
     */
    public static List<String> getPositiveEmoticons() {
        List<String> positiveEmoticonsList = new ArrayList<String>();
        
        positiveEmoticonsList.add(">:]");
        positiveEmoticonsList.add(":-)");
        positiveEmoticonsList.add(":)");
        positiveEmoticonsList.add(":o)");
        positiveEmoticonsList.add(":]");
        positiveEmoticonsList.add(":3");
        positiveEmoticonsList.add(":c)");
        positiveEmoticonsList.add(":>");
        positiveEmoticonsList.add("=]");
        positiveEmoticonsList.add("8)");
        positiveEmoticonsList.add("=)");
        positiveEmoticonsList.add(":}");
        positiveEmoticonsList.add(":^)");
        positiveEmoticonsList.add(">:D");
        positiveEmoticonsList.add(":-D");
        positiveEmoticonsList.add(":D");
        positiveEmoticonsList.add("8-D");
        positiveEmoticonsList.add("8D");
        positiveEmoticonsList.add("x-D");
        positiveEmoticonsList.add("xD");
        positiveEmoticonsList.add("X-D");
        positiveEmoticonsList.add("XD");
        positiveEmoticonsList.add("=-3");
        positiveEmoticonsList.add("=3");
        positiveEmoticonsList.add("8-)");
        positiveEmoticonsList.add(":-))");
        positiveEmoticonsList.add("<3");
        positiveEmoticonsList.add("(-;");        
        positiveEmoticonsList.add(": )");
        positiveEmoticonsList.add(";)");
        positiveEmoticonsList.add("<3");
        positiveEmoticonsList.add(":P");
        positiveEmoticonsList.add(";-)");
        positiveEmoticonsList.add(":*");
        positiveEmoticonsList.add("C:");
        positiveEmoticonsList.add("♥");
        
        return positiveEmoticonsList;
    }
    
    /**
     * 
     * @param filePath
     * @return 
     */
    public static List<String> readFile(String filePath) {
        
        List<String> list = new ArrayList<String>();
        Charset charset = Charset.forName("UTF-8");
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    new FileInputStream(filePath), charset));
            
            String line = null;
            while((line = br.readLine()) != null) {
                list.add(line);
            }
        } catch (FileNotFoundException ex) {
            LOGGER.error("Throwing FileNotFoundException ", ex);
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("Throwing UnsupportedEncodingException ", ex);
        } catch (IOException ex) {
            LOGGER.error("Throwing IOException ", ex);
        }
        
        return list;        
    }
    
    /**
     * Sort a map by values
     * 
     * @param map
     * @return 
     */
    public static Map sortByValueDescending(Map map) {
        List list = new ArrayList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue()).compareTo(((Map.Entry) (o2)).getValue());
            }
        });
         
        Map result = new LinkedHashMap(); // Retain the insertion order
        List valuesSortedDescending = new ArrayList();
        List keysSortedDescending = new ArrayList();
        
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry)it.next();
            // Ascending order
            valuesSortedDescending.add(entry.getValue());
            keysSortedDescending.add(entry.getKey());
        }
        
        // Descending order
        Collections.reverse(valuesSortedDescending);
        Collections.reverse(keysSortedDescending);
        
        for (int i = 0; i < valuesSortedDescending.size(); i++) {
            result.put(keysSortedDescending.get(i), valuesSortedDescending.get(i));
        }
        
        return result;
    }
    
    /**
     * Calculate the cosine similarity between 2 vectors
     * 
     * @param v1
     * @param v2
     * @return 
     */
    public static double cosineSimilarity(Map<String, Double> v1, Map<String, Double> v2) {
        Set<String> both = Sets.newHashSet(v1.keySet());
        // Get the elemets that are contained in both collections, their intersection 
        both.retainAll(v2.keySet());
        double sclar = 0, norm1 = 0, norm2 = 0;
        
        for (String k : both){
            sclar += v1.get(k) * v2.get(k);
        }
        for (String k : v1.keySet()){
            norm1 += v1.get(k) * v1.get(k);
        }
        for (String k : v2.keySet()){
            norm2 += v2.get(k) * v2.get(k);
        }
        return sclar / Math.sqrt(norm1 * norm2);
    }
    
    /**
     * The words do not contain punctuation for easy comparison
     * 
     * @return 
     */
    /**
     * The words do not contain punctuation for easy comparison
     * 
     * @return 
     */
    public static List getGreekSwearWords() {
        
        List<String> swearWords = new ArrayList<String>();
        swearWords.add("αχαριστ");
        swearWords.add("αχρηστ");
        swearWords.add("αχρειος");
        swearWords.add("αχρεια");
        swearWords.add("αρχιδ");
        swearWords.add("αρχειδ");
        swearWords.add("ανικαν");
        swearWords.add("ανωμαλος");
        swearWords.add("ανωμαλη");
        swearWords.add("αλαζονικ");
        swearWords.add("αλητη");
        swearWords.add("αλητισ");
        swearWords.add("αλητια");
        swearWords.add("αλογομουρ");
        swearWords.add("αλλοπροσαλ");
        swearWords.add("αηδια");
        swearWords.add("αθλιο");
        swearWords.add("αθλια");
        swearWords.add("αισχρ");
        swearWords.add("αιμοδιψη");
        swearWords.add("ανανδρ");
        swearWords.add("αναξιο");
        swearWords.add("ανακριβ");
        swearWords.add("αναληθ");
        swearWords.add("ανεπιθυμ");
        swearWords.add("ανοητ");
        swearWords.add("ανοησι");
        swearWords.add("ανιδε");
        swearWords.add("ανικαν");
        swearWords.add("ανυποφ");
        swearWords.add("απατεων");
        swearWords.add("αποκρουστικ");
        swearWords.add("απλυτ");
        swearWords.add("ασχετ");
        swearWords.add("αχαριστ");
        
        swearWords.add("βαρελα");
        swearWords.add("βιζιτ");
        swearWords.add("βλακ");
        swearWords.add("βλαξ");
        swearWords.add("βλαμ");
        swearWords.add("βλαχαδερ");
        swearWords.add("βλημα");
        swearWords.add("βουρλο");
        swearWords.add("βρυκολακ");
        swearWords.add("βρωμο");
        swearWords.add("βλακοκαγκουρ");
        swearWords.add("βλαχοκαγκουρ");
        swearWords.add("βριζ");
        swearWords.add("βρωμιαρ");
        
        swearWords.add("γαμω");
        swearWords.add("γαμια");
        swearWords.add("γαμιολ");
        swearWords.add("γαμηδ");
        swearWords.add("γαμημ");
        swearWords.add("γαμησ");
        swearWords.add("γαμισ");
        swearWords.add("γαμα");
        swearWords.add("γ@μα");
        swearWords.add("γ@μ@");
        swearWords.add("γ@μη");
        swearWords.add("γελοι");
        swearWords.add("γερμανοτσολι");
        swearWords.add("γκαβλα");
        swearWords.add("γκαβλο");
        swearWords.add("γκεουλ");
        swearWords.add("γλιτσας");
        swearWords.add("γλυφτ");
        swearWords.add("γουρουνι");
        swearWords.add("γυναιο");
        swearWords.add("γυφτος");
        swearWords.add("γυφτισ");
        swearWords.add("γυφτικ");
        
        swearWords.add("διαολο");
        swearWords.add("διαβολικ");
        swearWords.add("διαστροφικ");
        swearWords.add("διεστραμμεν");
        swearWords.add("διεστραμεν");
        swearWords.add("δολοφον");
        swearWords.add("δρακουλ");
        swearWords.add("δυστυχ");
        
        swearWords.add("εγκλημα");
        swearWords.add("εγλυψε");
        swearWords.add("ελλειν");
        swearWords.add("εκφυλ");
        swearWords.add("εμετικ");
        swearWords.add("εξευτελ");
        swearWords.add("ενδοπαλαμιος");
        swearWords.add("επρηξ");
        
        swearWords.add("ζωντοβολ");
        
        swearWords.add("ηλιθιο");
        swearWords.add("ηλιθια");
        
        swearWords.add("θλιβερ");
        swearWords.add("θλιψη");
        
        swearWords.add("κακε");
        swearWords.add("κακη");
        swearWords.add("κακο");
        swearWords.add("καθαρμ");
        swearWords.add("καραφλ");
        swearWords.add("καργιολη");
        swearWords.add("καριολο");
        swearWords.add("κατινα");
        swearWords.add("καφρος");
        swearWords.add("καφριλα");
        swearWords.add("καθικι");
        swearWords.add("καθυστερημεν");
        swearWords.add("κακασχημ");
        swearWords.add("κανιβαλος");
        swearWords.add("καποτα");
        swearWords.add("καυλι");
        swearWords.add("καυλωμεν");
        swearWords.add("καραβοσκυλο");
        swearWords.add("καραγκιοζ");
        swearWords.add("καραπουταναρ");
        swearWords.add("κατεστραμμεν");
        swearWords.add("καχεκτικο");
        swearWords.add("κερατας");
        swearWords.add("κλαμπαρχιδ");
        swearWords.add("κλασομπανιερ");
        swearWords.add("κλεφτ");
        swearWords.add("κνωδαλ");
        swearWords.add("κοκοτα");
        swearWords.add("κολο");
        swearWords.add("κοροιδ");
        swearWords.add("κουραδ");
        swearWords.add("κουτο");
        swearWords.add("κουτη");
        swearWords.add("κουφαλα");
        swearWords.add("κωλο");
        swearWords.add("κωλαντερο");
        swearWords.add("κ@λ@");
        swearWords.add("κομπλεξικ");
        swearWords.add("κοντοπουταν");
        swearWords.add("κοπανος");
        swearWords.add("κοπρο");
        swearWords.add("κoυτορνιθ");
        swearWords.add("κυνικ");
        swearWords.add("κωμικοτραγικ");
        
        swearWords.add("λαμογι");
        swearWords.add("λινατσα");
        swearWords.add("λοβοτομημ");
        swearWords.add("λουγκρα");
        swearWords.add("λυπηση");
        swearWords.add("λωβοτομημενος");
        swearWords.add("λωβοτομημενη");
        
        swearWords.add("μαλακ");
        swearWords.add("μ@λακ");
        swearWords.add("μαλ@κ");
        swearWords.add("μ@λ@κ");
        swearWords.add("μ@@@");
        swearWords.add("μλκ");
        swearWords.add("μουροχαβλ");
        swearWords.add("μπαζο");
        swearWords.add("μπαρουφ");
        swearWords.add("μπασμεν");
        swearWords.add("μπασταρδ");
        swearWords.add("μπ@στ@ρδ");
        swearWords.add("μπετοβλακα");
        swearWords.add("μπουρδελ");
        swearWords.add("μπινες");
        swearWords.add("μωρος");
        swearWords.add("μυγοφτυμα");
        swearWords.add("μπουμπουνα");
        swearWords.add("μπουφος");
        swearWords.add("μπουχεσα");
        swearWords.add("μαζοχα");
        swearWords.add("μαλαπερδα");
        swearWords.add("μαλεας");
        swearWords.add("μαμοθρεφτο");
        swearWords.add("μαμουχαλο");
        swearWords.add("μαντροσκυλο");
        swearWords.add("μουλος");
        swearWords.add("μουνακ");
        swearWords.add("μουν@κ");
        swearWords.add("μουνι");
        swearWords.add("μουνο");
        swearWords.add("μπουλουκο");
        swearWords.add("μπουλουκα");
        swearWords.add("μπουμπουκο");
        swearWords.add("μωρη");
        
        swearWords.add("νταβατζ");
        swearWords.add("ντιντης");
        swearWords.add("νεκρο");
        swearWords.add("νεκρη");
        swearWords.add("νεκρα");
        
        swearWords.add("ξεδιαντροπ");
        swearWords.add("ξενοδουλ");
        swearWords.add("ξετσουτσουνισμεν");
        swearWords.add("ξεκωλο");
        swearWords.add("ξεπουλημενος");
        swearWords.add("ξερατα");
        swearWords.add("ξερατο");
        swearWords.add("ξεσκισμενη");
        swearWords.add("ξεφτιλ");
        swearWords.add("ξυπνοπουλ");
        
        swearWords.add("οπισθοβαλλομεν");
        
        swearWords.add("παπαρια");
        swearWords.add("παλαβ");
        swearWords.add("παλιογαμ");
        swearWords.add("παλιοκαριολ");
        swearWords.add("παλιομουν");
        swearWords.add("παλιοποζερ");
        swearWords.add("παλιομουνοσκεπασμα");
        swearWords.add("παλιομουνοπαν");
        swearWords.add("παλιομουνογλυφτ");
        swearWords.add("παλιομουναρ");
        swearWords.add("παλιομαλακισμεν");
        swearWords.add("παλιομαλακα");
        swearWords.add("παλιολινατσα");
        swearWords.add("παλιοκερατουκλ");
        swearWords.add("παλιομουνοψωλλαρπαχτρ");
        swearWords.add("παλιομπαζ");
        swearWords.add("παλιομπασταρδομουν");
        swearWords.add("παλιοπαιδ");
        swearWords.add("παλιοντενεκες");
        swearWords.add("παλιομπινες");
        swearWords.add("παλιομπιζμπικης");
        swearWords.add("παλιομπασταρδ");
        swearWords.add("παλιοκαριολ");
        swearWords.add("παλιοκαργια");
        swearWords.add("παλιοαρχιδ");
        swearWords.add("παλιοgay");
        swearWords.add("παλινδρομιστης");
        swearWords.add("παλιοβλαχος");
        swearWords.add("παλιοβουτυροπαιδο");
        swearWords.add("παλιογαργαντουα");
        swearWords.add("παλιογαμιολη");
        swearWords.add("παλιοβρωμοπουστα");
        swearWords.add("παλιοβρωμιαρ");
        swearWords.add("παλιοπορδη");
        swearWords.add("παλιοτραμπουκ");
        swearWords.add("παλιοστραβοχυμεν");
        swearWords.add("παλιοσκουληκ");
        swearWords.add("παλιοσαβουρ");
        swearWords.add("παλιοπουταν");
        swearWords.add("παλιοτροβαδουρος");
        swearWords.add("παλιοτρομπα");
        swearWords.add("παλιοτσουλακ");
        swearWords.add("παλιοψωλλα");
        swearWords.add("παλιοψοφιμι");
        swearWords.add("παλιοχλεμπονιαρ");
        swearWords.add("παλιοχαρακτηρ");
        swearWords.add("παλιοφλωρακ");
        swearWords.add("παλιοτυροσαυρ");
        swearWords.add("παλιοπουσταρ");
        swearWords.add("π@π@ρια");
        swearWords.add("π@παρ");
        swearWords.add("παρανοια");
        swearWords.add("παρανοικ");
        swearWords.add("πατσαβουρα");
        swearWords.add("παχυδερμ");
        swearWords.add("πισωκουνα");
        swearWords.add("πισωκολλημεν");
        swearWords.add("πισωγλεντ");
        swearWords.add("πιπωμεν");
        swearWords.add("πλακομουνι");
        swearWords.add("ποταπο");
        swearWords.add("πουσταρ");
        swearWords.add("πουστι");
        swearWords.add("πουστη");
        swearWords.add("πουτσ");
        swearWords.add("πουταν");
        swearWords.add("πουτ@ν@");
        swearWords.add("πορνη");
        swearWords.add("πορδοβουλωμα");
        swearWords.add("πορνιδιο");
        swearWords.add("πορνογερος");
        swearWords.add("πρεζα");
        swearWords.add("προβοκατορ");
        swearWords.add("προδοτ");
        swearWords.add("πρωκτολη");
        swearWords.add("πρηξαρχιδ");
        swearWords.add("πηδιολα");
        swearWords.add("πεογαλο");
        swearWords.add("παπατζη");
        swearWords.add("πατσαβουρα");
        swearWords.add("παρτουζ");
        swearWords.add("παρθενοπιπιτσα");
        swearWords.add("πεοδουλ");
        swearWords.add("πεοθηλαζ");
        swearWords.add("πεοθηλαστηρ");
        swearWords.add("πηδηχτουλ");
        swearWords.add("πετουγια");
        swearWords.add("πεος");
        swearWords.add("πεοσυλλεκτ");
        swearWords.add("πεολαγν");
        swearWords.add("πυροβολημενο");
        
        swearWords.add("ρεμαλι");
        swearWords.add("ρουφοκαβλετα");
        swearWords.add("ρουφοπηδιολα");
        
        swearWords.add("σαβουρογαμ");
        swearWords.add("σαπιλα");
        swearWords.add("σαπιοκωλακ");
        swearWords.add("σατυρος");
        swearWords.add("σαχλο");
        swearWords.add("σαχλη");
        swearWords.add("σεξοπορνοδιαστροφικ");
        swearWords.add("σιχαμεν");
        swearWords.add("σιχτηροζαδωμαζωχτρα");
        swearWords.add("σκασε");
        swearWords.add("σκατα");
        swearWords.add("σκ@τ@");
        swearWords.add("σκατωμεν");
        swearWords.add("σκατιαρ");
        swearWords.add("σκατο");
        swearWords.add("σκασμο");
        swearWords.add("σιχαμ");
        swearWords.add("σιχαθ");
        swearWords.add("σκορδοπουτσογλου");
        swearWords.add("σκουληκαντερο");
        swearWords.add("σκροφα");
        swearWords.add("σκυλαραπα");
        swearWords.add("σκυλομπασταρδο");
        swearWords.add("σκυλοπηδημενη");
        swearWords.add("σκυλιαζ");
        swearWords.add("σκορδοκαηλας");
        swearWords.add("σιχαμεν");
        swearWords.add("σπερματοδοχειο");
        swearWords.add("σπερματοζητιανα");
        swearWords.add("σπερματοκανατα");
        swearWords.add("σπερματοκαταβοθρα");
        swearWords.add("σπερματοκαταπιολ");
        swearWords.add("σπερματοσταλακτ");
        swearWords.add("σπερμοποτ");
        swearWords.add("σταχτοκαριολαρ");
        swearWords.add("σπασαρχιδ");
        swearWords.add("σουφρωξεκωλιασμα");
        swearWords.add("σουργελο");
        swearWords.add("σουφρα");
        swearWords.add("στραβογαμ");
        swearWords.add("στραβοχυμεν");
        swearWords.add("συμμορι");
        swearWords.add("συφηλιαρ");
        swearWords.add("συφιλιασμεν");
        swearWords.add("σχιζοφρεν");
        
        swearWords.add("ταβανοπροκα");
        swearWords.add("τεμπελ");
        swearWords.add("τιποτενι");
        swearWords.add("τυρογαμηκουλ");
        swearWords.add("τυροβλαχ");
        swearWords.add("τσουτσουν");
        swearWords.add("τσουλα");
        swearWords.add("τσογλαν");
        swearWords.add("τσιμπουκοχειλ");
        swearWords.add("τσιμπουκι");
        swearWords.add("τρισαθλι");
        swearWords.add("τραμπουκ");
        swearWords.add("τραβελογαμηκουλ");
        swearWords.add("τρυπομουνα");
        swearWords.add("τσαντιρογυφτ");
        swearWords.add("τσιμπουκο");
        swearWords.add("τσιμπουκλ");
        swearWords.add("τσιμπουκι");
        swearWords.add("τσιμεντομαλακας");
        swearWords.add("τσαπερδονοκολοσφυριχτρα");
        swearWords.add("τραγικ");
        swearWords.add("τρομακτικ");
        
        swearWords.add("υποχονδρι");
        swearWords.add("υποκριτης");
        swearWords.add("υποκριτρια");
        swearWords.add("υστερικ");
        
        swearWords.add("φονια");
        swearWords.add("φονισ");
        swearWords.add("φακλανα");
        swearWords.add("φλομπα");
        swearWords.add("φλωρος");
        swearWords.add("φτωχομπιν");
        
        swearWords.add("χαζε");
        swearWords.add("χαζο");
        swearWords.add("χαζη");
        swearWords.add("χαιβανι");
        swearWords.add("χαζογκομενα");
        swearWords.add("χοντροχαμηλοκωλ");
        swearWords.add("χτικιαρ");
        swearWords.add("χυσι");
        swearWords.add("χυσοκαταπινοβα");
        swearWords.add("χυσοξεκωλιασμεν");
        swearWords.add("χυσοποτ");
        swearWords.add("χυσοσπαρμενη");
        swearWords.add("χοντρο");
        swearWords.add("χλαπατσα");
        swearWords.add("χαζο");
        swearWords.add("χαμουρ");
        swearWords.add("χαποπουτσης");
        swearWords.add("χαφιε");
        swearWords.add("χεστρα");
        swearWords.add("χεστηκ");
        swearWords.add("χιλιογαμημεν");
        
        swearWords.add("ψεκασμεν");
        swearWords.add("ψευτη");
        swearWords.add("ψευτε");
        swearWords.add("ψευτρα");
        swearWords.add("ψευτο");
        swearWords.add("ψευτικ");
        swearWords.add("ψιλοφλωρ");
        swearWords.add("ψυχακια");
        swearWords.add("ψυχοπαθ");
        swearWords.add("ψωλλη");
        swearWords.add("ψωλη");
        swearWords.add("ψ@λη");
        swearWords.add("ψωλλο");
        swearWords.add("ψωλλοχαστουκ");
        swearWords.add("ψωλλοχυμεν");
        swearWords.add("ψωλλαρπαχτρ");
        swearWords.add("ψωλλαρχιδ");
        swearWords.add("ψωλλοκοπαν");
        swearWords.add("ψωριαρ");
        swearWords.add("ψωρο");
        
        return swearWords;
    }
    
    public static List<String> getGreekNegations() {
        
        List<String> negations = new ArrayList<String>();
//        negations.add("όχι");
//        negations.add("οχι");
//        negations.add("μη");
        negations.add("μην");
//        negations.add("δεν");
//        negations.add("δε");
        
        return negations;
    }    
    
//    public static List<String> getBanksTokens() {
//        List banks = new ArrayList();
//        banks.add("Ergasias");
//        
//        return banks;
//    }
//    
//    public static List<String> getBanksTokensIgnoreCase() {
//        List banks = new ArrayList();
//        banks.add("Eurobank");
//        
//        return banks;
//    }            
}
