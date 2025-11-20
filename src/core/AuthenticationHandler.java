package core;

import comms.Packet;

import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AuthenticationHandler {
    private final HashMap<String, User> registeredUsers = new HashMap<>();

    private final Lock readLock = new ReentrantLock();
    private final Lock writeLock = new ReentrantLock();


    public void handle(Packet packet){

    }

    public boolean userExists(String username){
        readLock.lock();
        try{
            return registeredUsers.containsKey(username);
        } finally {
            readLock.unlock();
        }
    }

    public void registerUser(String username, String password){
        if (userExists(username)) return;
        writeLock.lock();
        registeredUsers.put(username, new User(username, password));
        writeLock.unlock();
    }

    public boolean loginUser(String username, String password){
        User u = getUser(username);
        return u != null && u.attemptPassword(password);
    }

    public User getUser(String username){
        readLock.lock();
        try{
            return registeredUsers.get(username);
        } finally {
            readLock.unlock();
        }
    }
}
