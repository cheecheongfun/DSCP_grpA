package sg.edu.np.mad.greencycle.Forum;

import com.google.firebase.Timestamp;
import java.util.List;

public class Post {
    private String id;
    private String title;
    private String content;
    private List<String> imageUrls;  // Adjusted to match the field in Firestore
    private List<String> likedBy;  // Adjusted to match the field in Firestore
    private String savedBy;
    private Timestamp timestamp;
    private String user;  // Adjusted to match the field in Firestore

    // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    public Post() {}

    // Getters and setters for each field
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrl) {
        this.imageUrls = imageUrl;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public String getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(String savedBy) {
        this.savedBy = savedBy;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
