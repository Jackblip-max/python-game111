package com.perisic.heart.gui;

import com.perisic.heart.model.Player;
import com.perisic.heart.model.GameSession;
import com.perisic.heart.service.*;
import com.perisic.heart.events.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;

public class GameWindow extends JFrame implements GameEventDispatcher.GameEventListener {
    
    private GameService gameService;
    private SoundManager soundManager;
    private JLabel imageLabel;
    private JLabel scoreLabel;
    private JLabel accuracyLabel;
    private JLabel feedbackLabel;
    private JLabel rankLabel;
    private JLabel timerLabel;
    private JProgressBar timerProgress;
    private Timer countdownTimer;
    private Timer answerDelayTimer;
    private JButton[] numberButtons;
    private boolean isShowingGameOver = false;
    private JButton soundToggleBtn;
    
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
        soundManager = SoundManager.getInstance();
        
        // IMPORTANT: Register listener BEFORE starting session!
        GameEventDispatcher.getInstance().addListener(this);
        
        setupClassicGameWindow(player);
        
        // Manually reset display to 0 before starting session
        scoreLabel.setText(createStatLabel("SCORE", "0", SUCCESS).getText());
        accuracyLabel.setText(createStatLabel("ACCURACY", "0.0%", LIGHT_BLUE).getText());
        
        // Now start the session (this will fire events)
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
        
        JPanel topPanel = createCompactTopPanel();
        JPanel centerPanel = createBalancedGamePanel();
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
        JPanel statsPanel = new JPanel(new GridLayout(1, 5, 8, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new CompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 12, 15, 12)
        ));
        
        scoreLabel = createStatLabel("SCORE", "0", SUCCESS);
        accuracyLabel = createStatLabel("ACCURACY", "0%", LIGHT_BLUE);
        rankLabel = createStatLabel("RANK", "-", LIGHT_BLUE);
        
        JButton leaderboardBtn = new JButton("LEADERBOARD");
        leaderboardBtn.setFont(new Font("Arial", Font.BOLD, 11));
        leaderboardBtn.setBackground(DARK_GRAY);
        leaderboardBtn.setForeground(Color.WHITE);
        leaderboardBtn.setFocusPainted(false);
        leaderboardBtn.setBorderPainted(false);
        leaderboardBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leaderboardBtn.addActionListener(e -> showLeaderboard());
        
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
        statsPanel.add(soundToggleBtn);
        
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
            new Color(231, 76, 60), new Color(52, 152, 219), new Color(46, 204, 113),
            new Color(155, 89, 182), new Color(243, 156, 18), new Color(230, 126, 34),
            new Color(26, 188, 156), new Color(52, 73, 94), new Color(149, 165, 166),
            new Color(192, 57, 43)
        };
        
        numberButtons = new JButton[10];
        
        for (int i = 0; i < 10; i++) {
            JButton btn = createNumberButton(String.valueOf(i), colors[i]);
            final int number = i;
            btn.addActionListener(e -> handleAnswer(number));
            numberButtons[i] = btn;
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
    
    private long lastTickSecond = -1;
    
    private void startCountdownTimer() {
        lastTickSecond = -1;
        
        countdownTimer = new Timer(100, e -> {
            if (gameService.getSession() != null && gameService.getSession().isSessionActive()) {
                long remaining = gameService.getSession().getRemainingSeconds();
                timerLabel.setText(String.valueOf(remaining));
                timerProgress.setValue((int) remaining);
                
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
                    
                    if (countdownTimer != null && countdownTimer.isRunning()) {
                        countdownTimer.stop();
                    }
                    
                    disableAllButtons();
                    
                    if (answerDelayTimer != null && answerDelayTimer.isRunning()) {
                        System.out.println("❌ Cancelling pending answer timer...");
                        answerDelayTimer.stop();
                    }
                    
                    soundManager.playGameOverSound();
                    
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
        if (gameService.getSession() == null || !gameService.getSession().isSessionActive()) {
            System.out.println("⚠️ Session inactive, ignoring button click");
            return;
        }
        
        System.out.println("🎯 Button " + answer + " clicked");
        gameService.submitAnswer(answer);
        
        if (answerDelayTimer != null && answerDelayTimer.isRunning()) {
            answerDelayTimer.stop();
        }
        
        answerDelayTimer = new Timer(800, e -> {
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
    
    private void disableAllButtons() {
        if (numberButtons != null) {
            for (JButton btn : numberButtons) {
                btn.setEnabled(false);
            }
        }
    }
    
    private void toggleSound() {
        soundManager.toggleSound();
        soundToggleBtn.setText(soundManager.isSoundEnabled() ? "♪ SOUND ON" : "X SOUND OFF");
        soundToggleBtn.setBackground(soundManager.isSoundEnabled() ? SUCCESS : new Color(127, 140, 141));
        
        if (soundManager.isSoundEnabled()) {
            soundManager.playCorrectSound();
        }
    }
    
    private void showGameOverDialog() {
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
        
        boolean passed = sessionAccuracy >= 50.0;
        
        String title;
        String message;
        int messageType;
        
        if (passed) {
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
        isShowingGameOver = false;
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            Player existingPlayer = PlayerDataService.getInstance().loadPlayer(username);
            if (existingPlayer != null) {
                GameWindow newGame = new GameWindow(existingPlayer);
                newGame.setVisible(true);
            } else {
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
        System.out.println("📝 Answer submitted: " + event.answer + " - " + (event.correct ? "CORRECT" : "WRONG"));
        
        SwingUtilities.invokeLater(() -> {
            GameSession session = gameService.getSession();
            
            if (session == null) {
                System.out.println("⚠️ WARNING: Session is null!");
                return;
            }
            
            System.out.println("📊 Session stats - Score: " + session.getSessionScore() + 
                             ", Accuracy: " + session.getSessionAccuracy() + 
                             ", Attempts: " + session.getSessionAttempts());
            
            if (event.correct) {
                feedbackLabel.setText("✓ CORRECT! Well done!");
                feedbackLabel.setBackground(SUCCESS);
                soundManager.playCorrectSound();
            } else {
                feedbackLabel.setText("✗ WRONG! Try again!");
                feedbackLabel.setBackground(DANGER);
                soundManager.playWrongSound();
            }
            
            scoreLabel.setText(createStatLabel("SCORE", 
                String.valueOf(session.getSessionScore()), SUCCESS).getText());
            accuracyLabel.setText(createStatLabel("ACCURACY", 
                String.format("%.1f%%", session.getSessionAccuracy()), LIGHT_BLUE).getText());
                
            System.out.println("✅ Display updated");
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
                imageLabel.setText("Failed to load game. Check internet connection.");
                feedbackLabel.setText("Loading next game...");
            }
        });
    }
    
    @Override
    public void onPlayerLoggedIn(GameEvent.PlayerLoggedIn event) {
        System.out.println("🎮 Player logged in event - Resetting display to 0");
        SwingUtilities.invokeLater(() -> {
            scoreLabel.setText(createStatLabel("SCORE", "0", SUCCESS).getText());
            accuracyLabel.setText(createStatLabel("ACCURACY", "0.0%", LIGHT_BLUE).getText());
            System.out.println("✅ Display reset complete");
        });
    }
    
    @Override
    public void onScoreUpdated(GameEvent.ScoreUpdated event) {
        System.out.println("📊 Score updated event received");
        SwingUtilities.invokeLater(() -> {
            LeaderboardService lb = LeaderboardService.getInstance();
            int rank = lb.getPlayerRank(event.player.getUsername());
            int total = lb.getTotalPlayers();
            rankLabel.setText(createStatLabel("RANK", rank + "/" + total, LIGHT_BLUE).getText());
            System.out.println("✅ Rank updated: " + rank + "/" + total);
        });
    }
    
    @Override
    public void onSessionEnded(GameEvent.SessionEnded event) {
        System.out.println("📢 SessionEnded event received!");
        
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
