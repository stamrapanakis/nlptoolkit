
package gr.atc.nlptoolkit.language;

/**
 * 
 * @author Stamatis
 */
public interface LanguageDetector {
    
    /**
     * Initialize the detector by reading language specific files
     * 
     * @param modelFilePath
     */
    public void register(String modelFilePath);
    
    /**
     * Returns true if the text is in English
     * 
     * @param tweetText Text to determine the language
     * @return 
     */
    public boolean isEnglishText(String tweetText);
    
    /**
     * Returns true if the text is in Greek
     * 
     * @param tweetText Text to determine the language
     * @return 
     */
    public boolean isGreekText(String tweetText);
}


