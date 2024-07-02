package sg.edu.np.mad.greencycle.SolarForecast;

import java.util.ArrayList;

public class RainfallResponse {
    public ApiInfo api_info;
    public Metadata metadata;
    public ArrayList<RainfallItem> items;

    public static class ApiInfo {
        public String status;
    }

    public static class Metadata {
        public ArrayList<Station> stations;
        public String reading_type;
        public String reading_unit;

        public static class Station {
            public String id;
            public String device_id;
            public String name;
            public Location location;

            public static class Location {
                public double longitude;
                public double latitude;
            }
        }
    }

    public static class RainfallItem {
        public String timestamp;
        public ArrayList<StationReading> readings;

        public static class StationReading {
            public String station_id;
            public float value;
        }
    }
}
