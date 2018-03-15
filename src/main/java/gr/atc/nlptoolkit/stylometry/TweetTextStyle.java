
package gr.atc.nlptoolkit.stylometry;

import cmu.arktweetnlp.Tagger;
import gr.atc.nlptoolkit.utils.Tools;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 *
 * Determines the tweet text style
 * 
 * @author SRapanakis
 */
public class TweetTextStyle {

    private final String modelDataPath;
    private static final Tagger ARK_TAGGER = new Tagger();
    
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TweetTextStyle.class);

    public TweetTextStyle(String modelDataPath) {
        this.modelDataPath = modelDataPath;
        setModels();
    }
    
    private void setModels() {
        String arkTaggerModelFilePath = modelDataPath+"/arktweetnlp/model.20120919";
        try {
            ARK_TAGGER.loadModel(arkTaggerModelFilePath);
        } catch (IOException ex) {
            LOGGER.error("Unable to load the model {}", ex);
        }        
    }
    
    /**
     * 
     * @param tweetText
     * @return 
     */
    public TextStyleResponse getTextStyleEL(String tweetText) {
        boolean containsSwearingWord = false;
        boolean containsSlangWords = false;
        int decorationSymbols = 0;
        int emoticonsSymbols = 0;
        int spamWords = 0;
        
        TextStyleResponse response = new TextStyleResponse();
        if (StringUtils.isBlank(tweetText)) {
            return response;
        }
        
        List<Tagger.TaggedToken> taggedTokens = ARK_TAGGER.tokenizeAndTag(tweetText);
        List<String> swearWords = Tools.getGreekSwearWords();
        List<String> slangWords = getTwitterSlangWords();
        List<String> spamPhrases = getCommonSpamPhrases();
        
        StringBuilder textWithoutHttp = new StringBuilder();
        
        int counter = 0;
        for (Tagger.TaggedToken tweetToken:taggedTokens) {
            String word = tweetToken.token.toLowerCase();
            
            // Remove the urls
            if (!"U".equals(tweetToken.tag)) {
                counter++;
                textWithoutHttp.append(tweetToken.token);
                // Do not add a line separator in the last item
                if (counter != taggedTokens.size()) {
                    textWithoutHttp.append(" ");
                }
            }
            
            // Check for swearing words        
            for (String swearWord:swearWords) {
                if (word.startsWith(swearWord)) {
                    containsSwearingWord = true;
                    break;
                }
            }
            
            // Check for slang words        
            for (String slangWord:slangWords) {
                if (word.equalsIgnoreCase(slangWord)) {
                    containsSlangWords = true;
                    break;
                }
            }            
            
            for (String spamPhrase:spamPhrases) {
                String spamPhraseLowerCase = spamPhrase.toLowerCase();
                if (word.contains(spamPhraseLowerCase)) {
                    spamWords++;
                }
            }
            
        }
        
        decorationSymbols = EmoticonsDetector.getNumberOfDecorators(textWithoutHttp.toString());
        emoticonsSymbols = EmoticonsDetector.getNumberOfEmoticons(textWithoutHttp.toString());
        
        response.setSwearingWords(containsSwearingWord);
        response.setSlangWords(containsSlangWords);
        response.setDecorationsSymbols(decorationSymbols);        
        response.setEmoticonsSymbols(emoticonsSymbols);
        response.setSpamWords(spamWords);
        
        return response;        
    }    
    
    /**
     * 
     * @param tweetText
     * @return 
     */
    public TextStyleResponse getTextStyleEN(String tweetText) {
        
        boolean containsSwearingWord = false;
        boolean containsSlangWords = false;
        int decorationSymbols = 0;
        int emoticonsSymbols = 0;
        int spamWords = 0;
        
        TextStyleResponse response = new TextStyleResponse();
        if (StringUtils.isBlank(tweetText)) {
            return response;
        }
        
        List<Tagger.TaggedToken> taggedTokens = ARK_TAGGER.tokenizeAndTag(tweetText);
        List<String> swearWords = Tools.getTwitterSwearWords();
        List<String> slangWords = getTwitterSlangWords();
        List<String> spamPhrases = getCommonSpamPhrases();
        
        StringBuilder textWithoutHttp = new StringBuilder();
        
        int counter = 0;
        for (Tagger.TaggedToken tweetToken:taggedTokens) {
            String word = tweetToken.token.toLowerCase();
            
            // Remove the urls
            if (!"U".equals(tweetToken.tag)) {
                counter++;
                textWithoutHttp.append(tweetToken.token);
                // Do not add a line separator in the last item
                if (counter != taggedTokens.size()) {
                    textWithoutHttp.append(" ");
                }
            }
            
            // Check for swearing words        
            for (String swearWord:swearWords) {
                if (word.equalsIgnoreCase(swearWord)) {
                    containsSwearingWord = true;
                    break;
                }
            }
            
            // Check for slang words        
            for (String slangWord:slangWords) {
                if (word.equalsIgnoreCase(slangWord)) {
                    containsSlangWords = true;
                    break;
                }
            }            
            
            for (String spamPhrase:spamPhrases) {
                String spamPhraseLowerCase = spamPhrase.toLowerCase();
                if (word.contains(spamPhraseLowerCase)) {
                    spamWords++;
                }
            }
            
        }
        
        decorationSymbols = EmoticonsDetector.getNumberOfDecorators(textWithoutHttp.toString());
        emoticonsSymbols = EmoticonsDetector.getNumberOfEmoticons(textWithoutHttp.toString());
        
        response.setSwearingWords(containsSwearingWord);
        response.setSlangWords(containsSlangWords);
        response.setDecorationsSymbols(decorationSymbols);        
        response.setEmoticonsSymbols(emoticonsSymbols);
        response.setSpamWords(spamWords);
        
        return response;
    }
    
    /**
     * 
     * @param textStyle
     * @return 
     */
    public String characterizeTextStyle(TextStyleResponse textStyle) {
        
        // Do not display the Common string case
        String textStyleDescription = "Common";        
        
        if (textStyle != null) {
            boolean containsSwearingWords = textStyle.isSwearingWords();
            boolean containsSlangWords = textStyle.isSlangWords();
            int emoticons = textStyle.getEmoticonsSymbols();
            int spamWords = textStyle.getSpamWords();

            if (containsSwearingWords || containsSlangWords) {
                textStyleDescription = "Slangy";
            } else if ((emoticons > 1) || (spamWords > 1)) {
                textStyleDescription = "Informal";
            }            
        }
        
        return textStyleDescription;        
    }
    
    /**
     * List of common Twitter slang words/ abbreviations
     * 
     * @return 
     */
    private List<String> getTwitterSlangWords() {
        
        List<String> slangWords = new ArrayList<String>();
        
        //http://www.bloggertipsseotricks.com/2011/07/most-slang-words-facebook-chatting.html
        
        slangWords.add("asl");
        slangWords.add("b4");
        slangWords.add("bbl");
        slangWords.add("bbs");
        slangWords.add("brb");
        slangWords.add("btw");
        slangWords.add("dude");
        slangWords.add("fyi");
        slangWords.add("iwsn");
        slangWords.add("idk");
        slangWords.add("j4f");
        slangWords.add("j4u");
        slangWords.add("jlt");
        slangWords.add("lmao");
        slangWords.add("lmirl");
        slangWords.add("lol");
        slangWords.add("mirl");
        slangWords.add("noob");
        slangWords.add("omg");
        slangWords.add("oops");
        slangWords.add("plz");
        slangWords.add("pron");
        slangWords.add("rofl");
        slangWords.add("srsly");
        slangWords.add("tdtm");
        slangWords.add("ttyl");
        slangWords.add("tu");
        slangWords.add("wtv");
        slangWords.add("wtf");
        slangWords.add("zomg");
        
        return slangWords;
    }
    
    
    /**
     * Common phrases used in spam messages
     * 
     * @return 
     */
    private List<String> getCommonSpamPhrases() {
        
        List<String> spamPhrases = new ArrayList<String>();
        spamPhrases.add("Remove in quotes");
        spamPhrases.add("For just");
        spamPhrases.add("Free priority mail");
        spamPhrases.add("Take action now");
        spamPhrases.add("Clearance");
        spamPhrases.add("Offer");
        spamPhrases.add("Undisclosed Recipient");
        spamPhrases.add("They’re just giving it away");
        spamPhrases.add("No gimmicks");
        spamPhrases.add("Join Millions");
        spamPhrases.add("Section 301");
        spamPhrases.add("Sent in compliance");
        spamPhrases.add("Unsecured debt");
        spamPhrases.add("Cannot be combined with any other offer");
        spamPhrases.add("Marketing");
        spamPhrases.add("Guarantee");
        spamPhrases.add("Have you been turned down?");
        spamPhrases.add("Click");
        spamPhrases.add("Strong buy");
        spamPhrases.add("Reverses Aging");
        spamPhrases.add("Buy");
        spamPhrases.add("Why pay more?");
        spamPhrases.add("Potential earnings");
        spamPhrases.add("#1");
        spamPhrases.add("Free offer");
        spamPhrases.add("No obligation");
        spamPhrases.add("One hundred percent guaranteed");
        spamPhrases.add("Don’t Hesitate");
        spamPhrases.add("Unsolicited");
        spamPhrases.add("Legal");
        spamPhrases.add("While you sleep");
        spamPhrases.add("New domain extensions");
        spamPhrases.add("Additional Income");
        spamPhrases.add("Offers coupon");
        spamPhrases.add("Only $");
        spamPhrases.add("Different reply to");
        spamPhrases.add("Free DVD");
        spamPhrases.add("Claims");
        spamPhrases.add("Win");
        spamPhrases.add("Direct marketing");
        spamPhrases.add("Buy direct");
        spamPhrases.add("Tells you it’s an ad");
        spamPhrases.add("Credit Cards");
        spamPhrases.add("Accept credit cards");
        spamPhrases.add("Sales");
        spamPhrases.add("Buying judgments");
        spamPhrases.add("Pre-approved");
        spamPhrases.add("The best rates");
        spamPhrases.add("Offer expires");
        spamPhrases.add("Message contains disclaimer");
        spamPhrases.add("Work From Home");
        spamPhrases.add("The following form");
        spamPhrases.add("Free membership");
        spamPhrases.add("Compare rates");
        spamPhrases.add("Lowest Price");
        spamPhrases.add("Special promotion");
        spamPhrases.add("Hidden assets");
        spamPhrases.add("Satisfaction guaranteed");
        spamPhrases.add("Message contains");
        spamPhrases.add("Social security number");
        spamPhrases.add("Earn per week");
        spamPhrases.add("Investment");
        spamPhrases.add("Winner");
        spamPhrases.add("Email marketing");
        spamPhrases.add("MLM");
        spamPhrases.add("Please read");
        spamPhrases.add("Human growth hormone");
        spamPhrases.add("Money");
        spamPhrases.add("Don’t hesitate");
        spamPhrases.add("Being a member");
        spamPhrases.add("Order Now");
        spamPhrases.add("We hate spam");
        spamPhrases.add("Claims to be legal");
        spamPhrases.add("Expect to earn");
        spamPhrases.add("Removal instructions");
        spamPhrases.add("No experience");
        spamPhrases.add("Drastically reduced");
        spamPhrases.add("Refund");
        spamPhrases.add("Calling creditors");
        spamPhrases.add("Produced and sent out");
        spamPhrases.add("Dear friend");
        spamPhrases.add("Round the world");
        spamPhrases.add("New customers only");
        spamPhrases.add("Print out and fax");
        spamPhrases.add("Free money");
        spamPhrases.add("Extra income");
        spamPhrases.add("Email harvest");
        spamPhrases.add("Lose");
        spamPhrases.add("Off shore");
        spamPhrases.add("Call now");
        spamPhrases.add("Discount");
        spamPhrases.add("Stuff on sale");
        spamPhrases.add("Copy accurately");
        spamPhrases.add("We honor all");
        spamPhrases.add("Consolidate debt and credit");
        spamPhrases.add("Call");
        spamPhrases.add("No age restrictions");
        spamPhrases.add("Claims to be in accordance with some spam law");
        spamPhrases.add("Dig up dirt on friends");
        spamPhrases.add("In accordance with laws");
        spamPhrases.add("Increase traffic");
        spamPhrases.add("Call free");
        spamPhrases.add("Once in lifetime");
        spamPhrases.add("Get started now");
        spamPhrases.add("Cents on the dollar");
        spamPhrases.add("Satisfaction");
        spamPhrases.add("Member stuff");
        spamPhrases.add("Lower monthly payment");
        spamPhrases.add("Credit bureaus");
        spamPhrases.add("Cures baldness");
        spamPhrases.add("Mail in order form");
        spamPhrases.add("US dollars");
        spamPhrases.add("Phone");
        spamPhrases.add("Dear somebody");
        spamPhrases.add("Work at home");
        spamPhrases.add("Cell phone cancer scam");
        spamPhrases.add("Find out anything");
        spamPhrases.add("4U");
        spamPhrases.add("Discusses search engine listings");
        spamPhrases.add("Information you requested");
        spamPhrases.add("You Have Been Selected");
        spamPhrases.add("Claims not to be selling anything");
        spamPhrases.add("Free installation");
        spamPhrases.add("Income");
        spamPhrases.add("Compete for your business");
        spamPhrases.add("Confidentially on all orders");
        spamPhrases.add("Vacation offers");
        spamPhrases.add("You’ve Won");
        spamPhrases.add("Copy DVDs");
        spamPhrases.add("No gimmick");
        spamPhrases.add("Money back");
        spamPhrases.add("Extra Cash");
        spamPhrases.add("Opportunity");
        spamPhrases.add("Apply online");
        spamPhrases.add("Here");
        spamPhrases.add("All natural");
        spamPhrases.add("Performance");
        spamPhrases.add("Amazing");
        spamPhrases.add("Investment decision");
        spamPhrases.add("People just leave money laying around");
        spamPhrases.add("Get It Now");
        spamPhrases.add("One hundred percent free");
        spamPhrases.add("Click Below");
        spamPhrases.add("Auto email removal");
        spamPhrases.add("Pennies a day");
        spamPhrases.add("All new");
        spamPhrases.add("Meet singles");
        spamPhrases.add("Beneficiary");
        spamPhrases.add("Dear Friend");
        spamPhrases.add("Brand new pager");
        spamPhrases.add("Be your own boss");
        spamPhrases.add("Click here link");
        spamPhrases.add("Earn Money");
        spamPhrases.add("Avoid bankruptcy");
        spamPhrases.add("Insurance");
        spamPhrases.add("Click to remove");
        spamPhrases.add("Requires initial investment");
        spamPhrases.add("Multi level marketing");
        spamPhrases.add("Sign up free today");
        spamPhrases.add("Not intended");
        spamPhrases.add("Giving away");
        spamPhrases.add("Online biz opportunity");
        spamPhrases.add("Talks about hidden charges");
        spamPhrases.add("This isn’t junk");
        spamPhrases.add("Pure profit");
        spamPhrases.add("Who really wins?");
        spamPhrases.add("Unsubscribe");
        spamPhrases.add("Direct email");
        spamPhrases.add("Lose Weight");
        spamPhrases.add("Click below");
        spamPhrases.add("While supplies last");
        spamPhrases.add("One time mailing");
        spamPhrases.add("Lower Payment");
        spamPhrases.add("No cost");
        spamPhrases.add("Save up to");
        spamPhrases.add("Print form signature");
        spamPhrases.add("Bulk");
        spamPhrases.add("Increase sales");
        spamPhrases.add("Free");
        spamPhrases.add("Free preview");
        spamPhrases.add("Marketing solutions");
        spamPhrases.add("Do it today");
        spamPhrases.add("Free access");
        spamPhrases.add("You have been selected");
        spamPhrases.add("Easy terms");
        spamPhrases.add("Refinance home");
        spamPhrases.add("Mortgage rates");
        spamPhrases.add("No questions asked");
        spamPhrases.add("Profits");
        spamPhrases.add("Real thing");
        spamPhrases.add("No strings attached");
        spamPhrases.add("Wants credit card");
        spamPhrases.add("Long distance phone offer");
        spamPhrases.add("Be amazed");
        spamPhrases.add("No fees");
        spamPhrases.add("Claims you registered with Some Kind of Partner");
        spamPhrases.add("Best price");
        spamPhrases.add("Hidden");
        spamPhrases.add("Cards Accepted");
        spamPhrases.add("Best Price");
        spamPhrases.add("No investment");
        spamPhrases.add("Offers extra cash");
        spamPhrases.add("Name brand");
        spamPhrases.add("Billing address");
        spamPhrases.add("Opt in");
        spamPhrases.add("Free hosting");
        spamPhrases.add("Lose weight spam");
        spamPhrases.add("Addresses on CD");
        spamPhrases.add("Serious cash");
        spamPhrases.add("Price");
        spamPhrases.add("More Internet traffic");
        spamPhrases.add("Stock disclaimer statement");
        spamPhrases.add("Terms and conditions");
        spamPhrases.add("Free quote");
        spamPhrases.add("Home employment");
        spamPhrases.add("Join millions of Americans");
        spamPhrases.add("Winning");
        spamPhrases.add("Shopping spree");
        spamPhrases.add("Free leads");
        spamPhrases.add("Free trial");
        spamPhrases.add("Congratulations");
        spamPhrases.add("What are you waiting for?");
        spamPhrases.add("Urgent");
        spamPhrases.add("Reserves the right");
        spamPhrases.add("No catch");
        spamPhrases.add("See for yourself");
        spamPhrases.add("Fast Viagra delivery");
        spamPhrases.add("Eliminate Debt");
        spamPhrases.add("Removes wrinkles");
        spamPhrases.add("Weekend getaway");
        spamPhrases.add("Solution");
        spamPhrases.add("Your income");
        spamPhrases.add("Reverses aging");
        spamPhrases.add("University diplomas");
        spamPhrases.add("Never");
        spamPhrases.add("Limited Time");
        spamPhrases.add("Risk free");
        spamPhrases.add("Lowest price");
        spamPhrases.add("One time");
        spamPhrases.add("Casino");
        spamPhrases.add("Save $");
        spamPhrases.add("Money making");
        spamPhrases.add("No inventory");
        spamPhrases.add("For free");
        spamPhrases.add("No claim forms");
        spamPhrases.add("No Hidden Costs");
        spamPhrases.add("Certified");
        spamPhrases.add("Eliminate bad credit");
        spamPhrases.add("Big bucks");
        spamPhrases.add("Offers free passwords");
        spamPhrases.add("Stock pick");
        spamPhrases.add("Get it now");
        spamPhrases.add("Billion dollars");
        spamPhrases.add("Search engines");
        spamPhrases.add("No-obligation");
        spamPhrases.add("Score with babes");
        spamPhrases.add("Gift certificate");
        spamPhrases.add("Orders shipped by priority mail");
        spamPhrases.add("Remove subject");
        spamPhrases.add("Great offer");
        spamPhrases.add("one-time");
        spamPhrases.add("Financial freedom");
        spamPhrases.add("No selling");
        spamPhrases.add("Free website");
        spamPhrases.add("As seen on");
        spamPhrases.add("Lower interest rates");
        spamPhrases.add("Stop snoring");
        spamPhrases.add("Will not believe your eyes");
        spamPhrases.add("Bargain");
        spamPhrases.add("Laser printer");
        spamPhrases.add("Reply remove subject");
        spamPhrases.add("No medical exams");
        spamPhrases.add("Bill 1618");
        spamPhrases.add("Free consultation");
        spamPhrases.add("Can’t live without");
        spamPhrases.add("Unsecured credit");
        spamPhrases.add("Supplies are limited");
        spamPhrases.add("Subject to credit");
        spamPhrases.add("Free sample");
        spamPhrases.add("Outstanding values");
        spamPhrases.add("Unlimited");
        spamPhrases.add("Safeguard notice");
        spamPhrases.add("Additional income");
        spamPhrases.add("Success");
        spamPhrases.add("Bonus");
        spamPhrases.add("Full refund");
        spamPhrases.add("Limited time only");
        spamPhrases.add("Cost");
        spamPhrases.add("Mass email");
        spamPhrases.add("Serious only");
        spamPhrases.add("Act Now!");
        spamPhrases.add("Supplies Are Limited");
        spamPhrases.add("Cash bonus");
        spamPhrases.add("Free");
        spamPhrases.add("No credit check");
        spamPhrases.add("Luxury car");
        spamPhrases.add("Credit card offers");
        spamPhrases.add("Order Status");
        spamPhrases.add("Online pharmacy");
        spamPhrases.add("Free grant money");
        spamPhrases.add("Free investment");
        spamPhrases.add("Click to remove mailto");
        spamPhrases.add("Prize");
        spamPhrases.add("Cable converter");
        spamPhrases.add("Save big money");
        spamPhrases.add("Stock alert");
        spamPhrases.add("It's effective");
        spamPhrases.add("Month trial offer");
        spamPhrases.add("Order");
        spamPhrases.add("No middleman");
        spamPhrases.add("All Natural");
        spamPhrases.add("Increase Traffic");
        spamPhrases.add("If only it were that easy");
        spamPhrases.add("Order now");
        spamPhrases.add("Claims you are a winner");
        spamPhrases.add("Form");
        spamPhrases.add("For instant access");
        spamPhrases.add("Click Here");
        spamPhrases.add("Talks about prizes");
        spamPhrases.add("Don’t delete");
        spamPhrases.add("Fantastic deal");
        spamPhrases.add("They keep your money — no refund!");
        spamPhrases.add("Act Now");
        spamPhrases.add("Remove");
        spamPhrases.add("No disappointment");
        spamPhrases.add("No purchase necessary");
        spamPhrases.add("Cancel at any time");
        spamPhrases.add("Bulk email");
        spamPhrases.add("Check");
        spamPhrases.add("Check or money order");
        spamPhrases.add("Stainless steel");
        spamPhrases.add("Apply Online");
        spamPhrases.add("Affordable");
        spamPhrases.add("This isn’t spam");
        spamPhrases.add("Dear email");
        spamPhrases.add("Promise you");
        spamPhrases.add("Cures");
        spamPhrases.add("Now");
        spamPhrases.add("Order today");
        spamPhrases.add("Viagra");
        spamPhrases.add("Get paid");
        spamPhrases.add("100% satisfied");
        spamPhrases.add("Cash");
        spamPhrases.add("Order status");
        spamPhrases.add("Wife");
        spamPhrases.add("Free");
        spamPhrases.add("shipping");
        spamPhrases.add("fingertips");
        
        return spamPhrases;
    }        
}
