package sg.edu.np.mad.greencycle.SolarForecast;
import java.util.ArrayList;

public class HumidityResponse {
    public ArrayList<HumidityItem> items;

    public static class HumidityItem {
        public String timestamp;
        public ArrayList<StationReading> readings;
    }

    public static class StationReading {
        public String station_id;
        public float value;
    }
}


