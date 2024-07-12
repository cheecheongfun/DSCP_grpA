package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;

public class Log implements Parcelable {
    private int logId;
    private String logDate;
    private ArrayList<Food> greens, browns;
    private String notes;

    public Log() {
    }

    public Log(int logId, String logDate, ArrayList<Food> greens, ArrayList<Food> browns, String notes) {
        this.logId = logId;
        this.logDate = logDate;
        this.greens = greens;
        this.browns = browns;
        this.notes = notes;
    }

    protected Log(Parcel in) {
        logId = in.readInt();
        logDate = in.readString();
        greens = new ArrayList<>();
        in.readList(this.greens, Food.class.getClassLoader());
        browns = new ArrayList<>();
        in.readList(this.browns, Food.class.getClassLoader());
        notes = in.readString();
    }

    public static final Creator<Log> CREATOR = new Creator<Log>() {
        @Override
        public Log createFromParcel(Parcel in) {
            return new Log(in);
        }

        @Override
        public Log[] newArray(int size) {
            return new Log[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(logId);
        parcel.writeString(logDate);
        parcel.writeList(greens);
        parcel.writeList(browns);
        parcel.writeString(notes);
    }

    // Getters and Setters
    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public String getLogDate() {
        return logDate;
    }

    public void setLogDate(String logDate) {
        this.logDate = logDate;
    }

    public ArrayList<Food> getGreens() {
        return greens;
    }

    public void setGreens(ArrayList<Food> greens) {
        this.greens = greens;
    }

    public ArrayList<Food> getBrowns() {
        return browns;
    }

    public void setBrowns(ArrayList<Food> browns) {
        this.browns = browns;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
