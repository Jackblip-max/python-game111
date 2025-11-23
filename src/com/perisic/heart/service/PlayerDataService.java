package com.perisic.heart.service;

import com.perisic.heart.model.Player;
import com.perisic.heart.database.DatabaseConnection;
import java.sql.*;

public class PlayerDataService {
    private static PlayerDataService instance;
    
    private PlayerDataService() {}
    
    public static PlayerDataService getInstance() {
        if (instance == null) {
            instance = new PlayerDataService();
        }
        return instance;
    }
    
    public Player loadPlayer(String username) {
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
    
    public void savePlayer(Player player) {
        String sql = "UPDATE player_stats ps " +
                     "JOIN users u ON ps.user_id = u.user_id " +
                     "SET ps.total_score = ?, " +
                     "    ps.correct_answers = ?, " +
                     "    ps.total_attempts = ?, " +
                     "    ps.games_played = ps.games_played + 1, " +
                     "    ps.last_played = CURRENT_TIMESTAMP " +
                     "WHERE u.username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, player.getScore());
            stmt.setInt(2, player.getCorrectAnswers());
            stmt.setInt(3, player.getTotalAttempts());
            stmt.setString(4, player.getUsername());
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error saving player: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void saveSession(String username, int sessionScore, double sessionAccuracy, int gamesInSession) {
        String sql = "INSERT INTO game_sessions " +
                     "(user_id, session_score, session_accuracy, games_in_session) " +
                     "SELECT user_id, ?, ?, ? FROM users WHERE username = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionScore);
            stmt.setDouble(2, sessionAccuracy);
            stmt.setInt(3, gamesInSession);
            stmt.setString(4, username);
            
            stmt.executeUpdate();
            
            updateBestSession(username, sessionScore);
            
        } catch (SQLException e) {
            System.err.println("Error saving session: " + e.getMessage());
        }
    }
    
    private void updateBestSession(String username, int sessionScore) {
        String sql = "UPDATE player_stats ps " +
                     "JOIN users u ON ps.user_id = u.user_id " +
                     "SET ps.best_session_score = ? " +
                     "WHERE u.username = ? AND ps.best_session_score < ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, sessionScore);
            stmt.setString(2, username);
            stmt.setInt(3, sessionScore);
            
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating best session: " + e.getMessage());
        }
    }
}