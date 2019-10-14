package uk.ac.bris.cs.databases.api;

import uk.ac.bris.cs.databases.util.Params;

/**
 * Summary of a single forum.
 * @author csxdb
 */
public class AdvancedForumSummaryView {
    
    /* The title of this forum. */
    private final String title;
    
    /* The id of this forum. */
    private final int id;
    
    /* The last topic in which a post was made in this forum
     * or NULL if this forum contains no entries.
     */
    private final TopicSummaryView lastTopic;

    public AdvancedForumSummaryView(int id,
                                    String title,
                                    TopicSummaryView lastTopic) {
        Params.cannotBeEmpty(title);
        
        this.id = id;
        this.title = title;
        this.lastTopic = lastTopic;
    }
    
    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }  

    /**
     * @return the lastTopic
     */
    public TopicSummaryView getLastTopic() {
        return lastTopic;
    }
    
}
