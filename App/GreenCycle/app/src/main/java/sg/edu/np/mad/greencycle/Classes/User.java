package sg.edu.np.mad.greencycle.Classes;

import java.util.ArrayList;

import sg.edu.np.mad.greencycle.LiveData.Tank;

public class User {
    private String username, password;
    private ArrayList<Tank> tanks;

    public User(){}

    public User(String username, String password){
        this.password= password;
        this.username = username;
        this.tanks = new ArrayList<>();
    }

    // Method to add a tank to the user's list
    public void addTank(Tank tank) {
        tanks.add(tank);
    }

    // Method to remove a tank from the user's list
    public void removeTank(Tank tank) {
        tanks.remove(tank);
    }

    // Getters and Setters
    public ArrayList<Tank> getTanks() {
        return tanks;
    }

    public void setTanks(ArrayList<Tank> tanks) {
        this.tanks = tanks;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

