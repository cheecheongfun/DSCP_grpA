package sg.edu.np.mad.greencycle.Forum;

import com.google.firebase.Timestamp;
import java.util.List;

public class Reply {
    private String id;
    private String author;
    private String display;
    private String content;
    private Timestamp timestamp;
    private List<String> likedBy;

    private String commentId; // Optional, if you include like functionality for replies

    // Constructors
    public Reply() {}

    // Getters and setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.id = commentId; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
}
