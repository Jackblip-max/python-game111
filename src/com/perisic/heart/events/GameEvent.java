package com.perisic.heart.events;

import com.perisic.heart.model.Game;
import com.perisic.heart.model.Player;

public class GameEvent {
    
    public static class AnswerSubmitted {
        public final boolean correct;
        public final int answer;
        public final Player player;
        
        public AnswerSubmitted(int answer, boolean correct, Player player) {
            this.answer = answer;
            this.correct = correct;
            this.player = player;
        }
    }
    
    public static class GameLoaded {
        public final Game game;
        
        public GameLoaded(Game game) {
            this.game = game;
        }
    }
    
    public static class PlayerLoggedIn {
        public final Player player;
        
        public PlayerLoggedIn(Player player) {
            this.player = player;
        }
    }
    
    public static class ScoreUpdated {
        public final Player player;
        
        public ScoreUpdated(Player player) {
            this.player = player;
        }
    }
    
  
    public static class SessionEnded {
        public final Player player;
        
        public SessionEnded(Player player) {
            this.player = player;
        }
    }
}