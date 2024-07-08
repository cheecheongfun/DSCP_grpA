package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.Forum.Comment;

public class User implements Parcelable {
    private String username;
    private String password;
    private String displayname;
    private ArrayList<Tank> tanks;
    private ArrayList<String> likedPosts;
    private ArrayList<String> comments;
    private String salt;

    public User() {
        tanks = new ArrayList<>();
        likedPosts = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public User(String username, String password, String displayname, ArrayList<Tank> tanks, ArrayList<String> likedPosts, ArrayList<String> comments, String salt) {
        this.username = username;
        this.password = password;
        this.displayname = displayname;
        this.tanks = tanks;
        this.likedPosts = likedPosts;
        this.comments = comments;
        this.salt = salt;
    }

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        displayname = in.readString();
        tanks = new ArrayList<>();
        in.readList(tanks, Tank.class.getClassLoader());
        likedPosts = new ArrayList<>();
        in.readList(likedPosts, String.class.getClassLoader());
        comments = new ArrayList<>();
        in.readList(comments, String.class.getClassLoader());
        salt = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(displayname);
        dest.writeList(tanks);
        dest.writeList(likedPosts);
        dest.writeList(comments);
        dest.writeString(salt);
    }

    // Methods to manipulate tanks
    public void addTank(Tank tank) {
        tanks.add(tank);
    }

    public void removeTank(Tank tank) {
        tanks.remove(tank);
    }

    // Methods to manipulate likedPosts
    public void addLikedPost(String postId) {
        likedPosts.add(postId);
    }

    public void removeLikedPost(String postId) {
        likedPosts.remove(postId);
    }

    // Methods to manipulate comments
    public void addComment(String commentId) {
        comments.add(commentId);
    }

    public void removeComment(String commentId) {
        comments.remove(commentId);
    }

    // Getters and Setters
    public ArrayList<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(ArrayList<Tank> tanks) {
        this.tanks = tanks;
    }

    public ArrayList<String> getLikedPosts() {
        return likedPosts;
    }

    public void setLikedPosts(ArrayList<String> likedPosts) {
        this.likedPosts = likedPosts;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    public void setComments(ArrayList<String> comments) {
        this.comments = comments;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }
}
