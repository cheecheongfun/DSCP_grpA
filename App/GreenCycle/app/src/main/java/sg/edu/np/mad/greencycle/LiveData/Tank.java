package sg.edu.np.mad.greencycle.LiveData;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;
import sg.edu.np.mad.greencycle.FeedingLog.Log;

import java.util.ArrayList;

// Fionn, S10240073K
public class Tank implements Parcelable {
    private int tankID, numberOfWorms;
    private double pHValue, temperature, humidity;
    private ArrayList<Double> npkValues; // Using ArrayList to store NPK values
    private ArrayList<Log> feedingLog;
    private String tankName, dateCreated, description;

    // Default Constructor
    public Tank() {
        this.npkValues = new ArrayList<>();
    }

    // Full Constructor
    public Tank(int tankID, String tankName, String description, int numberOfWorms,
                ArrayList<Double> npkValues, double temperature, double humidity, double pHValue, String dateCreated, ArrayList<Log>feedingLog) {
        this.tankID = tankID;
        this.tankName = tankName;
        this.description = description;
        this.numberOfWorms = numberOfWorms;
        this.npkValues = npkValues;
        this.temperature = temperature;
        this.humidity = humidity;
        this.pHValue = pHValue;
        this.dateCreated = dateCreated;
        this.feedingLog = feedingLog;
    }

    // Parcelable Constructor
    protected Tank(Parcel in) {
        tankID = in.readInt();
        tankName = in.readString();
        description = in.readString();
        numberOfWorms = in.readInt();
        dateCreated = in.readString();
        pHValue = in.readDouble();
        temperature = in.readDouble();
        humidity = in.readDouble();
        npkValues = new ArrayList<>();
        in.readList(this.npkValues, Double.class.getClassLoader()); // Read the list of NPK values
        feedingLog = new ArrayList<>();
        in.readList(this.feedingLog, Log.class.getClassLoader());
    }

    public static final Creator<Tank> CREATOR = new Creator<Tank>() {
        @Override
        public Tank createFromParcel(Parcel in) {
            return new Tank(in);
        }

        @Override
        public Tank[] newArray(int size) {
            return new Tank[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(tankID);
        dest.writeString(tankName);
        dest.writeString(description);
        dest.writeInt(numberOfWorms);
        dest.writeString(dateCreated);
        dest.writeDouble(pHValue);
        dest.writeDouble(temperature);
        dest.writeDouble(humidity);
        dest.writeList(npkValues); // Write the list of NPK values
        dest.writeList(feedingLog);
    }

    // Getters and Setters
    public int getTankID() { return tankID; }
    public void setTankID(int tankID) { this.tankID = tankID; }
    public String getTankName() { return tankName; }
    public void setTankName(String tankName) { this.tankName = tankName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getNumberOfWorms() { return numberOfWorms; }
    public void setNumberOfWorms(int numberOfWorms) { this.numberOfWorms = numberOfWorms; }
    public ArrayList<Double> getNpkValues() { return npkValues; }
    public void setNpkValues(ArrayList<Double> npkValues) { this.npkValues = npkValues; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getHumidity() {return humidity;}
    public void setHumidity(double humidity) {this.humidity = humidity;}
    public double getPHValue() { return pHValue; }
    public void setPHValue(double pHValue) { this.pHValue = pHValue; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }

    public ArrayList<Log> getFeedingLog() {return feedingLog;}
    public void setFeedingLog(ArrayList<Log> feedingLog) { this.feedingLog = feedingLog;}
    public void addLog(Log log) {
        feedingLog.add(log);
    }
    public void removeLog(Log log) {
        feedingLog.remove(log);
    }
}


//public class Tank implements Parcelable{
//    private int tankID, numberOfWorms;
//    private String tankName, dateCreated, description;
//
//    public Tank(){
//
//    }
//
//    public Tank(int tankID, String tankName, String description, int numberOfWorms, String dateCreated){
//        this.tankID = tankID;
//        this.tankName = tankName;
//        this.description = description;
//        this.numberOfWorms = numberOfWorms;
//        this.dateCreated = dateCreated;
//    }
//
//    protected Tank(Parcel in){
//        tankID = in.readInt();
//        tankName = in.readString();
//        description = in.readString();
//        numberOfWorms = in.readInt();
//        dateCreated = in.readString();
//    }
//
//    public static final Creator<Tank> CREATOR = new Creator<Tank>() {
//        @Override
//        public Tank createFromParcel(Parcel in) {
//            return new Tank(in);
//        }
//
//        @Override
//        public Tank[] newArray(int size) {
//            return new Tank[size];
//        }
//    };
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(@NonNull Parcel dest, int flags) {
//        dest.writeInt(tankID);
//        dest.writeString(tankName);
//        dest.writeString(description);
//        dest.writeInt(numberOfWorms);
//        dest.writeString(dateCreated);
//    }
//
//    public int getTankID() {return tankID;}
//    public void setTankID(int tankID) {this.tankID = tankID;}
//    public String getTankName() {return tankName;}
//    public void setTankName(String tankName) {this.tankName = tankName;}
//    public String getDescription() {return description;}
//    public void setDescription(String description) {this.description = description;}
//    public int getNumberOfWorms() {return numberOfWorms;}
//    public void setNumberOfWorms(int numberOfWorms) {this.numberOfWorms = numberOfWorms;}
//    public String getDateCreated() {return dateCreated;}
//    public void setDateCreated(String dateCreated) {this.dateCreated = dateCreated;}
//}
