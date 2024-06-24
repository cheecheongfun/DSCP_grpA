package sg.edu.np.mad.greencycle.Forum;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Post implements Parcelable {
    private String id;
    private String title;
    private String content;
    private List<String> imageUrls;
    private List<String> likedBy;
    private List<String> tags; // List to store tags
    private String savedBy;
    private Timestamp timestamp;
    private String user;

    // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    public Post() {}

    protected Post(Parcel in) {
        id = in.readString();
        title = in.readString();
        content = in.readString();
        imageUrls = in.createStringArrayList();
        likedBy = in.createStringArrayList();
        tags = in.createStringArrayList();  // Read the list of tags from the parcel
        savedBy = in.readString();
        long time = in.readLong();  // Read the timestamp as long
        timestamp = new Timestamp(time / 1000, (int) (time % 1000) * 1000);
        user = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeStringList(imageUrls);
        dest.writeStringList(likedBy);
        dest.writeStringList(tags); // Write the list of tags to the parcel
        dest.writeString(savedBy);
        dest.writeLong(timestamp.getSeconds() * 1000 + timestamp.getNanoseconds() / 1000000);  // Convert Timestamp to milliseconds
        dest.writeString(user);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Post> CREATOR = new Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
    public List<String> getLikedBy() { return likedBy; }
    public void setLikedBy(List<String> likedBy) { this.likedBy = likedBy; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    public String getSavedBy() { return savedBy; }
    public void setSavedBy(String savedBy) { this.savedBy = savedBy; }
    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }
}
