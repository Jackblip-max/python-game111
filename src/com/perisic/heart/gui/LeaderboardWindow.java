package com.perisic.heart.gui;

import com.perisic.heart.model.LeaderboardEntry;
import com.perisic.heart.service.LeaderboardService;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class LeaderboardWindow extends JFrame {
    
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private LeaderboardService leaderboardService;
    
    // Modern colors
    private static final Color DARK_BLUE = new Color(31, 58, 96);
    private static final Color SUCCESS = new Color(39, 174, 96);
    private static final Color GOLD = new Color(255, 193, 7);
    private static final Color SILVER = new Color(192, 192, 192);
    private static final Color BRONZE = new Color(205, 127, 50);
    private static final Color LIGHT_GRAY = new Color(236, 240, 241);
    private static final Color DARK_GRAY = new Color(52, 73, 94);
    
    public LeaderboardWindow(JFrame parent) {
        leaderboardService = LeaderboardService.getInstance();
        setupModernWindow(parent);
        loadLeaderboard();
    }
    
    private void setupModernWindow(JFrame parent) {
        setTitle("Leaderboard - Top Players");
        setSize(750, 550);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(LIGHT_GRAY);
        
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(LIGHT_GRAY);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = createHeader();
        
        // Table
        JPanel tablePanel = createTablePanel();
        
        // Buttons
        JPanel buttonPanel = createButtonPanel();
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(tablePanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeader() {
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(DARK_BLUE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("<html>TOP PLAYERS</html>");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Hall of Fame - Best Players");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(255, 255, 255, 200));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(8));
        headerPanel.add(subtitleLabel);
        
        return headerPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(new LineBorder(new Color(200, 200, 200), 1));
        
        String[] columnNames = {"Rank", "Player", "Score", "Accuracy", "Games"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 2 || column == 4) {
                    return Integer.class;
                }
                return String.class;
            }
        };
        
        leaderboardTable = new JTable(tableModel);
        
        // Modern table styling
        leaderboardTable.setFont(new Font("Arial", Font.PLAIN, 14));
        leaderboardTable.setRowHeight(40);
        leaderboardTable.setShowGrid(false);
        leaderboardTable.setIntercellSpacing(new Dimension(0, 0));
        leaderboardTable.setSelectionBackground(new Color(52, 152, 219, 50));
        leaderboardTable.setSelectionForeground(DARK_GRAY);
        
        // Header styling
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 13));
        header.setBackground(DARK_GRAY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Custom renderer for rank column (medals!)
        leaderboardTable.getColumnModel().getColumn(0).setCellRenderer(new RankCellRenderer());
        
        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 5; i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(
                i == 0 ? new RankCellRenderer() : centerRenderer
            );
        }
        
        // Column widths
        leaderboardTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Rank
        leaderboardTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Player
        leaderboardTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Score
        leaderboardTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Accuracy
        leaderboardTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Games
        
        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    // Custom renderer for rank with medals
    class RankCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            setHorizontalAlignment(CENTER);
            setFont(new Font("Arial", Font.BOLD, 16));
            
            if (value != null) {
                int rank = (Integer) value;
                
                // Add medal icons for top 3
                if (rank == 1) {
                    setText("<html><span style='font-size:18px; color:rgb(255,193,7)'>&#9733;</span> " + rank + "</html>");
                    setForeground(GOLD);
                } else if (rank == 2) {
                    setText("<html><span style='font-size:18px; color:rgb(192,192,192)'>&#9733;</span> " + rank + "</html>");
                    setForeground(SILVER);
                } else if (rank == 3) {
                    setText("<html><span style='font-size:18px; color:rgb(205,127,50)'>&#9733;</span> " + rank + "</html>");
                    setForeground(BRONZE);
                } else {
                    setText(String.valueOf(rank));
                    setForeground(DARK_GRAY);
                }
            }
            
            // Alternating row colors
            if (!isSelected) {
                setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249));
            }
            
            return c;
        }
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(LIGHT_GRAY);
        
        JButton refreshBtn = createModernButton("REFRESH", SUCCESS);
        JButton closeBtn = createModernButton("CLOSE", DARK_GRAY);
        
        refreshBtn.addActionListener(e -> {
            loadLeaderboard();
            JOptionPane.showMessageDialog(this, "Leaderboard refreshed!", "Success", JOptionPane.INFORMATION_MESSAGE);
        });
        
        closeBtn.addActionListener(e -> dispose());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(closeBtn);
        
        return buttonPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(150, 40));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            Color original = bgColor;
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(original);
            }
        });
        
        return btn;
    }
    
    private void loadLeaderboard() {
        tableModel.setRowCount(0);
        
        List<LeaderboardEntry> topPlayers = leaderboardService.getTopPlayers(50);
        
        if (topPlayers.isEmpty()) {
            // Show empty state
            Object[] emptyRow = {"", "No players yet", "", "Be the first!", ""};
            tableModel.addRow(emptyRow);
        } else {
            int rank = 1;
            for (LeaderboardEntry entry : topPlayers) {
                Object[] row = {
                    rank++,
                    entry.getUsername(),
                    entry.getScore(),
                    String.format("%.1f%%", entry.getAccuracy()),
                    entry.getGamesPlayed()
                };
                tableModel.addRow(row);
            }
        }
    }
}
