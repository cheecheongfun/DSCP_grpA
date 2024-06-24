package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;

public class FeedSchedule implements Parcelable {
    private String scheduleName;
    private ArrayList<Food> greenFood;
    private ArrayList<Food> brownFood;
    private String repeatType; // "None", "Daily", "Weekly", "Monthly", "Custom"
    private HashMap<String, ArrayList<String>> repeatDetails; // Additional details like days for weekly repeats
    private String notification;
    private int waterAmt;
    private String startDate; // Store startDate as String
    public FeedSchedule(){

    }

    // Constructor
    public FeedSchedule(String scheduleName, ArrayList<Food> greenFood, ArrayList<Food> brownFood, String repeatType, HashMap<String, ArrayList<String>> repeatDetails, String notification, int waterAmt, String startDate) {
        this.scheduleName = scheduleName;
        this.greenFood = greenFood;
        this.brownFood = brownFood;
        this.repeatType = repeatType;
        this.repeatDetails = repeatDetails;
        this.notification = notification;
        this.waterAmt = waterAmt;
        this.startDate = startDate; // Store startDate as String
    }

    // Getter and setter methods
    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public ArrayList<Food> getGreenFood() {
        return greenFood;
    }

    public void setGreenFood(ArrayList<Food> greenFood) {
        this.greenFood = greenFood;
    }

    public ArrayList<Food> getBrownFood() {
        return brownFood;
    }

    public void setBrownFood(ArrayList<Food> brownFood) {
        this.brownFood = brownFood;
    }

    public String getRepeatType() {
        return repeatType;
    }

    public void setRepeatType(String repeatType) {
        this.repeatType = repeatType;
    }

    public HashMap<String, ArrayList<String>> getRepeatDetails() {
        return repeatDetails;
    }

    public void setRepeatDetails(HashMap<String, ArrayList<String>> repeatDetails) {
        this.repeatDetails = repeatDetails;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public int getWaterAmt() {
        return waterAmt;
    }

    public void setWaterAmt(int waterAmt) {
        this.waterAmt = waterAmt;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    // Parcelable implementation
    protected FeedSchedule(Parcel in) {
        scheduleName = in.readString();
        greenFood = in.createTypedArrayList(Food.CREATOR);
        brownFood = in.createTypedArrayList(Food.CREATOR);
        repeatType = in.readString();
        repeatDetails = (HashMap<String, ArrayList<String>>) in.readSerializable();
        notification = in.readString();
        waterAmt = in.readInt();
        startDate = in.readString(); // Read String
    }

    public static final Creator<FeedSchedule> CREATOR = new Creator<FeedSchedule>() {
        @Override
        public FeedSchedule createFromParcel(Parcel in) {
            return new FeedSchedule(in);
        }

        @Override
        public FeedSchedule[] newArray(int size) {
            return new FeedSchedule[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(scheduleName);
        parcel.writeTypedList(greenFood);
        parcel.writeTypedList(brownFood);
        parcel.writeString(repeatType);
        parcel.writeSerializable(repeatDetails);
        parcel.writeString(notification);
        parcel.writeInt(waterAmt);
        parcel.writeString(startDate); // Write String
    }
}
