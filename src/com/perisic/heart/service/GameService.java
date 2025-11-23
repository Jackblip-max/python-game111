package com.perisic.heart.service;

import com.perisic.heart.model.*;
import com.perisic.heart.events.*;

/**
 * GameService with non-blocking game loading
 * Games load in background thread - NO MORE LAGGING!
 */
public class GameService {
    private GameServer gameServer;
    private GameEventDispatcher dispatcher;
    private GameSession session;
    private LeaderboardService leaderboardService;
    private boolean isLoadingGame = false;
    private boolean hasSessionEnded = false;  // ← ADDED: Prevent multiple endSession calls
    
    public GameService() {
        this.gameServer = new GameServer();
        this.dispatcher = GameEventDispatcher.getInstance();
        this.leaderboardService = LeaderboardService.getInstance();
    }
    
    // 30 SECONDS GAME MODE!
    public void startSession(Player player) {
        this.session = new GameSession(player, 30); // 30 seconds!
        this.hasSessionEnded = false;  // ← ADDED: Reset flag for new session
        dispatcher.notifyPlayerLoggedIn(new GameEvent.PlayerLoggedIn(player));
        loadNextGame();
    }
    
    /**
     * Load game in BACKGROUND THREAD - UI stays responsive!
     */
    public void loadNextGame() {
        // Don't load if already loading or session ended
        if (isLoadingGame || session == null || !session.isSessionActive()) {
            if (session != null && !session.isSessionActive()) {
                endSession();
            }
            return;
        }
        
        isLoadingGame = true;
        
        // Load game in background thread (doesn't block UI!)
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("📥 Loading game from API...");
                    
                    // This slow network call runs in background
                    Game game = gameServer.getRandomGame();
                    
                    // Check if session is still active
                    if (session != null && session.isSessionActive()) {
                        if (game != null) {
                            System.out.println("✅ Game loaded successfully!");
                            session.setCurrentGame(game);
                            dispatcher.notifyGameLoaded(new GameEvent.GameLoaded(game));
                        } else {
                            System.out.println("❌ Failed to load game from API");
                            // Retry after 1 second
                            Thread.sleep(1000);
                            isLoadingGame = false;
                            loadNextGame();
                        }
                    } else {
                        System.out.println("⏰ Session ended while loading game");
                    }
                    
                } catch (InterruptedException e) {
                    System.out.println("⚠️ Game loading interrupted: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("❌ Error loading game: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    isLoadingGame = false;
                }
            }
        }).start();
    }
    
    public boolean submitAnswer(int answer) {
        if (session == null || !session.isSessionActive()) {
            endSession();
            return false;
        }
        
        Player player = session.getPlayer();
        boolean correct = session.checkAnswer(answer);
        
        dispatcher.notifyAnswerSubmitted(
            new GameEvent.AnswerSubmitted(answer, correct, player)
        );
        
        leaderboardService.updatePlayer(
            player.getUsername(), 
            player.getScore(), 
            player.getAccuracy(),
            player.getTotalAttempts()
        );
        
        dispatcher.notifyScoreUpdated(new GameEvent.ScoreUpdated(player));
        
        return correct;
    }
    
    public void endSession() {
        // Prevent multiple calls to endSession
        if (hasSessionEnded) {
            System.out.println("⚠️ endSession already called, ignoring duplicate call");
            return;
        }
        
        if (session != null) {
            System.out.println("🎮 Ending game session...");
            hasSessionEnded = true;
            session.endSession();
            dispatcher.notifySessionEnded(new GameEvent.SessionEnded(session.getPlayer()));
        }
    }
    
    public GameSession getSession() {
        return session;
    }
}
