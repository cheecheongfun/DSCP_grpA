package sg.edu.np.mad.greencycle.Forum;

import com.google.firebase.Timestamp;
import java.util.List;

public class Comment {
    private String author,display,Id;
    private String content;
    private List<String> likedBy;
    private String postId;
    private List<String> repliedBy;
    private Timestamp timestamp;

    // Default constructor is needed for Firebase deserialization
    public Comment() {
    }

    // Constructor with all fields
    public Comment(String author, String content, List<String> likedBy, String postId, List<String> repliedBy, Timestamp timestamp, String display,String Id) {
        this.author = author;
        this.content = content;
        this.likedBy = likedBy;
        this.postId = postId;
        this.repliedBy = repliedBy;
        this.timestamp = timestamp;
        this.display = display;
        this.Id = Id;
    }

    // Getters and setters
    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }
    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public List<String> getRepliedBy() {
        return repliedBy;
    }

    public void setRepliedBy(List<String> repliedBy) {
        this.repliedBy = repliedBy;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

