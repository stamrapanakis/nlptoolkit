
package gr.atc.nlptoolkit.ner;

public class NerToken {

    // The text to be examined for names entities
    private String text;
    // Start index of the identified ner
    private int startIndex;
    // End index of the identified ner
    private int endIndex;
    // LOCATION, PERSON, ORGANIZATION
    private NER_CATEGORY nerType;

    public NerToken() {
    }

    public NerToken(String text, int startIndex, int endIndex, NER_CATEGORY nerType) {
        this.text = text;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.nerType = nerType;
    }
    
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public NER_CATEGORY getNerType() {
        return nerType;
    }

    public void setNerType(NER_CATEGORY nerType) {
        this.nerType = nerType;
    }

    @Override
    public String toString() {
        return "NerToken{ nerType=" + nerType + ", text=" + text + ", startIndex=" + startIndex + ", endIndex=" + endIndex + '}';
    }
    
}
