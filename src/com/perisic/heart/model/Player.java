package com.perisic.heart.model;

public class Player {
    private String username;
    private int totalScore;
    private int correctAnswers;
    private int totalAttempts;
    
    public Player(String username) {
        this.username = username;
        this.totalScore = 0;
        this.correctAnswers = 0;
        this.totalAttempts = 0;
    }
    
    public void recordAnswer(boolean correct) {
        totalAttempts++;
        if (correct) {
            correctAnswers++;
            totalScore++;
        }
    }
    
    // NEW METHOD to restore saved progress!
    public void setStats(int score, int correct, int attempts) {
        this.totalScore = score;
        this.correctAnswers = correct;
        this.totalAttempts = attempts;
    }
    
    public String getUsername() { return username; }
    public int getScore() { return totalScore; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getTotalAttempts() { return totalAttempts; }
    
    public double getAccuracy() {
        if (totalAttempts == 0) return 0;
        return (correctAnswers * 100.0) / totalAttempts;
    }
}
