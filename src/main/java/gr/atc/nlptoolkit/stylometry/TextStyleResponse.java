
package gr.atc.nlptoolkit.stylometry;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TextStyleResponse {

    @Expose
    @SerializedName(value = "emoticonsSymbols")
    private int emoticonsSymbols;

    @Expose
    @SerializedName(value = "decorationsSymbols")
    private int decorationsSymbols;

    @Expose
    @SerializedName(value = "spamWords")
    private int spamWords;        

    @Expose
    @SerializedName(value = "containsSwearingWords")
    private boolean swearingWords;
    
    @Expose
    @SerializedName(value = "containsSlangWords")
    private boolean slangWords;
    
    public int getEmoticonsSymbols() {
        return emoticonsSymbols;
    }

    public void setEmoticonsSymbols(int emoticonsSymbols) {
        this.emoticonsSymbols = emoticonsSymbols;
    }

    public int getDecorationsSymbols() {
        return decorationsSymbols;
    }

    public void setDecorationsSymbols(int decorationsSymbols) {
        this.decorationsSymbols = decorationsSymbols;
    }

    public int getSpamWords() {
        return spamWords;
    }

    public void setSpamWords(int spamWords) {
        this.spamWords = spamWords;
    }

    public boolean isSwearingWords() {
        return swearingWords;
    }

    public void setSwearingWords(boolean swearingWords) {
        this.swearingWords = swearingWords;
    }

    public boolean isSlangWords() {
        return slangWords;
    }

    public void setSlangWords(boolean slangWords) {
        this.slangWords = slangWords;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + this.emoticonsSymbols;
        hash = 97 * hash + this.decorationsSymbols;
        hash = 97 * hash + this.spamWords;
        hash = 97 * hash + (this.swearingWords ? 1 : 0);
        hash = 97 * hash + (this.slangWords ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TextStyleResponse other = (TextStyleResponse) obj;
        if (this.emoticonsSymbols != other.emoticonsSymbols) {
            return false;
        }
        if (this.decorationsSymbols != other.decorationsSymbols) {
            return false;
        }
        if (this.spamWords != other.spamWords) {
            return false;
        }
        if (this.swearingWords != other.swearingWords) {
            return false;
        }
        if (this.slangWords != other.slangWords) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TextStyleResponse{" + "emoticonsSymbols=" + emoticonsSymbols + ", decorationsSymbols=" + decorationsSymbols + ", spamWords=" + spamWords + ", swearingWords=" + swearingWords + ", slangWords=" + slangWords + '}';
    }
}