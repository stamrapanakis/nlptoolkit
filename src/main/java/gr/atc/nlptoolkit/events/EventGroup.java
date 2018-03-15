
package gr.atc.nlptoolkit.events;

import gr.atc.nlptoolkit.utils.Tools;
import java.util.List;

/**
 * Represents a group of events
 * 
 * @author SRapanakis
 */
public class EventGroup {
    
    // A unique group identifier
    private Integer id;
    
    // A short text label of the group
    private String description;
    
    // The tweets that consist the group
    private List<String> tweets;

    public EventGroup(Integer id, String description, List<String> tweets) {
        this.id = id;
        this.description = description;
        this.tweets = tweets;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getTweets() {
        return tweets;
    }

    public void setTweets(List<String> tweets) {
        this.tweets = tweets;
    }

    @Override
    public String toString() {
        return "EventGroup{" + "id=" + id + ", description=" + description +
                ", tweets=" + Tools.prettyPrintList(tweets) + '}';
    }
}