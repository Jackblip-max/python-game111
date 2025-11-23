package com.perisic.heart.service;

import com.perisic.heart.model.LeaderboardEntry;
import com.perisic.heart.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

public class LeaderboardService {
    private static LeaderboardService instance;
    
    private LeaderboardService() {}
    
    public static LeaderboardService getInstance() {
        if (instance == null) {
            instance = new LeaderboardService();
        }
        return instance;
    }
    
    public void updatePlayer(String username, int score, double accuracy, int gamesPlayed) {
        // Data is automatically updated when PlayerDataService.savePlayer() is called
    }
    
    public List<LeaderboardEntry> getTopPlayers(int limit) {
        List<LeaderboardEntry> entries = new ArrayList<>();
        
        String sql = "SELECT * FROM leaderboard" + 
                     (limit > 0 ? " LIMIT ?" : "");
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (limit > 0) {
                stmt.setInt(1, limit);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                LeaderboardEntry entry = new LeaderboardEntry(
                    rs.getString("username"),
                    rs.getInt("total_score"),
                    rs.getDouble("accuracy"),
                    rs.getInt("games_played")
                );
                entries.add(entry);
            }
            
        } catch (SQLException e) {
            System.err.println("Error loading leaderboard: " + e.getMessage());
        }
        
        return entries;
    }
    
    public int getPlayerRank(String username) {
        String sql = "SELECT COUNT(*) + 1 as rank FROM leaderboard " +
                     "WHERE total_score > (SELECT total_score FROM leaderboard WHERE username = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("rank");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting rank: " + e.getMessage());
        }
        
        return -1;
    }
    
    public int getTotalPlayers() {
        String sql = "SELECT COUNT(*) as total FROM leaderboard";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("total");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting total players: " + e.getMessage());
        }
        
        return 0;
    }
}