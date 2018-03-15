
package gr.atc.nlptoolkit.language;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author SRapanakis
 */
                 
public class GramTree {
    
    private int min;
    private int max;
    private long gramcount;
    private final Node root;
    
    public GramTree(int min, int max) {
        root = new Node('\u0000');
        gramcount = 0;
        this.min = min;
        this.max = max;
    }
    
    public void learn(CharSequence text) {
        NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
        for (CharSequence token : tokenizer) {
            addGram(token);
        }
    }

    private void addGram(CharSequence gram) {
        Node cur = root;
        for (int i=0; i<gram.length();i++) {
            char c = gram.charAt(i);
            Node next = cur.getChild(c);
            if (next==null) {
                next = cur.addTransition(c);
            }
            cur = next;
            if (i==gram.length()-1) {
                cur.inc();
            }			
        }
        gramcount++;
    }

    public void compress() {
        root.compress();
    }

    public double scoreText(CharSequence text) {
        NGramTokenizer tokenizer = new NGramTokenizer(text, min, max);
        double tot = 0;
        for (CharSequence charSequence : tokenizer) {
            double s = scoreGram(charSequence);
            tot += s;
            }
        double score = tot / Math.log(gramcount);
        
        return score;
    }

    private double scoreGram(CharSequence gram) {
        Node cur = root;
        for (int i=0; i<gram.length();i++) {
            char c = gram.charAt(i);
            Node next = cur.getChild(c);
            if (next==null) {
                return 0;
            }
            cur = next;
        }
        return Math.log(cur.freq);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public long getGramcount() {
        return gramcount;
    }

    public void setGramcount(long gramcount) {
        this.gramcount = gramcount;
    }

    public Node getRoot() {
        return root;
    }

    static class Node implements Serializable, Comparable<Node> {
        private final static int DEFAULT_ALLOC = 64;
        
        private char c;
        private long freq;
        private Node[] children;
        private int childcount;
        
        private Node(char c) {
            this.c = c;
            this.freq = 0;
            this.childcount = 0;
        }
        
        public Node addTransition(char c) {
            Node child = new Node(c);
            if (children==null) {
                children = new Node[DEFAULT_ALLOC];
            }
            if (childcount==children.length-1) {
                // reallocate
                Node[] realloc = new Node[children.length+DEFAULT_ALLOC];
                System.arraycopy(children, 0, realloc, 0, children.length);
                children = realloc;
            }
            children[childcount] = child;
            childcount++;
            Arrays.sort(children, 0, childcount);
            return child;
        }
        
        private void inc() {
            freq++;
        }
        
        @Override
        public int compareTo(Node o) {
            return c-o.c;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + this.c;
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
            final Node other = (Node) obj;
            if (this.c != other.c) {
                return false;
            }
            return true;
        }

        public Node getChild(char c) {
            for (int i=0; i<childcount;i++) {
                if (children[i].c==c) {
                    return children[i];
                }
                if (children[i].c>c) {
                    return null;
                }
            }
            return null;
        }
        
        private void compress() {
            if (childcount==0) {
                return;
            }
            Node[] children2 = new Node[childcount];
            System.arraycopy(children,0,children2,0,childcount);
            children = children2;
            for (Node child : children) {
                child.compress();
            }
        }        

        public char getC()  {
            return c;
        }

        public void setC(char c) {
            this.c = c;
        }

        public long getFreq() {
            return freq;
        }

        public void setFreq(long freq) {
            this.freq = freq;
        }

        public Node[] getChildren() {
            return children;
        }

        public void setChildren(Node[] children) {
            this.children = children;
        }

        public int getChildcount() {
            return childcount;
        }

        public void setChildcount(int childcount) {
            this.childcount = childcount;
        }

        @Override
        public String toString() {
            return "Node{" + "c=" + c + ", freq=" + freq + ", childcount=" + childcount + '}';
        }
        
    }
}
