/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.atc.nlptoolkit.greekpos;

import edu.stanford.nlp.CLclassify.*;
import edu.stanford.nlp.CLling.RVFDatum;
import edu.stanford.nlp.CLstats.ClassicCounter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MoreFunctions {

    protected static HashMap<String, String> list;
    protected static HashMap<String, SmallSetWordWithCategories> words;
    protected static HashMap<String, SmallSetWordWithCategories> endings1;
    protected static HashMap<String, SmallSetWordWithCategories> endings2;
    protected static HashMap<String, SmallSetWordWithCategories> endings3;
    protected static int corpus_used;

    //returns the 3 possible endings of a word
    protected static String getEnding(int numOfLetters, String word) {
        char[] array = word.toCharArray();

        if (numOfLetters == 1) {
            return Character.toString(array[array.length - 1]);
        }
        if (numOfLetters == 2) {
            return Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
        }
        if (numOfLetters == 3) {
            return Character.toString(array[array.length - 3]) + Character.toString(array[array.length - 2]) + Character.toString(array[array.length - 1]);
        }
        return null;
    }

    //returns the length of a word
    protected static int findLength(String word) {
        char[] array = word.toCharArray();
        if (array.length == 1) {
            return 1;
        }
        if (array.length == 2) {
            return 2;
        }
        if (array.length > 2) {
            return 3;
        }
        return 0;
    }

    //opens a buffer in order to read the file with properties
    protected static void readFileWithProperties(String fileName, RVFDataset ds, boolean test) throws FileNotFoundException {
        FileInputStream fstream = new FileInputStream(fileName);
        DataInputStream in = new DataInputStream(fstream);
        if (test) {
            useRealValuedClassifierTest(new BufferedReader(new InputStreamReader(in)), ds);
        } else if (!test) {
            //useRealValuedClassifier(new BufferedReader(new InputStreamReader(in)), ds);//we use it only for cross validation
        }
    }
    
    //creates a data structure of train instances
    protected static void useRealValuedClassifierTest(BufferedReader br, RVFDataset ds) {
        String dataLine;
        String label;
        RVFDatum d;
        try {
            while ((dataLine = br.readLine()) != null) {
                ClassicCounter<String> cc = new ClassicCounter<String>();
                String[] data = dataLine.split(" ");
                label = data[0];
                for (int i = 1; i < data.length; i++) {
                    cc.incrementCount("feature" + i, Double.parseDouble(data[i]));
                }
                d = new RVFDatum(cc, label);
                ds.add(d);
            }
        } catch (IOException ex) {
            Logger.getLogger(MoreFunctions.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    

    //creates a data structure of train instances
    protected static void setDataset(RVFDataset ds, Vector<SmallSetInstance> vector) {
        String label;
        RVFDatum d;        
        if (vector != null){
            for (SmallSetInstance smallSetInstance:vector) {
                label = smallSetInstance.getCategory();
                ClassicCounter<String> cc = new ClassicCounter<String>();
                
                int i = 1;
                cc.incrementCount("feature" + i++, smallSetInstance.getLength());
                
                for (String key:smallSetInstance.getCurrent().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getCurrent().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getCurrentEnding1().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getCurrentEnding1().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getCurrentEnding2().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getCurrentEnding2().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getCurrentEnding3().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getCurrentEnding3().getListOfProperties().get(key));
                }
                
                cc.incrementCount("feature" + i++, smallSetInstance.getHas_apostrophe());
                cc.incrementCount("feature" + i++, smallSetInstance.getHas_digit());
                cc.incrementCount("feature" + i++, smallSetInstance.getHas_dot());
                cc.incrementCount("feature" + i++, smallSetInstance.getHas_comma());
                cc.incrementCount("feature" + i++, smallSetInstance.getHas_latin_character());
                
                for (String key:smallSetInstance.getNext().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getNext().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getNextEnding1().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getNextEnding1().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getNextEnding2().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getNextEnding2().getListOfProperties().get(key));
                }
                
                for (String key:smallSetInstance.getNextEnding3().getListOfProperties().keySet()) {
                    cc.incrementCount("feature" + i++, smallSetInstance.getNextEnding3().getListOfProperties().get(key));
                }
                
                d = new RVFDatum(cc, label);
                ds.add(d);
            }
        }
    }
    
    //returns the square of a number
    protected static double square(double a) {
        return a * a;
    }

    //creates a vector which contains the words to be classified
    protected static Vector<String> createVector(String wholeText) {
        Vector<String> justWords = new Vector<String>();
        StringTokenizer st = new StringTokenizer(wholeText, " ");
        justWords.add("null");
        while (st.hasMoreTokens()) {
            justWords.add(st.nextToken());
        }
        justWords.add("null");
        return justWords;
    }
}
