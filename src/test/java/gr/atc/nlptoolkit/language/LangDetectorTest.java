
package gr.atc.nlptoolkit.language;

import gr.atc.nlptoolkit.language.GramTree;
import gr.atc.nlptoolkit.language.LangDetector;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gr.atc.nlptoolkit.utils.ConfigurationUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Stamatis
 */
public class LangDetectorTest {
    
    private static LangDetector langDetector;
    
    private final static Logger LOGGER = LoggerFactory.getLogger(LangDetectorTest.class);
    
    @BeforeClass
    public static void setUpClass() {
        langDetector = new LangDetector();
        
        Gson gson = new GsonBuilder().create();
        String fileNamePath = ConfigurationUtil.getModelsFilePath()+"/europarl/out";
        File in = new File(fileNamePath);
        int numberOfResourceFiles = 11;
        int resourceFilesCounter = 0;
        for (File file : in.listFiles()) {
            try {
                // Read the language files
                String fileName = file.getName().substring(0,2);
                // Convert the file stream to a json string to reveal its internal structure
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"));
                GramTree myGramtree = gson.fromJson(reader, GramTree.class);
                langDetector.register(fileName, myGramtree);
                resourceFilesCounter++;
            } catch (FileNotFoundException ex) {
                LOGGER.error("Exception in registering languages in detector {}", ex);
            }
        }
        
        assertTrue("There is a problem with reading the language resource files. Path "+fileNamePath, (resourceFilesCounter == numberOfResourceFiles));
    }
    
    @AfterClass
    public static void tearDownClass() {
    }    
    
    public LangDetectorTest() {
    }

    @Test
    public void testDetectLang() {
        
        String[][] texts = new String[][] {
            new String[] {"un texte en français","fr"},
            new String[] {"a text in english","en"},
            new String[] {"un texto en español","es"},
            new String[] {"un texte un peu plus long en français","fr"},
            new String[] {"a text a little longer in english","en"},
            new String[] {"a little longer text in english","en"},
            new String[] {"un texto un poco mas longo en español","es"},
            new String[] {"J'aime les bisounours !","fr"},
            new String[] {"Bienvenue à Montmartre !", "fr"},
            new String[] {"Welcome to London !", "en"},
            new String[] {"un piccolo testo in italiano", "it"},
            new String[] {"μια μικρή ελληνική γλώσσα", "el"},
            new String[] {"een kleine Nederlandse tekst", "nl"},
            new String[] {"Matching sur des lexiques", "fr"},
            new String[] {"Matching on lexicons", "en"},
            new String[] {"Une première optimisation consiste à ne tester que les sous-chaînes de taille compatibles avec le lexique.", "fr"},
            new String[] {"A otimização é a primeira prova de que não sub-canais compatível com o tamanho do léxico.", "pt"},
            new String[] {"Ensimmäinen optimointi ei pidä testata, että osa-kanavien kanssa koko sanakirja.", "fi"},
        };
        
        for (String[] text : texts) {
            assertEquals(text[1], langDetector.detectLang(text[0]));
        }
    }
    
}
