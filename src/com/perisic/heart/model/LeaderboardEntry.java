package com.perisic.heart.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class LeaderboardEntry implements Serializable, Comparable<LeaderboardEntry> {
    private static final long serialVersionUID = 1L;
    
    private String username;
    private int score;
    private double accuracy;
    private int gamesPlayed;
    private LocalDateTime lastPlayed;
    
    public LeaderboardEntry(String username, int score, double accuracy, int gamesPlayed) {
        this.username = username;
        this.score = score;
        this.accuracy = accuracy;
        this.gamesPlayed = gamesPlayed;
        this.lastPlayed = LocalDateTime.now();
    }
    
    public void updateStats(int newScore, double newAccuracy, int newGamesPlayed) {
        this.score = newScore;
        this.accuracy = newAccuracy;
        this.gamesPlayed = newGamesPlayed;
        this.lastPlayed = LocalDateTime.now();
    }
    
    @Override
    public int compareTo(LeaderboardEntry other) {
        if (this.score != other.score) {
            return Integer.compare(other.score, this.score);
        }
        return Double.compare(other.accuracy, this.accuracy);
    }
    
    public String getUsername() { return username; }
    public int getScore() { return score; }
    public double getAccuracy() { return accuracy; }
    public int getGamesPlayed() { return gamesPlayed; }
    public LocalDateTime getLastPlayed() { return lastPlayed; }
}
