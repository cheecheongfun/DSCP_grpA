package sg.edu.np.mad.greencycle.Classes;


import java.util.List;

public class ForecastResponse {
    public Hourly hourly;

    public static class Hourly {
        public List<String> time;
        public List<Double> temperature_2m;
        public List<Double> relative_humidity_2m;
        public List<Double> precipitation;
        public List<Double> cloud_cover;
    }
}
