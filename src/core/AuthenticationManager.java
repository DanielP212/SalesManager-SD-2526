package core;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationManager {
    private static final HashMap<String, User> registeredUsers = new HashMap<>();

    // ClientID, User logged in
    private static final HashMap<Integer, User> loggedInUsers = new HashMap<>();

    private static final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

    public boolean userExists(String username){
        readLock.lock();
        try{
            return registeredUsers.containsKey(username);
        } finally {
            readLock.unlock();
        }
    }

    public boolean registerUser(String username, String password, boolean isAdmin){
        if (userExists(username)) return false;
        writeLock.lock();
        try{
            registeredUsers.put(username, new User(username, password, isAdmin));
            return true;
        } finally {
            writeLock.unlock();
        }
    }

    public boolean loginUser(int clientID, String username, String password){
        User u = getUser(username);
        boolean loggedIn = u != null && u.attemptPassword(password);
        if (loggedIn){
            loggedInUsers.put(clientID, u);
            return true;
        }
        return false;
    }

    public static User getUser(String username){
        readLock.lock();
        try{
            return registeredUsers.get(username);
        } finally {
            readLock.unlock();
        }
    }

    public static boolean isUserAdmin(int clientID){
        readLock.lock();
        try{
            User u = loggedInUsers.get(clientID);
            if(u.getIsAdmin() == true){
                return true;
            }else return false;
        }finally {
            readLock.unlock();
        }
    }

    public boolean isClientLoggedIn(int requesterClient) {
        return loggedInUsers.containsKey(requesterClient);
    }
}
