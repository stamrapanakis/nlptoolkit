/*
 * POStagger 2011
 * Athens University of Economics and Business
 * Department of Informatics
 * Koleli Evangelia
 */
package gr.atc.nlptoolkit.greekpos;

import java.io.*;

//class that stores an instance with all its properties
public class SmallSetInstance implements Serializable {
    
    private SmallSetWordWithCategories current;
    private SmallSetWordWithCategories currentEnding1;
    private SmallSetWordWithCategories currentEnding2;
    private SmallSetWordWithCategories currentEnding3;
    private double length;
    private double has_apostrophe;
    private double has_digit;
    private double has_dot;
    private double has_comma;
    private double has_latin_character;
    private SmallSetWordWithCategories next;
    private SmallSetWordWithCategories nextEnding1;
    private SmallSetWordWithCategories nextEnding2;
    private SmallSetWordWithCategories nextEnding3;
    private String category;    

    // Constructor
    public SmallSetInstance(String w) {
        current = new SmallSetWordWithCategories(w);
        currentEnding1 = new SmallSetWordWithCategories(w);
        currentEnding2 = new SmallSetWordWithCategories(w);
        currentEnding3 = new SmallSetWordWithCategories(w);
        length = 0;
        has_apostrophe = 0.0;
        has_digit = 0.0;
        has_dot = 0.0;
        has_comma = 0.0;
        has_latin_character = 0.0;
        next = new SmallSetWordWithCategories(w);
        nextEnding1 = new SmallSetWordWithCategories(w);
        nextEnding2 = new SmallSetWordWithCategories(w);
        nextEnding3 = new SmallSetWordWithCategories(w);
        category = "";
    }

    // Copy constructor
    public SmallSetInstance(SmallSetInstance in) {
        current = new SmallSetWordWithCategories(in.current);
        currentEnding1 = new SmallSetWordWithCategories(in.currentEnding1);
        currentEnding2 = new SmallSetWordWithCategories(in.currentEnding2);
        currentEnding3 = new SmallSetWordWithCategories(in.currentEnding3);
        length = in.length;
        has_apostrophe = in.has_apostrophe;
        has_digit = in.has_digit;
        has_dot = in.has_dot;
        has_comma = in.has_comma;
        has_latin_character = in.has_latin_character;
        next = new SmallSetWordWithCategories(in.next);
        nextEnding1 = new SmallSetWordWithCategories(in.nextEnding1);
        nextEnding2 = new SmallSetWordWithCategories(in.nextEnding2);
        nextEnding3 = new SmallSetWordWithCategories(in.nextEnding3);
        category = in.category;
    }

    //get and set functions
    protected double getWordsLength() {
        return length;
    }

    protected void setBooleanProperties(int index, double b) {
        switch (index) {
            case 0:
                has_apostrophe = b;
            case 1:
                has_digit = b;
            case 2:
                has_dot = b;
            case 3:
                has_comma = b;
            case 4:
                has_latin_character = b;
        }
    }

    protected void setAmbitagProperties(int index, SmallSetWordWithCategories w) {
        switch (index) {
            case 0:
                current = new SmallSetWordWithCategories(w);
            case 1:
                currentEnding1 = new SmallSetWordWithCategories(w);
            case 2:
                currentEnding2 = new SmallSetWordWithCategories(w);
            case 3:
                currentEnding3 = new SmallSetWordWithCategories(w);
            case 4:
                next = new SmallSetWordWithCategories(w);
            case 5:
                nextEnding1 = new SmallSetWordWithCategories(w);
            case 6:
                nextEnding2 = new SmallSetWordWithCategories(w);
            case 7:
                nextEnding3 = new SmallSetWordWithCategories(w);
        }
    }

    protected void setCategory(String c) {
        category = c;
    }

    protected void setWordsLength(Double l) {
        length = l;
    }

    protected double getBooleanProperties(int index) {
        switch (index) {
            case 0:
                return has_apostrophe;
            case 1:
                return has_digit;
            case 2:
                return has_dot;
            case 3:
                return has_comma;
            case 4:
                return has_latin_character;
        }
        return 0.34;
    }

    protected SmallSetWordWithCategories getAmbitagProperties(int index) {
        switch (index) {
            case 0:
                return current;
            case 1:
                return currentEnding1;
            case 2:
                return currentEnding2;
            case 3:
                return currentEnding3;
            case 4:
                return next;
            case 5:
                return nextEnding1;
            case 6:
                return nextEnding2;
            case 7:
                return nextEnding3;
        }
        return null;
    }

    protected String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        String instance_string = category + " " + length + " ";
        
        instance_string += current;
        instance_string += currentEnding1;
        instance_string += currentEnding2;
        instance_string += currentEnding3;
        
        instance_string += has_apostrophe + " ";
        instance_string += has_digit + " ";
        instance_string += has_dot + " ";
        instance_string += has_comma + " ";
        instance_string += has_latin_character + " ";
        
        instance_string += next;
        instance_string += nextEnding1;
        instance_string += nextEnding2;
        instance_string += nextEnding3;
        
        return instance_string;
    }

    public SmallSetWordWithCategories getCurrent() {
        return current;
    }

    public void setCurrent(SmallSetWordWithCategories current) {
        this.current = current;
    }

    public SmallSetWordWithCategories getCurrentEnding1() {
        return currentEnding1;
    }

    public void setCurrentEnding1(SmallSetWordWithCategories currentEnding1) {
        this.currentEnding1 = currentEnding1;
    }

    public SmallSetWordWithCategories getCurrentEnding2() {
        return currentEnding2;
    }

    public void setCurrentEnding2(SmallSetWordWithCategories currentEnding2) {
        this.currentEnding2 = currentEnding2;
    }

    public SmallSetWordWithCategories getCurrentEnding3() {
        return currentEnding3;
    }

    public void setCurrentEnding3(SmallSetWordWithCategories currentEnding3) {
        this.currentEnding3 = currentEnding3;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public double getHas_apostrophe() {
        return has_apostrophe;
    }

    public void setHas_apostrophe(double has_apostrophe) {
        this.has_apostrophe = has_apostrophe;
    }

    public double getHas_digit() {
        return has_digit;
    }

    public void setHas_digit(double has_digit) {
        this.has_digit = has_digit;
    }

    public double getHas_dot() {
        return has_dot;
    }

    public void setHas_dot(double has_dot) {
        this.has_dot = has_dot;
    }

    public double getHas_comma() {
        return has_comma;
    }

    public void setHas_comma(double has_comma) {
        this.has_comma = has_comma;
    }

    public double getHas_latin_character() {
        return has_latin_character;
    }

    public void setHas_latin_character(double has_latin_character) {
        this.has_latin_character = has_latin_character;
    }

    public SmallSetWordWithCategories getNext() {
        return next;
    }

    public void setNext(SmallSetWordWithCategories next) {
        this.next = next;
    }

    public SmallSetWordWithCategories getNextEnding1() {
        return nextEnding1;
    }

    public void setNextEnding1(SmallSetWordWithCategories nextEnding1) {
        this.nextEnding1 = nextEnding1;
    }

    public SmallSetWordWithCategories getNextEnding2() {
        return nextEnding2;
    }

    public void setNextEnding2(SmallSetWordWithCategories nextEnding2) {
        this.nextEnding2 = nextEnding2;
    }

    public SmallSetWordWithCategories getNextEnding3() {
        return nextEnding3;
    }

    public void setNextEnding3(SmallSetWordWithCategories nextEnding3) {
        this.nextEnding3 = nextEnding3;
    }
    
    
}
