
package com.perisic.heart.service;

import javax.sound.sampled.*;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Sound Manager - Plays sound files from sounds/ folder
 * Falls back to system beeps if files not found
 */
public class SoundManager {
    private static SoundManager instance;
    private Map<String, Clip> soundClips;
    private boolean soundEnabled = true;
    
    private static final String SOUNDS_DIR = "sounds/";
    
    private SoundManager() {
        soundClips = new HashMap<>();
        loadSounds();
    }
    
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * Load all sound files from sounds/ folder
     */
    private void loadSounds() {
        System.out.println("🔊 Loading sound files...");
        
        loadSound("correct", SOUNDS_DIR + "correct.wav");
        loadSound("wrong", SOUNDS_DIR + "wrong.wav");
        loadSound("tick", SOUNDS_DIR + "tick.wav");
        loadSound("gameover", SOUNDS_DIR + "gameover.wav");
        loadSound("achievement", SOUNDS_DIR + "achievement.wav");
        
        if (soundClips.isEmpty()) {
            System.out.println("⚠️  No sound files found. Using system beeps.");
            System.out.println("💡 To add sounds:");
            System.out.println("   1. Create 'sounds/' folder in project root");
            System.out.println("   2. Add these files:");
            System.out.println("      - correct.wav (success sound)");
            System.out.println("      - wrong.wav (error sound)");
            System.out.println("      - tick.wav (timer tick)");
            System.out.println("      - gameover.wav (time's up)");
            System.out.println("      - achievement.wav (high score)");
        } else {
            System.out.println("✅ Loaded " + soundClips.size() + " sound files!");
        }
    }
    
    /**
     * Load a single sound file
     */
    private void loadSound(String name, String filePath) {
        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioStream);
                soundClips.put(name, clip);
                System.out.println("   ✓ " + name + ".wav loaded");
            } else {
                System.out.println("   ✗ " + name + ".wav not found");
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.out.println("   ✗ " + name + ".wav error: " + e.getMessage());
        }
    }
    
    /**
     * Play correct answer sound
     */
    public void playCorrectSound() {
        if (!soundEnabled) return;
        
        if (soundClips.containsKey("correct")) {
            playClip("correct");
        } else {
            // Fallback: Two quick beeps
            new Thread(new Runnable() {
                public void run() {
                    Toolkit.getDefaultToolkit().beep();
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    Toolkit.getDefaultToolkit().beep();
                }
            }).start();
        }
    }
    
    /**
     * Play wrong answer sound
     */
    public void playWrongSound() {
        if (!soundEnabled) return;
        
        if (soundClips.containsKey("wrong")) {
            playClip("wrong");
        } else {
            // Fallback: One low beep
            Toolkit.getDefaultToolkit().beep();
        }
    }
    
    /**
     * Play timer tick sound
     */
    public void playTickSound() {
        if (!soundEnabled) return;
        
        if (soundClips.containsKey("tick")) {
            playClip("tick");
        } else {
            // Fallback: Quick beep (only if tick.wav not found)
            new Thread(new Runnable() {
                public void run() {
                    Toolkit.getDefaultToolkit().beep();
                }
            }).start();
        }
    }
    
    /**
     * Play game over sound
     */
    public void playGameOverSound() {
        if (!soundEnabled) return;
        
        if (soundClips.containsKey("gameover")) {
            playClip("gameover");
        } else {
            // Fallback: Three beeps
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        try { Thread.sleep(200); } catch (InterruptedException e) {}
                    }
                }
            }).start();
        }
    }
    
    /**
     * Play achievement sound
     */
    public void playAchievementSound() {
        if (!soundEnabled) return;
        
        if (soundClips.containsKey("achievement")) {
            playClip("achievement");
        } else {
            // Fallback: Victory beeps
            new Thread(new Runnable() {
                public void run() {
                    for (int i = 0; i < 3; i++) {
                        Toolkit.getDefaultToolkit().beep();
                        try { Thread.sleep(150); } catch (InterruptedException e) {}
                    }
                }
            }).start();
        }
    }
    
    /**
     * Play a sound clip from loaded files
     */
    private void playClip(String name) {
        if (soundClips.containsKey(name)) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        Clip clip = soundClips.get(name);
                        if (clip != null) {
                            clip.setFramePosition(0); // Rewind to start
                            clip.start();
                            System.out.println("🔊 Playing: " + name + ".wav"); // Debug log
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error playing " + name + ": " + e.getMessage());
                    }
                }
            }).start();
        } else {
            System.out.println("⚠️  " + name + ".wav not loaded, using beep"); // Debug log
        }
    }
    
    /**
     * Toggle sound on/off
     */
    public void toggleSound() {
        soundEnabled = !soundEnabled;
        System.out.println(soundEnabled ? "🔊 Sound enabled" : "🔇 Sound muted");
    }
    
    /**
     * Check if sound is enabled
     */
    public boolean isSoundEnabled() {
        return soundEnabled;
    }
    
    /**
     * Set sound enabled/disabled
     */
    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }
}