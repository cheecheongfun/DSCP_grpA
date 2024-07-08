package sg.edu.np.mad.greencycle.Classes;

import android.os.Parcel;
import android.os.Parcelable;

public class Food implements Parcelable {
    private String name;
    private double amount;

    // Constructor
    public Food(){

    }
    public Food(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    // Parcelable implementation
    protected Food(Parcel in) {
        name = in.readString();
        amount = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeDouble(amount);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<Food> CREATOR = new Creator<Food>() {
        @Override
        public Food createFromParcel(Parcel in) { return new Food(in); }

        @Override
        public Food[] newArray(int size) { return new Food[size]; }
    };

    // Getter and Setter methods
    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }
}
