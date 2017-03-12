package ua.vk.music.download.object;

public class User {

    private static User user;

    private String name;
    private String password;
    private String token;

    private User(){

    }

    private User(String name, String password){
        this.name = name;
        this.password = password;
    }

    public static User getInstance(){
        if(user == null) user = new User();
        return user;
    }

    public static User createUser(String name, String password){
        if(user == null) user = new User(name, password);
        return user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
