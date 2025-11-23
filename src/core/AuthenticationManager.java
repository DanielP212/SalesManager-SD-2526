package core;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationManager {
    private final HashMap<String, User> registeredUsers = new HashMap<>();

    // ClientID, User logged in
    private final HashMap<Integer, User> loggedInUsers = new HashMap<>();

    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();

    public boolean userExists(String username){
        readLock.lock();
        try{
            return registeredUsers.containsKey(username);
        } finally {
            readLock.unlock();
        }
    }

    public boolean registerUser(String username, String password){
        if (userExists(username)) return false;
        writeLock.lock();
        try{
            registeredUsers.put(username, new User(username, password));
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

    public User getUser(String username){
        readLock.lock();
        try{
            return registeredUsers.get(username);
        } finally {
            readLock.unlock();
        }
    }

    public boolean isClientLoggedIn(int requesterClient) {
        return loggedInUsers.containsKey(requesterClient);
    }
}
