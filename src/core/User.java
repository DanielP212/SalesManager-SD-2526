package core;

public class User {
    private final String username;
    private final String password;
    private final boolean isAdmin;

    public User(String username, String password, boolean isAdmin){
        this.username = username;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public boolean getIsAdmin(){
        return this.isAdmin;
    }

    public boolean attemptPassword(String pass){ return password.equals(pass); }
}
