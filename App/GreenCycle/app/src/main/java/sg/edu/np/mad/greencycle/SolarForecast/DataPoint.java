package sg.edu.np.mad.greencycle.SolarForecast;

import java.time.LocalDate;

public class DataPoint {
    private LocalDate date;
    private double humidity;
    private double airTemp;
    private double rainFall;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public double getAirTemp() { return airTemp; }
    public void setAirTemp(double airTemp) { this.airTemp = airTemp; }
    public double getRainFall() { return rainFall; }
    public void setRainFall(double rainFall) { this.rainFall = rainFall; }
}
