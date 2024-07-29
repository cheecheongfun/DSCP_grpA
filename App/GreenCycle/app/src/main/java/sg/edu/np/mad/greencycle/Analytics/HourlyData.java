package sg.edu.np.mad.greencycle.Analytics;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class HourlyData {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String timestamp;
    public String tankId;  // Add this field
    public float ec;
    public float moisture;
    public float nitrogen;
    public float potassium;
    public float phosphorous;
    public float temperature;
    public float humidity;
    public float ph;
}
