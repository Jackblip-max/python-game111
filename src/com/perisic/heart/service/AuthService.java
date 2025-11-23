package com.perisic.heart.service;

import com.perisic.heart.model.Player;
import com.perisic.heart.database.DatabaseConnection;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Base64;

public class AuthService {
    private static AuthService instance;
    
    private AuthService() {
        DatabaseConnection.getConnection();
    }
    
    public static AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }
    
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) {
                    int userId = keys.getInt(1);
                    createInitialStats(userId);
                }
                return true;
            }
            
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("Username already exists");
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    public Player login(String username, String password) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password_hash = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, hashPassword(password));
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                updateLastLogin(rs.getInt("user_id"));
                return loadPlayerFromDatabase(username);
            }
            
        } catch (SQLException e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    private Player loadPlayerFromDatabase(String username) {
        String sql = "SELECT ps.total_score, ps.correct_answers, ps.total_attempts " +
                     "FROM users u JOIN player_stats ps ON u.user_id = ps.user_id " +
                     "WHERE u.username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Player player = new Player(username);
                player.setStats(
                    rs.getInt("total_score"),
                    rs.getInt("correct_answers"),
                    rs.getInt("total_attempts")
                );
                return player;
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading player: " + e.getMessage());
        }
        
        return new Player(username);
    }
    
    private void createInitialStats(int userId) {
        String sql = "INSERT INTO player_stats (user_id, total_score, correct_answers, total_attempts) " +
                     "VALUES (?, 0, 0, 0)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error creating initial stats: " + e.getMessage());
        }
    }
    
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating last login: " + e.getMessage());
        }
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return password;
        }
    }
}