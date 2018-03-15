
package gr.atc.nlptoolkit.language;

import java.util.Iterator;

/**
 * Iterates over a char sequence to produce n-grams. Requires both minimal and maximal gram length.
 */
public class NGramIterator implements Iterator<CharSequence> {
    private final CharSequence buffer;
    private final int max;
    
    private int pos;
    private int window;

    public NGramIterator(CharSequence buffer, int min, int max) {
        this.buffer = buffer;
        this.max = max;
        pos = -1;
        window = min;
    }

    public boolean hasNext() {
        boolean ok = pos+window<buffer.length();
        if (!ok) {
            ok = (window+1<=max) && (window+1<buffer.length());
        }
        return ok;
    }

    public CharSequence next() {
        pos++;
        if (pos+window>buffer.length()) {
            pos = 0;
            window++;
        }
        if ((window>max)||(pos+window>buffer.length())) {
            return null;
        }
        return buffer.subSequence(pos, pos+window);
    }
    
    public void remove() {
        throw new UnsupportedOperationException();
    }

}