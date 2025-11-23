package com.perisic.heart;

import com.perisic.heart.database.DatabaseConnection;

public class TestDatabase {
    public static void main(String[] args) {
        System.out.println("Testing MySQL connection...");
        
        if (DatabaseConnection.testConnection()) {
            System.out.println("✅ SUCCESS! Database is connected!");
            System.out.println("You're ready to proceed!");
        } else {
            System.out.println("❌ FAILED! Check:");
            System.out.println("1. MySQL is running");
            System.out.println("2. Password in DatabaseConnection.java is correct");
            System.out.println("3. Database 'heart_game' exists");
        }
    }
}