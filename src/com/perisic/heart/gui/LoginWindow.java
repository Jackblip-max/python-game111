package com.perisic.heart.gui;

import com.perisic.heart.model.Player;
import com.perisic.heart.service.*;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Login window with animated floating hearts background
 */
public class LoginWindow extends JFrame {
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private AuthService authService;
    private SessionManager sessionManager;
    private AnimatedBackgroundPanel backgroundPanel;
    
    public LoginWindow() {
        authService = AuthService.getInstance();
        sessionManager = SessionManager.getInstance();
        
        setupWindow();
        checkSavedSession();
    }
    
    private void setupWindow() {
        setTitle("Heart Game - Login");
        setSize(500, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create animated background panel
        backgroundPanel = new AnimatedBackgroundPanel();
        setContentPane(backgroundPanel);
        backgroundPanel.setLayout(null);
        
        // Create login form panel (on top of animated background)
        JPanel formPanel = createLoginForm();
        formPanel.setBounds(50, 100, 400, 450);
        backgroundPanel.add(formPanel);
        
        // Start animation
        backgroundPanel.startAnimation();
    }
    
    private JPanel createLoginForm() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(255, 255, 255, 240)); // Semi-transparent white
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(52, 152, 219), 3),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        
        // Title
        JLabel title = new JLabel("HEART GAME");
        title.setFont(new Font("Arial", Font.BOLD, 32));
        title.setForeground(new Color(231, 76, 60));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(title);
        
        JLabel subtitle = new JLabel("Count Hearts • Beat the Clock");
        subtitle.setFont(new Font("Arial", Font.ITALIC, 14));
        subtitle.setForeground(new Color(127, 140, 141));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(subtitle);
        panel.add(Box.createVerticalStrut(30));
        
        // Username
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Arial", Font.BOLD, 12));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(userLabel);
        panel.add(Box.createVerticalStrut(5));
        
        usernameField = new JTextField();
        usernameField.setMaximumSize(new Dimension(320, 35));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(usernameField);
        panel.add(Box.createVerticalStrut(15));
        
        // Password
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Arial", Font.BOLD, 12));
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passLabel);
        panel.add(Box.createVerticalStrut(5));
        
        passwordField = new JPasswordField();
        passwordField.setMaximumSize(new Dimension(320, 35));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(passwordField);
        panel.add(Box.createVerticalStrut(15));
        
        // Remember me
        rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(new Font("Arial", Font.PLAIN, 12));
        rememberMeBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        rememberMeBox.setOpaque(false);
        panel.add(rememberMeBox);
        panel.add(Box.createVerticalStrut(20));
        
        // Buttons
        JButton loginBtn = createStyledButton(" LOGIN", new Color(52, 152, 219));
        JButton registerBtn = createStyledButton(" REGISTER", new Color(46, 204, 113));
        JButton leaderboardBtn = createStyledButton(" VIEW LEADERBOARD", new Color(155, 89, 182));
        
        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> handleRegister());
        leaderboardBtn.addActionListener(e -> showLeaderboard());
        
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        leaderboardBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(loginBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(registerBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(leaderboardBtn);
        
        // Enter key support
        passwordField.addActionListener(e -> handleLogin());
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMaximumSize(new Dimension(320, 40));
        btn.setPreferredSize(new Dimension(320, 40));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = color;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(original);
            }
        });
        
        return btn;
    }
    
    private void checkSavedSession() {
        String savedUser = sessionManager.loadSession();
        if (savedUser != null && !savedUser.isEmpty()) {
            int choice = JOptionPane.showConfirmDialog(
                this,
                "Continue as " + savedUser + "?",
                "Welcome Back",
                JOptionPane.YES_NO_OPTION
            );
            if (choice == JOptionPane.YES_OPTION) {
                startGame(new Player(savedUser));
            } else {
                sessionManager.clearSession();
            }
        }
    }
    
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }
        
        Player player = authService.login(username, password);
        
        if (player != null) {
            if (rememberMeBox.isSelected()) {
                sessionManager.saveSession(username);
            }
            backgroundPanel.stopAnimation();
            startGame(player);
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Invalid username or password",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields");
            return;
        }
        
        if (authService.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "Registration successful! Please login.");
            passwordField.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Username already exists");
        }
    }
    
    private void showLeaderboard() {
        LeaderboardWindow leaderboard = new LeaderboardWindow(this);
        leaderboard.setVisible(true);
    }
    
    private void startGame(Player player) {
        GameWindow gameWindow = new GameWindow(player);
        gameWindow.setVisible(true);
        dispose();
    }
    
    // ========================================================================
    // ANIMATED BACKGROUND PANEL - Floating Hearts!
    // ========================================================================
    
    class AnimatedBackgroundPanel extends JPanel {
        private List<FloatingHeart> hearts;
        private Timer animationTimer;
        private Random random;
        
        public AnimatedBackgroundPanel() {
            hearts = new ArrayList<>();
            random = new Random();
            
            // Create 15 floating hearts
            for (int i = 0; i < 15; i++) {
                hearts.add(new FloatingHeart());
            }
        }
        
        public void startAnimation() {
            animationTimer = new Timer(30, e -> {
                // Update all hearts
                for (FloatingHeart heart : hearts) {
                    heart.update();
                }
                repaint();
            });
            animationTimer.start();
        }
        
        public void stopAnimation() {
            if (animationTimer != null) {
                animationTimer.stop();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, new Color(255, 182, 193), // Light pink
                0, getHeight(), new Color(255, 228, 225) // Lighter pink
            );
            g2d.setPaint(gradient);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Draw all floating hearts
            for (FloatingHeart heart : hearts) {
                heart.draw(g2d);
            }
        }
        
        // Individual floating heart
        class FloatingHeart {
            private float x, y;
            private float speedY;
            private float speedX;
            private int size;
            private Color color;
            private float rotation;
            private float rotationSpeed;
            
            public FloatingHeart() {
                reset();
            }
            
            private void reset() {
                x = random.nextInt(500);
                y = random.nextInt(600) + 600; // Start below screen
                speedY = -0.5f - random.nextFloat() * 1.5f; // Float upward
                speedX = -0.5f + random.nextFloat(); // Drift sideways
                size = 20 + random.nextInt(30);
                
                // Random pink/red colors
                int r = 200 + random.nextInt(56);
                int g = 50 + random.nextInt(100);
                int b = 100 + random.nextInt(100);
                int alpha = 100 + random.nextInt(100);
                color = new Color(r, g, b, alpha);
                
                rotation = random.nextFloat() * 360;
                rotationSpeed = -1 + random.nextFloat() * 2;
            }
            
            public void update() {
                y += speedY;
                x += speedX;
                rotation += rotationSpeed;
                
                // Reset when it goes off top
                if (y < -size) {
                    reset();
                }
                
                // Wrap around sides
                if (x < -size) x = 500;
                if (x > 500 + size) x = -size;
            }
            
            public void draw(Graphics2D g2d) {
                g2d.setColor(color);
                
                // Save transform
                java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
                
                // Rotate around heart center
                g2d.rotate(Math.toRadians(rotation), x, y);
                
                // Draw heart shape
                Path2D.Float heart = new Path2D.Float();
                float halfSize = size / 2f;
                
                heart.moveTo(x, y + halfSize * 0.3f);
                heart.curveTo(
                    x - halfSize, y - halfSize * 0.5f,
                    x - halfSize, y + halfSize * 0.3f,
                    x, y + halfSize
                );
                heart.curveTo(
                    x + halfSize, y + halfSize * 0.3f,
                    x + halfSize, y - halfSize * 0.5f,
                    x, y + halfSize * 0.3f
                );
                heart.closePath();
                
                g2d.fill(heart);
                
                // Restore transform
                g2d.setTransform(oldTransform);
            }
        }
    }
}
