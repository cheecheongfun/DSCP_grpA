package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Tank implements Parcelable {
    private int tankID, numberOfWorms;
    private double pHValue, temperature, moisture, EC;
    private ArrayList<Double> npkValues; // Using ArrayList to store NPK values
    private ArrayList<Log> feedingLog;
    private ArrayList<FeedSchedule> feedSchedule; // Added field
    private String tankName, dateCreated, description, deviceID;

    // Default Constructor
    public Tank() {
        this.npkValues = new ArrayList<>();
        this.feedingLog = new ArrayList<>();
        this.feedSchedule = new ArrayList<>();
    }

    // Full Constructor
    public Tank(int tankID, String deviceID, String tankName, String description, int numberOfWorms,
                ArrayList<Double> npkValues, double temperature, double EC, double pHValue, double moisture,
                String dateCreated, ArrayList<Log> feedingLog, ArrayList<FeedSchedule> feedSchedule) {
        this.tankID = tankID;
        this.deviceID = deviceID;
        this.tankName = tankName;
        this.description = description;
        this.numberOfWorms = numberOfWorms;
        this.npkValues = npkValues;
        this.temperature = temperature;
        this.EC = EC;
        this.moisture = moisture;
        this.pHValue = pHValue;
        this.dateCreated = dateCreated;
        this.feedingLog = feedingLog;
        this.feedSchedule = feedSchedule;
    }

    // Parcelable Constructor
    protected Tank(Parcel in) {
        tankID = in.readInt();
        deviceID = in.readString();
        tankName = in.readString();
        description = in.readString();
        numberOfWorms = in.readInt();
        dateCreated = in.readString();
        pHValue = in.readDouble();
        temperature = in.readDouble();
        EC = in.readDouble();
        moisture = in.readDouble();
        npkValues = new ArrayList<>();
        in.readList(this.npkValues, Double.class.getClassLoader()); // Read the list of NPK values
        feedingLog = new ArrayList<>();
        in.readList(this.feedingLog, Log.class.getClassLoader());
        feedSchedule = new ArrayList<>();
        in.readList(this.feedSchedule, FeedSchedule.class.getClassLoader());
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
        dest.writeString(deviceID);
        dest.writeString(tankName);
        dest.writeString(description);
        dest.writeInt(numberOfWorms);
        dest.writeString(dateCreated);
        dest.writeDouble(pHValue);
        dest.writeDouble(temperature);
        dest.writeDouble(moisture);
        dest.writeDouble(EC);
        dest.writeList(npkValues); // Write the list of NPK values
        dest.writeList(feedingLog);
        dest.writeList(feedSchedule); // Write feedSchedule to parcel
    }

    // Getters and Setters
    public int getTankID() { return tankID; }
    public void setTankID(int tankID) { this.tankID = tankID; }
    public String getDeviceID() {return deviceID;}
    public void setDeviceID(String deviceID) { this.deviceID = deviceID;}
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
    public double getMoisture() { return moisture; }
    public void setMoisture(double moisture) { this.moisture = moisture; }
    public double getEC() { return EC; }
    public void setEC(double EC) { this.EC = EC; }
    public double getPHValue() { return pHValue; }
    public void setPHValue(double pHValue) { this.pHValue = pHValue; }
    public String getDateCreated() { return dateCreated; }
    public void setDateCreated(String dateCreated) { this.dateCreated = dateCreated; }
    public ArrayList<Log> getFeedingLog() { return feedingLog; }
    public void setFeedingLog(ArrayList<Log> feedingLog) { this.feedingLog = feedingLog; }
    public ArrayList<FeedSchedule> getFeedSchedule() { return feedSchedule; }
    public void setFeedSchedule(ArrayList<FeedSchedule> feedSchedule) { this.feedSchedule = feedSchedule; }
    public void addLog(Log log) {
        feedingLog.add(log);
    }
    public void removeLog(Log log) {
        feedingLog.remove(log);
    }

}
