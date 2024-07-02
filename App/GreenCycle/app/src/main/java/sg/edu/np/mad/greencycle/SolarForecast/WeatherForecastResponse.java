package sg.edu.np.mad.greencycle.SolarForecast;

import java.util.ArrayList;

public class WeatherForecastResponse {
    public ApiInfo api_info;
    public ArrayList<AreaMetadata> area_metadata;
    public ArrayList<Forecast> items;

    public static class ApiInfo {
        public String status;
    }

    public static class AreaMetadata {
        public String name;
        public LabelLocation label_location;

        public static class LabelLocation {
            public double longitude;
            public double latitude;
        }
    }

    public static class Forecast {
        public String update_timestamp;
        public String timestamp;
        public ValidPeriod valid_period;
        public General general;
        public ArrayList<Period> periods;

        public static class ValidPeriod {
            public String start;
            public String end;
        }

        public static class General {
            public String forecast;
            public Humidity relative_humidity;
            public Temperature temperature;
            public Wind wind;

            public static class Humidity {
                public int low;
                public int high;
            }

            public static class Temperature {
                public int low;
                public int high;
            }

            public static class Wind {
                public int low;
                public int high;
                public String direction;
            }
        }

        public static class Period {
            public Time time;
            public Regions regions;

            public static class Time {
                public String start;
                public String end;
            }

            public static class Regions {
                public String north;
                public String south;
                public String east;
                public String west;
                public String central;
            }
        }
    }
}
