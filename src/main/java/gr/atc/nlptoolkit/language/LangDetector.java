
package gr.atc.nlptoolkit.language;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class LangDetector {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(LangDetector.class);
    private final Map<String, GramTree> statsMap = new HashMap<String, GramTree>();
    
    public LangDetector() {
    }
    
    public void register(String lang, ObjectInputStream in) {
        try {
            statsMap.put(lang, (GramTree) in.readObject());
            in.close();
        } catch (IOException ex) {
            LOGGER.error("{}", ex);
        } catch (ClassNotFoundException ex) {
            LOGGER.error("{}", ex);
        }
    }

    public void register(String lang, GramTree tree) {
        statsMap.put(lang, tree);
    }

    public String detectLang(CharSequence aText) {
        double best = 0;
        String bestLang = null;
        for (Map.Entry<String, GramTree> entry : statsMap.entrySet()) {
            
            String languageKey = entry.getKey();
            double score = entry.getValue().scoreText(aText);
            LOGGER.debug("---------- result : "+languageKey+" : "+score+" -------------");
            
            if (score>best) {
                best = score;
                bestLang = entry.getKey();
            }
        }
        return bestLang;
    }    
    
}
