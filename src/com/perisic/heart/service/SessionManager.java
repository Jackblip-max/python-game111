package com.perisic.heart.service;

import java.io.*;
import java.nio.file.*;

public class SessionManager {
    private static SessionManager instance;
    private Path sessionFile;
    
    private SessionManager() {
        String home = System.getProperty("user.home");
        sessionFile = Paths.get(home, ".heartgame_session.txt");
    }
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    public void saveSession(String username) {
        try {
            Files.write(sessionFile, username.getBytes());
        } catch (IOException e) {
            System.out.println("Could not save session: " + e.getMessage());
        }
    }
    
    public String loadSession() {
        try {
            if (Files.exists(sessionFile)) {
                return new String(Files.readAllBytes(sessionFile));
            }
        } catch (IOException e) {
            System.out.println("Could not load session: " + e.getMessage());
        }
        return null;
    }
    
    public void clearSession() {
        try {
            Files.deleteIfExists(sessionFile);
        } catch (IOException e) {
            System.out.println("Could not clear session: " + e.getMessage());
        }
    }
}
