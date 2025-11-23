package com.perisic.heart.gui;

import com.perisic.heart.model.Player;
import com.perisic.heart.service.*;
import com.perisic.heart.events.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class GameWindow extends JFrame implements GameEventDispatcher.GameEventListener {
    
    private GameService gameService;
    private SoundManager soundManager;  // ← ADDED: Sound manager
    private JLabel imageLabel;
    private JLabel scoreLabel;
    private JLabel accuracyLabel;
    private JLabel feedbackLabel;
    private JLabel rankLabel;
    private JLabel timerLabel;
    private JProgressBar timerProgress;
    private Timer countdownTimer;
    private Timer answerDelayTimer;  // ← ADDED: Track the answer delay timer
    private JButton[] numberButtons;  // ← ADDED: Track all number buttons
    private boolean isShowingGameOver = false;  // ← ADDED: Prevent multiple dialogs
    private JButton soundToggleBtn;  // ← ADDED: Sound toggle button
    
    // Classic professional colors
    private static final Color DARK_BLUE = new Color(31, 58, 96);
    private static final Color LIGHT_BLUE = new Color(52, 152, 219);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color DANGER = new Color(231, 76, 60);
    private static final Color WARNING = new Color(243, 156, 18);
    private static final Color LIGHT_GRAY = new Color(236, 240, 241);
    private static final Color DARK_GRAY = new Color(52, 73, 94);
    
    public GameWindow(Player player) {
        gameService = new GameService();
        soundManager = SoundManager.getInstance();  // ← ADDED: Initialize sound manager
        GameEventDispatcher.getInstance().addListener(this);
        
        setupClassicGameWindow(player);
        gameService.startSession(player);
        startCountdownTimer();
    }
    
    private void setupClassicGameWindow(Player player) {
        setTitle("Heart Game - " + player.getUsername());
        setSize(1100, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(LIGHT_GRAY);
        
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setBackground(LIGHT_GRAY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Top: Timer + Stats (COMPACT)
        JPanel topPanel = createCompactTopPanel();
        
        // Center: BALANCED GAME IMAGE
        JPanel centerPanel = createBalancedGamePanel();
        
        // Bottom: Number buttons
        JPanel bottomPanel = createButtonPanel();
        
        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createCompactTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(8, 8));
        topPanel.setBackground(LIGHT_GRAY);
        topPanel.setPreferredSize(new Dimension(1080, 200)); 
        
        // Timer (left)
        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBackground(DARK_BLUE);
        timerPanel.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        timerPanel.setPreferredSize(new Dimension(180, 200)); 
        
        timerLabel = new JLabel("30", JLabel.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 40)); 
        timerLabel.setForeground(Color.WHITE);
        
        JLabel timerText = new JLabel("seconds", JLabel.CENTER);
        timerText.setFont(new Font("Arial", Font.PLAIN, 11));
        timerText.setForeground(new Color(255, 255, 255, 180));
        
        timerProgress = new JProgressBar(0, 30);
        timerProgress.setValue(30);
        timerProgress.setStringPainted(false);
        timerProgress.setForeground(SUCCESS);
        timerProgress.setBackground(DARK_BLUE.darker());
        timerProgress.setBorderPainted(false);
        timerProgress.setPreferredSize(new Dimension(150, 6));
        
        JPanel timerContent = new JPanel();
        timerContent.setLayout(new BoxLayout(timerContent, BoxLayout.Y_AXIS));
        timerContent.setOpaque(false);
        timerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerText.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerProgress.setAlignmentX(Component.CENTER_ALIGNMENT);
        timerContent.add(timerLabel);
        timerContent.add(timerText);
        timerContent.add(Box.createVerticalStrut(6));
        timerContent.add(timerProgress);
        
        timerPanel.add(timerContent, BorderLayout.CENTER);
        
        // Stats (center)
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 8, 0));  // ← CHANGED: 5 columns now
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 12, 15, 12)
        ));
        
        scoreLabel = createStatLabel("SCORE", "0", SUCCESS);
        accuracyLabel = createStatLabel("ACCURACY", "0%", LIGHT_BLUE);
        rankLabel = createStatLabel("RANK", "-", LIGHT_BLUE);  // ← CHANGED: Blue instead of warning yellow
        
        JButton leaderboardBtn = new JButton("LEADERBOARD");
        leaderboardBtn.setFont(new Font("Arial", Font.BOLD, 11));
        leaderboardBtn.setBackground(DARK_GRAY);
        leaderboardBtn.setForeground(Color.WHITE);
        leaderboardBtn.setFocusPainted(false);
        leaderboardBtn.setBorderPainted(false);
        leaderboardBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leaderboardBtn.addActionListener(e -> showLeaderboard());
        
        // ← ADDED: Sound toggle button
        soundToggleBtn = new JButton(soundManager.isSoundEnabled() ? "♪ SOUND ON" : "X SOUND OFF");
        soundToggleBtn.setFont(new Font("Arial", Font.BOLD, 11));
        soundToggleBtn.setBackground(soundManager.isSoundEnabled() ? SUCCESS : new Color(127, 140, 141));
        soundToggleBtn.setForeground(Color.WHITE);
        soundToggleBtn.setFocusPainted(false);
        soundToggleBtn.setBorderPainted(false);
        soundToggleBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        soundToggleBtn.addActionListener(e -> toggleSound());
        
        statsPanel.add(scoreLabel);
        statsPanel.add(accuracyLabel);
        statsPanel.add(rankLabel);
        statsPanel.add(leaderboardBtn);
        statsPanel.add(soundToggleBtn);  // ← ADDED: Add sound button to panel
        
        // Feedback (bottom)
        feedbackLabel = new JLabel("How many hearts do you see?", JLabel.CENTER);
        feedbackLabel.setFont(new Font("Arial", Font.BOLD, 15));
        feedbackLabel.setForeground(Color.WHITE);
        feedbackLabel.setOpaque(true);
        feedbackLabel.setBackground(DARK_GRAY);
        feedbackLabel.setBorder(new EmptyBorder(10, 15, 10, 15));
        feedbackLabel.setPreferredSize(new Dimension(1080, 42));
        
        topPanel.add(timerPanel, BorderLayout.WEST);
        topPanel.add(statsPanel, BorderLayout.CENTER);
        topPanel.add(feedbackLabel, BorderLayout.SOUTH);
        
        return topPanel;
    }
    
    private JLabel createStatLabel(String label, String value, Color valueColor) {
        String html = String.format(
            "<html><div style='text-align:center'>" +
            "<span style='font-size:10px; color:#7f8c8d'>%s</span><br>" +
            "<span style='font-size:24px; font-weight:bold; color:rgb(%d,%d,%d)'>%s</span>" +
            "</div></html>",
            label, valueColor.getRed(), valueColor.getGreen(), valueColor.getBlue(), value
        );
        return new JLabel(html, JLabel.CENTER);
    }
    
    private JPanel createBalancedGamePanel() {
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.setBackground(Color.WHITE);
        gamePanel.setBorder(new LineBorder(new Color(200, 200, 200), 2));
        gamePanel.setPreferredSize(new Dimension(1080, 420));
        
        imageLabel = new JLabel("Loading game...", JLabel.CENTER);
        imageLabel.setFont(new Font("Arial", Font.ITALIC, 16));
        imageLabel.setForeground(new Color(127, 140, 141));
        
        JScrollPane scrollPane = new JScrollPane(imageLabel);
        scrollPane.setBorder(null);
        gamePanel.add(scrollPane, BorderLayout.CENTER);
        
        return gamePanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 5, 8, 8));
        buttonPanel.setBackground(LIGHT_GRAY);
        buttonPanel.setPreferredSize(new Dimension(1080, 130));
        
        Color[] colors = {
            new Color(231, 76, 60),
            new Color(52, 152, 219),
            new Color(46, 204, 113),
            new Color(155, 89, 182),
            new Color(243, 156, 18),
            new Color(230, 126, 34),
            new Color(26, 188, 156),
            new Color(52, 73, 94),
            new Color(149, 165, 166),
            new Color(192, 57, 43)
        };
        
        numberButtons = new JButton[10];  // Store button references
        
        for (int i = 0; i < 10; i++) {
            JButton btn = createNumberButton(String.valueOf(i), colors[i]);
            final int number = i;
            btn.addActionListener(e -> handleAnswer(number));
            numberButtons[i] = btn;  // Save reference
            buttonPanel.add(btn);
        }
        
        return buttonPanel;
    }
    
    private JButton createNumberButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 28));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = color;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(original);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.darker());
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
        });
        
        return btn;
    }
    
    private long lastTickSecond = -1;  // Track when we last played tick sound
    
    private void startCountdownTimer() {
        lastTickSecond = -1;  // Reset tick tracker
        
        countdownTimer = new Timer(100, e -> {
            if (gameService.getSession() != null && gameService.getSession().isSessionActive()) {
                long remaining = gameService.getSession().getRemainingSeconds();
                timerLabel.setText(String.valueOf(remaining));
                timerProgress.setValue((int) remaining);
                
                // Play tick sound EVERY second (not just last 5)
                if (remaining > 0 && remaining != lastTickSecond) {
                    soundManager.playTickSound();
                    lastTickSecond = remaining;
                }
                
                JPanel timerPanel = (JPanel) timerLabel.getParent().getParent();
                if (remaining <= 5 && remaining > 0) {
                    timerPanel.setBackground(DANGER);
                    timerProgress.setForeground(Color.WHITE);
                } else if (remaining <= 10) {
                    timerPanel.setBackground(WARNING);
                    timerProgress.setForeground(Color.WHITE);
                } else {
                    timerPanel.setBackground(DARK_BLUE);
                    timerProgress.setForeground(SUCCESS);
                }
                
                if (remaining <= 0) {
                    System.out.println("⏰ Timer reached 0! Ending session...");
                    
                    // Stop timer immediately
                    if (countdownTimer != null && countdownTimer.isRunning()) {
                        countdownTimer.stop();
                    }
                    
                    // Disable all number buttons immediately
                    disableAllButtons();
                    
                    // Cancel any pending answer delay timer
                    if (answerDelayTimer != null && answerDelayTimer.isRunning()) {
                        System.out.println("❌ Cancelling pending answer timer...");
                        answerDelayTimer.stop();
                    }
                    
                    // Play sound
                    soundManager.playGameOverSound();
                    
                    // End session on EDT thread with delay to ensure sound plays
                    Timer endSessionTimer = new Timer(500, evt -> {
                        System.out.println("🎮 Calling endSession now...");
                        if (gameService.getSession() != null) {
                            gameService.endSession();
                        } else {
                            System.out.println("⚠️ Session is null, showing dialog manually");
                            showGameOverDialog();
                        }
                    });
                    endSessionTimer.setRepeats(false);
                    endSessionTimer.start();
                }
            }
        });
        countdownTimer.start();
    }
    
    private void handleAnswer(int answer) {
        // Check if session is still active before processing
        if (gameService.getSession() == null || !gameService.getSession().isSessionActive()) {
            System.out.println("⚠️ Session inactive, ignoring button click");
            return; // Don't process if game is over
        }
        
        System.out.println("🎯 Button " + answer + " clicked");
        gameService.submitAnswer(answer);
        
        // Cancel previous timer if exists
        if (answerDelayTimer != null && answerDelayTimer.isRunning()) {
            answerDelayTimer.stop();
        }
        
        // Always load next game after a short delay (whether correct or wrong)
        answerDelayTimer = new Timer(800, e -> {
            // Double-check session is still active before loading next game
            if (gameService.getSession() != null && gameService.getSession().isSessionActive()) {
                System.out.println("⏭️ Loading next game...");
                gameService.loadNextGame();
            } else {
                System.out.println("⏰ Session ended, not loading next game");
            }
        });
        answerDelayTimer.setRepeats(false);
        answerDelayTimer.start();
    }
    
    private void showLeaderboard() {
        LeaderboardWindow leaderboard = new LeaderboardWindow(this);
        leaderboard.setVisible(true);
    }
    

    // Disable all number buttons when game ends
    private void disableAllButtons() {
        if (numberButtons != null) {
            for (JButton btn : numberButtons) {
                btn.setEnabled(false);
            }
        }
    }
    
    // ← ADDED: Toggle sound method
    private void toggleSound() {
        soundManager.toggleSound();
        soundToggleBtn.setText(soundManager.isSoundEnabled() ? "♪ SOUND ON" : "X SOUND OFF");
        soundToggleBtn.setBackground(soundManager.isSoundEnabled() ? SUCCESS : new Color(127, 140, 141));
        
        // Play a test beep when enabling
        if (soundManager.isSoundEnabled()) {
            soundManager.playCorrectSound();
        }
    }
    
    private void showGameOverDialog() {
        // Extra safety check
        if (!isShowingGameOver) {
            System.out.println("⚠️ showGameOverDialog called but flag not set, aborting");
            return;
        }
        
        if (countdownTimer != null) {
            countdownTimer.stop();
        }
        
        if (answerDelayTimer != null && answerDelayTimer.isRunning()) {
            answerDelayTimer.stop();
        }
        
        Player player = gameService.getSession().getPlayer();
        String username = player.getUsername();
        double sessionAccuracy = gameService.getSession().getSessionAccuracy();
        
        PlayerDataService.getInstance().savePlayer(player);
        PlayerDataService.getInstance().saveSession(
            username,
            gameService.getSession().getSessionScore(),
            sessionAccuracy,
            gameService.getSession().getSessionAttempts()
        );
        
        // Check if player passed or failed (BASED ON SESSION ACCURACY ONLY!)
        boolean passed = sessionAccuracy >= 50.0;
        
        String title;
        String message;
        int messageType;
        
        if (passed) {
            // PASSED - Session accuracy >= 50%
            soundManager.playAchievementSound();
            title = "🎉 CONGRATULATIONS!";
            messageType = JOptionPane.INFORMATION_MESSAGE;
            message = String.format(
                "YOU PASSED! Well done!\n\n" +
                "=== THIS SESSION ===\n" +
                "Score: %d\n" +
                "Accuracy: %.1f%% ✅ (50%%+ required)\n" +
                "Games: %d\n\n" +
                "=== TOTAL PROGRESS ===\n" +
                "Overall Score: %d\n" +
                "Overall Accuracy: %.1f%%\n" +
                "Total Games: %d\n\n" +
                "Play another round?",
                gameService.getSession().getSessionScore(),
                sessionAccuracy,
                gameService.getSession().getSessionAttempts(),
                player.getScore(),
                player.getAccuracy(),
                player.getTotalAttempts()
            );
        } else {
            // FAILED - Session accuracy < 50%
            soundManager.playGameOverSound();
            title = "💀 GAME OVER!";
            messageType = JOptionPane.ERROR_MESSAGE;
            message = String.format(
                "YOU FAILED! Session accuracy too low!\n\n" +
                "=== THIS SESSION ===\n" +
                "Score: %d\n" +
                "Accuracy: %.1f%% ❌ (Need 50%%+)\n" +
                "Games: %d\n\n" +
                "=== TOTAL PROGRESS ===\n" +
                "Overall Score: %d\n" +
                "Overall Accuracy: %.1f%%\n" +
                "Total Games: %d\n\n" +
                "Try again?",
                gameService.getSession().getSessionScore(),
                sessionAccuracy,
                gameService.getSession().getSessionAttempts(),
                player.getScore(),
                player.getAccuracy(),
                player.getTotalAttempts()
            );
        }
        
        int choice = JOptionPane.showConfirmDialog(
            this,
            message,
            title,
            JOptionPane.YES_NO_OPTION,
            messageType
        );
        
        GameEventDispatcher.getInstance().removeListener(this);
        isShowingGameOver = false;  // ← ADDED: Reset flag for next game
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            // Load existing player data (don't create new one!)
            Player existingPlayer = PlayerDataService.getInstance().loadPlayer(username);
            if (existingPlayer != null) {
                GameWindow newGame = new GameWindow(existingPlayer);
                newGame.setVisible(true);
            } else {
                // Fallback: if load fails, use current player
                GameWindow newGame = new GameWindow(player);
                newGame.setVisible(true);
            }
        } else {
            dispose();
            LoginWindow login = new LoginWindow();
            login.setVisible(true);
        }
    }
    
    @Override
    public void onAnswerSubmitted(GameEvent.AnswerSubmitted event) {
        SwingUtilities.invokeLater(() -> {
            Player player = gameService.getSession().getPlayer();
            
            if (event.correct) {
                feedbackLabel.setText("✓ CORRECT! Well done!");
                feedbackLabel.setBackground(SUCCESS);
                soundManager.playCorrectSound();  // ← ADDED: Correct answer sound
            } else {
                feedbackLabel.setText("✗ WRONG! Try again!");
                feedbackLabel.setBackground(DANGER);
                soundManager.playWrongSound();  // ← ADDED: Wrong answer sound
            }
            
            scoreLabel.setText(createStatLabel("SCORE", String.valueOf(player.getScore()), SUCCESS).getText());
            accuracyLabel.setText(createStatLabel("ACCURACY", String.format("%.1f%%", player.getAccuracy()), LIGHT_BLUE).getText());
        });
    }
    
    @Override
    public void onGameLoaded(GameEvent.GameLoaded event) {
        SwingUtilities.invokeLater(() -> {
            if (event.game != null) {
                ImageIcon icon = new ImageIcon(event.game.getImage());
                imageLabel.setIcon(icon);
                imageLabel.setText("");
                feedbackLabel.setText("How many hearts do you see?");
                feedbackLabel.setBackground(DARK_GRAY);
            } else {
                // Show error if game failed to load
                imageLabel.setText("Failed to load game. Check internet connection.");
                feedbackLabel.setText("Loading next game...");
            }
        });
    }
    
    @Override
    public void onPlayerLoggedIn(GameEvent.PlayerLoggedIn event) {
        // Reset display to 0 when new session starts
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText(createStatLabel("SCORE", "0", SUCCESS).getText());
            accuracyLabel.setText(createStatLabel("ACCURACY", "0%", LIGHT_BLUE).getText());
        });
    }
    
    @Override
    public void onScoreUpdated(GameEvent.ScoreUpdated event) {
        SwingUtilities.invokeLater(() -> {
            LeaderboardService lb = LeaderboardService.getInstance();
            int rank = lb.getPlayerRank(event.player.getUsername());
            int total = lb.getTotalPlayers();
            rankLabel.setText(createStatLabel("RANK", rank + "/" + total, LIGHT_BLUE).getText());  // ← CHANGED: Blue color
        });
    }
    
    @Override
    public void onSessionEnded(GameEvent.SessionEnded event) {
        System.out.println("📢 SessionEnded event received!");
        
        // Prevent multiple dialogs
        if (isShowingGameOver) {
            System.out.println("⚠️ Game over dialog already showing, ignoring duplicate event");
            return;
        }
        
        isShowingGameOver = true;
        
        SwingUtilities.invokeLater(() -> {
            System.out.println("💬 Showing game over dialog...");
            showGameOverDialog();
        });
    }
}