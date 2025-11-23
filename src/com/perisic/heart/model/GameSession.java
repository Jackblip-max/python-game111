package com.perisic.heart.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class GameSession {
    private Player player;
    private Game currentGame;
    private LocalDateTime startTime;
    private boolean sessionActive;
    private int sessionDuration; 
    private int sessionScore; 
    private int sessionAttempts; 
    
    public GameSession(Player player, int durationSeconds) {
        this.player = player;
        this.startTime = LocalDateTime.now();
        this.sessionActive = true;
        this.sessionDuration = durationSeconds;
        this.sessionScore = 0;
        this.sessionAttempts = 0;
    }
    
    public void setCurrentGame(Game game) {
        this.currentGame = game;
    }
    
    public boolean checkAnswer(int answer) {
        if (currentGame == null) return false;
        boolean correct = (currentGame.getSolution() == answer);
        sessionAttempts++;
        if (correct) {
            sessionScore++;
        }
        player.recordAnswer(correct);
        return correct;
    }
    
    public long getElapsedSeconds() {
        return Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }
    
    public long getRemainingSeconds() {
        long remaining = sessionDuration - getElapsedSeconds();
        return remaining > 0 ? remaining : 0;
    }
    
    public boolean isSessionActive() {
        return sessionActive && getRemainingSeconds() > 0;
    }
    
    public void endSession() {
        this.sessionActive = false;
    }
    
    public double getSessionAccuracy() {
        if (sessionAttempts == 0) return 0;
        return (sessionScore * 100.0) / sessionAttempts;
    }
    
    public Player getPlayer() { return player; }
    public Game getCurrentGame() { return currentGame; }
    public LocalDateTime getStartTime() { return startTime; }
    public int getSessionDuration() { return sessionDuration; }
    public int getSessionScore() { return sessionScore; }
    public int getSessionAttempts() { return sessionAttempts; }
}