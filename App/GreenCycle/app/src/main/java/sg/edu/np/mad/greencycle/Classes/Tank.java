package sg.edu.np.mad.greencycle.Classes;

public class Tank {
    private String name;
    private String description;
    private int numberOfWorms;
    private double[] npkValues;  // Array to store Nitrogen, Potassium, and Phosphorous values
    private double temperature;
    private double pHValue;

    // Constructor
    public Tank(String name, String description, int numberOfWorms, double[] npkValues, double temperature, double pHValue) {
        this.name = name;
        this.description = description;
        this.numberOfWorms = numberOfWorms;
        this.npkValues = npkValues;
        this.temperature = temperature;
        this.pHValue = pHValue;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getNumberOfWorms() {
        return numberOfWorms;
    }

    public void setNumberOfWorms(int numberOfWorms) {
        this.numberOfWorms = numberOfWorms;
    }

    public double[] getNpkValues() {
        return npkValues;
    }

    public void setNpkValues(double[] npkValues) {
        this.npkValues = npkValues;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPHValue() {
        return pHValue;
    }

    public void setPHValue(double pHValue) {
        this.pHValue = pHValue;
    }
}

