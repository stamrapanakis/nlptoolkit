
package gr.atc.nlptoolkit.language;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.slf4j.LoggerFactory;

/**
 * Parses the Europarl corpus (http://www.statmt.org/europarl/). This corpus consists of (parallel) translations
 * of European Parliament proceedings for the 1996-2006 period. It is a perfect candidate for our learning
 * algorithm, with up to 44 million words per language.
 *
 * Training takes less than 5 minutes on my computer, with a quad core processor. The loader has been optimized
 * for multi-core systems.
 *
 * @author Cedric CHAMPEAU<cedric-dot-champeau-at-laposte.net>
 */
public class LingwayLanguageDetector implements LanguageDetector{
    
    private LangDetector detector;
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LingwayLanguageDetector.class);
    
    /**
     * 
     * @param modelFilePath 
     */
    @Override
    public void register(String modelFilePath) {
        this.detector = new LangDetector();
        setModels(modelFilePath);
    }
    
    private void setModels(String modelDataPath) {
        Gson gson = new GsonBuilder().create();
        String fileNamePath = modelDataPath+"/europarl/out";
        File in = new File(fileNamePath);
        for (File file : in.listFiles()) {
            try {
                // Read the language files
                String fileName = file.getName().substring(0,2);
                // Convert the file stream to a json string to reveal its internal structure
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                GramTree myGramtree = gson.fromJson(reader, GramTree.class);
                detector.register(fileName, myGramtree);
            } catch (FileNotFoundException ex) {
                LOGGER.error("Exception in registering languages in detector {}", ex);
            }
        }
    }
    
    /**
     * 
     * @param tweetText
     * @return 
     */
    @Override
    public boolean isEnglishText(final String tweetText){
        boolean result = false;
        try {
            result = "en".equals(detector.detectLang(tweetText));
        } catch (NullPointerException ex) {
            LOGGER.error("Exception during language detection ", ex);
        }
        return result;
    }    

    @Override
    public boolean isGreekText(String tweetText) {
        boolean result = false;
        try {
            result = "el".equals(detector.detectLang(tweetText));
        } catch (NullPointerException ex) {
            LOGGER.error("Exception during language detection ", ex);
        }
        return result;
    }

}
