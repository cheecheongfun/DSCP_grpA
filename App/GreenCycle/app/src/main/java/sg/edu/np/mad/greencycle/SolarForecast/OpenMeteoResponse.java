package sg.edu.np.mad.greencycle.SolarForecast;

import java.util.List;

public class OpenMeteoResponse {
    public Current current;
    public Hourly hourly;

    public static class Current {
        public String time;
        public int interval;
        public double temperature_2m;
        public int relative_humidity_2m;
        public int is_day;
        public double precipitation;
        public int cloud_cover;
    }

    public static class Hourly {
        public List<String> time;
        public List<Double> temperature_2m;
        public List<Integer> relative_humidity_2m;
        public List<Integer> precipitation_probability;
        public List<Double> precipitation;
        public List<Integer> cloud_cover;
    }
}
