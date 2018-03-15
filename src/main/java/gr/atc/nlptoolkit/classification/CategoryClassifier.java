
package gr.atc.nlptoolkit.classification;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.sentiment.MessagePolarityClassification;
import gr.atc.nlptoolkit.utils.Tools;
import java.io.IOException;
import java.util.List;
import libsvm.svm;
import libsvm.svm_model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SRapanakis
 */
public class CategoryClassifier {
    
    private static svm_model eventsModel;
    private static List<String> eventsModelFeaturesList;
    private static final Tagger ARK_TAGGER = new Tagger();
    private static String modelDataPath;
    private static MessagePolarityClassification msgPolClassiffication;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryClassifier.class);

    /**
     * 
     * @param modelDataPath Path that contains the model files
     */
    public CategoryClassifier(String modelDataPath) {
        this.modelDataPath = modelDataPath;
        setModels();
    }
    
    private void setModels() {
        String eventsModelFilePath = modelDataPath+"/event-model-ncategories";
        String arkTaggerModelFilePath = modelDataPath+"/arktweetnlp/model.20120919";
        String eventsModelFeaturesFilePath = modelDataPath+"/event-model-ncategories-features";
        try {
            ARK_TAGGER.loadModel(arkTaggerModelFilePath);
            eventsModel = svm.svm_load_model(eventsModelFilePath);
        } catch (IOException ex) {
            LOGGER.error("Unable to load the model {}", ex);
        }
        eventsModelFeaturesList = Tools.readFile(eventsModelFeaturesFilePath);
        msgPolClassiffication = new MessagePolarityClassification(modelDataPath);
    }
    
    /**
     * Get the category that the tweet belongs to
     * 
     * @param tweet
     * @return 
     */
    public Events findEvents(String tweet) {
        if (tweet != null) {
            CategoryTweetInstance testInstance = new CategoryTweetInstance("", tweet, ARK_TAGGER);
            
            // Use the trained model to predict the class of the test instance
            double predictedClass = svm.svm_predict(
                    eventsModel, msgPolClassiffication.createEventsTestInstance(testInstance, eventsModelFeaturesList));
            
            Events evaluatedCategory =  null;
            if (predictedClass == 0)  {
                evaluatedCategory = Events.JUNK;
            } else if (predictedClass == 1)  {
                evaluatedCategory = Events.TECHNOLOGY;
            } else if (predictedClass == 2)  {
                evaluatedCategory = Events.BUSINESS;
            } else if (predictedClass == 3)  {
                evaluatedCategory = Events.CULTURE;
            } else if (predictedClass == 4)  {
                evaluatedCategory = Events.SCIENCE;
            } else if (predictedClass == 5)  {
                evaluatedCategory = Events.POLITICS;
            } else if (predictedClass == 6)  {
                evaluatedCategory = Events.SPORTS;
            }
            
            return evaluatedCategory;            
        }
        
        return null;
    }
    
}
