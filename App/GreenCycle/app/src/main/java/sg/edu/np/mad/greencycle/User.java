package sg.edu.np.mad.greencycle;

public class User {
    private String username, password;

    public User(){}

    public User(String username, String password){
        this.password= password;
        this.username = username;
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
