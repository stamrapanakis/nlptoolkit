/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.atc.nlptoolkit.stylometry;

import cmu.arktweetnlp.Twokenize;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Contains functions with regex for detection of emoticons and other Twitter related symbols
 * See the class cmu.arktweetnlp.Twokenize
 * 
 * @author SRapanakis
 */
public class EmoticonsDetector {
    
    static Pattern Whitespace = Pattern.compile("[\\s\\p{Zs}]+");
    static String Hearts = "(?:<+/?3+)+";
    
    //  Emoticons
    static String normalEyes = "(?iu)[:=]"; // 8 and x are eyes but cause problems
    static String wink = "[;]";
    static String noseArea = "(?:|-|[^a-zA-Z0-9 ])"; // doesn't get :'-(
    static String happyMouths = "[D\\)\\]\\}]+";
    static String sadMouths = "[\\(\\[\\{]+";
    static String tongue = "[pPd3]+";
    static String otherMouths = "(?:[oO]+|[/\\\\]+|[vV]+|[Ss]+|[|]+)"; // remove forward slash if http://'s aren't cleaned

    // mouth repetition examples:
    // @aliciakeys Put it in a love song :-))
    // @hellocalyclops =))=))=)) Oh well

    static String bfLeft = "(♥|0|o|°|v|\\$|t|x|;|\\u0CA0|@|ʘ|•|・|◕|\\^|¬|\\*)";
    static String bfCenter = "(?:[\\.]|[_-]+)";
    static String bfRight = "\\2";
    static String s3 = "(?:--['\"])";
    static String s4 = "(?:<|&lt;|>|&gt;)[\\._-]+(?:<|&lt;|>|&gt;)";
    static String s5 = "(?:[.][_]+[.])";
    static String basicface = "(?:(?i)" +bfLeft+bfCenter+bfRight+ ")|" +s3+ "|" +s4+ "|" + s5;

    static String eeLeft = "[＼\\\\ƪԄ\\(（<>;ヽ\\-=~\\*]+";
    static String eeRight= "[\\-=\\);'\\u0022<>ʃ）/／ノﾉ丿╯σっµ~\\*]+";
    static String eeSymbol = "[^A-Za-z0-9\\s\\(\\)\\*:=-]";
    static String eastEmote = eeLeft + "(?:"+basicface+"|" +eeSymbol+")+" + eeRight;
    static String Arrows = "(?:<*[-―—=]*>+|<+[-―—=]*>*)|\\p{InArrows}+";
    static String decorations = "(?:[♫♪]+|[★☆]+|[♥❤♡]+|[\\u2639-\\u263b]+|[\\ue001-\\uebbb]+)";
    
    private static final String EMOTICON = regexOR(// Standard version  :) :( :] :D :P
    		"(?:>|&gt;)?" + regexOR(normalEyes, wink) + regexOR(noseArea,"[Oo]") + 
            	regexOR(tongue+"(?=\\W|$|RT|rt|Rt)", otherMouths+"(?=\\W|$|RT|rt|Rt)", sadMouths, happyMouths),

            // reversed version (: D:  use positive lookbehind to remove "(word):" because eyes on the right side is more ambiguous with the standard usage of : ;
            "(?<=(?: |^))" + regexOR(sadMouths,happyMouths,otherMouths) + noseArea + regexOR(normalEyes, wink) + "(?:<|&lt;)?",

            //inspired by http://en.wikipedia.org/wiki/User:Scapler/emoticons#East_Asian_style
            eastEmote.replaceFirst("2", "1"), basicface
            // iOS 'emoji' characters (some smileys, some symbols) [\ue001-\uebbb]  
            // TODO should try a big precompiled lexicon from Wikipedia, Dan Ramage told me (BTO) he does this
    );

    private EmoticonsDetector() {
    }
    
    public static String regexOR(String... parts) {
        String prefix="(?:";
        StringBuilder sb = new StringBuilder();
        for (String s:parts){
            sb.append(prefix);
            prefix="|";
            sb.append(s);
        }
        sb.append(")");
        return sb.toString();
    }
    
    static Pattern emoticonsHearts  = Pattern.compile(regexOR(Hearts,
                    EMOTICON
            ));
    
    static Pattern decoratorsArrows  = Pattern.compile(regexOR(
                Arrows,
                decorations
            )); 
    
    /**
     * 
     * @param text
     * @return 
     */
    public static int getNumberOfEmoticons(String text){
        
        int numberOfEmoticons = 0;
        if (StringUtils.isNotBlank(text)){
            
            // Twitter text comes HTML-escaped, so unescape it.
            // We also first unescape &amp;'s, in case the text has been buggily double-escaped.
            String normalizedText = Twokenize.normalizeTextForTagger(text);
            
            // "foo   bar " => "foo bar"
            String squeezeWhitespaceText = Twokenize.squeezeWhitespace(normalizedText);
            
            // Used to implement a punctuation policy ("edge punctuation" cases versus word-internal punctuation) 
            String splitEdgePunctText = Twokenize.splitEdgePunct(squeezeWhitespaceText);
            
            // Find the matches for subsequences that should be protected,
            // e.g. URLs, 1.0, U.N.K.L.E., 12:53
            Matcher emoticonsHeartsMatcher = emoticonsHearts.matcher(splitEdgePunctText);
            
            while(emoticonsHeartsMatcher.find()){
                numberOfEmoticons++;
            }
            
        }
        
        return numberOfEmoticons;
    }
    
    public static int getNumberOfDecorators(String text){
        
        int numberOfDecorators = 0;
        if (StringUtils.isNotBlank(text)){
            
            // Twitter text comes HTML-escaped, so unescape it.
            // We also first unescape &amp;'s, in case the text has been buggily double-escaped.
            String unescapeHTML = Twokenize.normalizeTextForTagger(text);
            
            // "foo   bar " => "foo bar"
            String removeWhitespaces = Twokenize.squeezeWhitespace(unescapeHTML);
            
            // Used to implement a punctuation policy ("edge punctuation" cases versus word-internal punctuation) 
            String handlePunctuations = Twokenize.splitEdgePunct(removeWhitespaces);
            
            Matcher decoratorsArrowsMatcher = decoratorsArrows.matcher(handlePunctuations);
            
            while(decoratorsArrowsMatcher.find()){
                numberOfDecorators++;
            }
        }
        
        return numberOfDecorators;
    }
    
    /**
     * 
     * @param text
     * @return 
     */
    public static List<String> tokenizeRawTweetText(String text) {
        if (StringUtils.isNotBlank(text)){
            return Twokenize.tokenizeRawTweetText(text);
        } else {
            return new ArrayList<String>();
        }
    }
}
