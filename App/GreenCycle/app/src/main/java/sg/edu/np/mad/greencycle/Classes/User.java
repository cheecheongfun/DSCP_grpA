package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.LiveData.Tank;

public class User implements Parcelable {
    private String username;
    private String password;
    private String displayname; // Including displayname field
    private ArrayList<Tank> tanks;

    public User() {
        // Initialize tanks ArrayList
        tanks = new ArrayList<>();
    }

    public User(String username, String password, String displayname, ArrayList<Tank> tanks) {
        this.username = username;
        this.password = password;
        this.displayname = displayname; // Setting displayname in constructor
        this.tanks = tanks;
    }

    protected User(Parcel in) {
        username = in.readString();
        password = in.readString();
        displayname = in.readString(); // Reading displayname from Parcel
        tanks = new ArrayList<>();
        in.readList(tanks, Tank.class.getClassLoader());
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
        dest.writeString(displayname); // Writing displayname to Parcel
        dest.writeList(tanks);
    }

    // Method to add a tank to the user's list
    public void addTank(Tank tank) {
        tanks.add(tank);
    }

    // Method to remove a tank from the user's list
    public void removeTank(Tank tank) {
        tanks.remove(tank);
    }

    // Getters and Setters
    public ArrayList<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(ArrayList<Tank> tanks) {
        this.tanks = tanks;
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
}

//    public String getFingerprintId() {
//        return fingerprintId;
//    }
//
//    public void setFingerprintId(String fingerprintId) {
//        this.password = password;
//    }

