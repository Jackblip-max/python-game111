package com.perisic.heart.events;

import java.util.ArrayList;
import java.util.List;

public class GameEventDispatcher {
    private static GameEventDispatcher instance;
    private List<GameEventListener> listeners = new ArrayList<>();
    
    public interface GameEventListener {
        void onAnswerSubmitted(GameEvent.AnswerSubmitted event);
        void onGameLoaded(GameEvent.GameLoaded event);
        void onPlayerLoggedIn(GameEvent.PlayerLoggedIn event);
        void onScoreUpdated(GameEvent.ScoreUpdated event);
        void onSessionEnded(GameEvent.SessionEnded event); 
    }
    
    private GameEventDispatcher() {}
    
    public static GameEventDispatcher getInstance() {
        if (instance == null) {
            instance = new GameEventDispatcher();
        }
        return instance;
    }
    
    public void addListener(GameEventListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyAnswerSubmitted(GameEvent.AnswerSubmitted event) {
        for (GameEventListener listener : listeners) {
            listener.onAnswerSubmitted(event);
        }
    }
    
    public void notifyGameLoaded(GameEvent.GameLoaded event) {
        for (GameEventListener listener : listeners) {
            listener.onGameLoaded(event);
        }
    }
    
    public void notifyPlayerLoggedIn(GameEvent.PlayerLoggedIn event) {
        for (GameEventListener listener : listeners) {
            listener.onPlayerLoggedIn(event);
        }
    }
    
    public void notifyScoreUpdated(GameEvent.ScoreUpdated event) {
        for (GameEventListener listener : listeners) {
            listener.onScoreUpdated(event);
        }
    }
    
    public void notifySessionEnded(GameEvent.SessionEnded event) {
        for (GameEventListener listener : listeners) {
            listener.onSessionEnded(event);
        }
    }
}