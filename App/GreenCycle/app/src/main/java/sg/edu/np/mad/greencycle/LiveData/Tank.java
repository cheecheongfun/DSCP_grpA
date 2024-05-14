package sg.edu.np.mad.greencycle.LiveData;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Tank implements Parcelable{
    private int tankID, numberOfWorms;
    private String tankName, dateCreated, description;

    public Tank(){

    }

    public Tank(int tankID, String tankName, String description, int numberOfWorms, String dateCreated){
        this.tankID = tankID;
        this.tankName = tankName;
        this.description = description;
        this.numberOfWorms = numberOfWorms;
        this.dateCreated = dateCreated;
    }

    protected Tank(Parcel in){
        tankID = in.readInt();
        tankName = in.readString();
        description = in.readString();
        numberOfWorms = in.readInt();
        dateCreated = in.readString();
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
    }

    public int getTankID() {return tankID;}
    public void setTankID(int tankID) {this.tankID = tankID;}
    public String getTankName() {return tankName;}
    public void setTankName(String tankName) {this.tankName = tankName;}
    public String getDescription() {return description;}
    public void setDescription(String description) {this.description = description;}
    public int getNumberOfWorms() {return numberOfWorms;}
    public void setNumberOfWorms(int numberOfWorms) {this.numberOfWorms = numberOfWorms;}
    public String getDateCreated() {return dateCreated;}
    public void setDateCreated(String dateCreated) {this.dateCreated = dateCreated;}
}
