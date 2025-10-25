
package calculate;

import java.io.*; 
import java.net.*; 
import javax.swing.*; 
import java.awt.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import javax.sound.sampled.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;



public class Client {
    private static boolean isInGame = false;
    private static boolean scoreShown = false;
    private static PrintWriter out;
    private static String playerName;
    private static JFrame frame;
    private static DefaultListModel<String> playerListModel = new DefaultListModel<>();
    private static DefaultListModel<String> waitingListModel = new DefaultListModel<>();
    private static JList<String> playerList;
    private static JList<String> waitingList;
    private static JButton playButton;
    private static JButton connectButton;
    private static JLabel timerLabel;
    private static JPanel gamePlayersLabel;
    private static String currentQuestionText = "";
    private static JLabel questionLabel;
    private static Clip backgroundClip;

    private static Map<String, Integer> playerScores = new HashMap<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Client::showLoginScreen);
    }

    private static void showLoginScreen() {
        frame = new JFrame("Math Game - Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setResizable(true);                      


        frame.setLayout(new BorderLayout());
        frame.add(createLogoBanner(), BorderLayout.NORTH);

    
        JPanel backgroundPanel = new JPanel(new BorderLayout());
        backgroundPanel.setBackground(new Color(255, 253, 208));

    
        try {
            ImageIcon gifLeft = new ImageIcon(Client.class.getResource("stars.gif"));
            JLabel gifLeftLabel = new JLabel(gifLeft);
            gifLeftLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            backgroundPanel.add(gifLeftLabel, BorderLayout.WEST);
        } catch (Exception e) {
            System.out.println("Left GIF load failed.");
        }

    
        try {
            ImageIcon gifRight = new ImageIcon(Client.class.getResource("stars.gif"));
            JLabel gifRightLabel = new JLabel(gifRight);
            gifRightLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            backgroundPanel.add(gifRightLabel, BorderLayout.EAST);
        } catch (Exception e) {
            System.out.println("Right GIF load failed.");
        }

        JPanel centerPanelWithGIF = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    ImageIcon gifCenter = new ImageIcon(Client.class.getResource("backgroundstars.gif"));
                    Image img = gifCenter.getImage();
                    g.drawImage(img, (getWidth() - img.getWidth(null)) / 2,
                                     (getHeight() - img.getHeight(null)) / 2, null);
                } catch (Exception e) {
                    System.out.println("Center GIF load failed.");
                }
            }
        };
        centerPanelWithGIF.setOpaque(false);
        centerPanelWithGIF.setBackground(new Color(255, 253, 208));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        try {
            ImageIcon logoIcon = new ImageIcon(Client.class.getResource("logomath.png"));
            Image originalImg = logoIcon.getImage();
            int width = 320;
            int height = 320;

            BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImg, 0, 0, width, height, null);
            g2d.dispose();

            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("Logo failed to load: " + e.getMessage());
        }

        mainPanel.add(Box.createVerticalStrut(10));

        JLabel nameLabel = new JLabel("Enter Name:");
        nameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(nameLabel);

        JTextField nameField = new JTextField(12);
        nameField.setMaximumSize(new Dimension(200, 30));
        nameField.setFont(new Font("Serif", Font.PLAIN, 14));
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(nameField);

        mainPanel.add(Box.createVerticalStrut(10));

        connectButton = new JButton("Connect");
        styleButton(connectButton);
        connectButton.setBackground(new Color(204, 0, 0)); // red
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(connectButton);

        centerPanelWithGIF.add(mainPanel);
        backgroundPanel.add(centerPanelWithGIF, BorderLayout.CENTER);


        playBackgroundMusic();

        frame.add(backgroundPanel, BorderLayout.CENTER);
        frame.setVisible(true);

        connectButton.addActionListener(e -> {
            playerName = nameField.getText().trim();
            if (!playerName.isEmpty()) {
                new Thread(Client::connectToServer).start();
            }
        });
    }




    private static void showGameScreen() {
        scoreShown = false;

        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Connected Players");
        //frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 253, 208)); 

        frame.add(createLogoBanner(), BorderLayout.NORTH);

        JLabel titleLabel = new JLabel("The Connected Players", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        playerList = new JList<>(playerListModel);
        playerList.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane scrollPane = new JScrollPane(playerList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        playButton = new JButton("Play");
        styleButton(playButton);
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(255, 253, 208)); // match background
        centerPanel.add(titleLabel);
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(playButton);

        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new Color(204, 0, 0));
        bottomBorder.setPreferredSize(new Dimension(frame.getWidth(), 10));
        frame.add(bottomBorder, BorderLayout.SOUTH);

        frame.setVisible(true);

        playButton.addActionListener(e -> {
            if (out != null) {
                out.println("play");
            }
        });
    }


    private static void showWaitingRoom() {
        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Waiting Room");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 253, 208)); 

        JPanel logoPanel = createLogoBanner();

        JLabel titleLabel = new JLabel("Waiting Room", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 18));

        timerLabel = new JLabel("Time: ", SwingConstants.RIGHT);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 14));
        timerLabel.setForeground(Color.RED);

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(255, 253, 208));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        titlePanel.add(timerLabel, BorderLayout.EAST);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(255, 253, 208));
        topContainer.add(logoPanel);
        topContainer.add(titlePanel);

        frame.add(topContainer, BorderLayout.NORTH);

        waitingList = new JList<>(waitingListModel);
        waitingList.setFont(new Font("Serif", Font.PLAIN, 14));
        JScrollPane waitingScrollPane = new JScrollPane(waitingList);
        waitingScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(255, 253, 208));
        centerPanel.add(waitingScrollPane, BorderLayout.CENTER);

        JButton startGameButton = new JButton("Start Game");
        styleButton(startGameButton);
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startGameButton.setPreferredSize(new Dimension(150, 35));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(255, 253, 208));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(startGameButton);

        centerPanel.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new Color(204, 0, 0));
        bottomBorder.setPreferredSize(new Dimension(frame.getWidth(), 10));
        frame.add(bottomBorder, BorderLayout.SOUTH);
        frame.setVisible(true);

        startGameButton.addActionListener(e -> {
            if (out != null && waitingListModel.size() >= 2) {
                out.println("MANUAL_START");
            }
        });
    }



    private static void showGame() {
        if (!isInGame) {
            return; 
        }

        frame.getContentPane().removeAll();
        frame.setTitle("Math Game - Question Time");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 253, 208)); // light yellow

        JPanel logoPanel = createLogoBanner();
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255, 253, 208));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gamePlayersLabel = new JPanel();
        gamePlayersLabel.setLayout(new BoxLayout(gamePlayersLabel, BoxLayout.Y_AXIS));
        gamePlayersLabel.setBackground(new Color(220, 235, 245));
        updateGamePlayersLabel();

        JScrollPane playerPane = new JScrollPane(gamePlayersLabel);
        topPanel.add(playerPane, BorderLayout.WEST);

        JButton leaveButton = new JButton("Leave");
        styleButton(leaveButton);
        leaveButton.setBackground(new Color(204, 0, 0));
        topPanel.add(leaveButton, BorderLayout.EAST);

        timerLabel = new JLabel("Time: 120s", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 14));
        timerLabel.setForeground(Color.RED);
        topPanel.add(timerLabel, BorderLayout.CENTER);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(255, 253, 208)); // light yellow behind it
        topContainer.add(logoPanel);
        topContainer.add(topPanel);

        frame.add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(50, 100, 50, 100));

        questionLabel = new JLabel(currentQuestionText, SwingConstants.CENTER);
        questionLabel.setFont(new Font("Serif", Font.BOLD, 20));
        questionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea answerArea = new JTextArea(2, 10);
        answerArea.setFont(new Font("Serif", Font.PLAIN, 16));
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(answerArea);
        scrollPane.setMaximumSize(new Dimension(150, 50));
        scrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton submitButton = new JButton("Submit");
        styleButton(submitButton);
        submitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(questionLabel);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(scrollPane);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(submitButton);

        frame.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new Color(204, 0, 0));
        bottomBorder.setPreferredSize(new Dimension(frame.getWidth(), 10));
        frame.add(bottomBorder, BorderLayout.SOUTH);

        frame.setVisible(true);

        leaveButton.addActionListener(e -> {
            if (out != null) {
                out.println("LEAVE");
            }
            playerScores.remove(playerName);
            updateGamePlayersLabel();
            frame.dispose();
            System.exit(0);
        });

        submitButton.addActionListener(e -> {
            String answer = answerArea.getText().trim();
            if (!answer.isEmpty() && out != null) {
                out.println("ANSWER:" + answer);
                answerArea.setText("");
            }
        });
    }

    private static void updateGamePlayersLabel() {
        if (gamePlayersLabel == null) {
            return;
        }
        
        gamePlayersLabel.removeAll();
        for (String name : playerScores.keySet()) {
            JLabel label = new JLabel(name + " : " + playerScores.get(name));
            label.setFont(new Font("Serif", Font.PLAIN, 14));
            gamePlayersLabel.add(label);
        }
        gamePlayersLabel.revalidate();
        gamePlayersLabel.repaint();
    }

    private static void processServerMessage(String message) {
        if (message.contains("Players connected:")) {
            playerListModel.clear();
            for (String name : message.replace("Players connected: ", "").split(", ")) {
                if (!name.trim().isEmpty()) {
                    playerListModel.addElement(name);
                }
            }
        } else if (message.equals("WAITING_ROOM")) {
            SwingUtilities.invokeLater(Client::showWaitingRoom);
        } else if (message.startsWith("Waiting Room:")) {
            waitingListModel.clear();
            for (String name : message.replace("Waiting Room: ", "").split(", ")) {
                if (!name.trim().isEmpty()) {
                    waitingListModel.addElement(name);
                    playerScores.putIfAbsent(name, 0);
                    updateGamePlayersLabel();
                }
            }
        } else if (message.equals("WAITING_ROOM_FULL")) {
            if (playButton != null){
                playButton.setEnabled(false);
            }
         
        } else if (message.equals("START_GAME_NOW")) {
            isInGame = true; 
            updateGamePlayersLabel();
            
            showGame();
            if (out != null){
                out.println("READY_FOR_QUESTION");
            } 
        } else if (message.startsWith("TIMER:")) {
            if (timerLabel != null) {
                timerLabel.setText("Time: " + message.split(":")[1] + "s");
            }
        } else if (message.startsWith("QUESTION:")) {
            currentQuestionText = message.substring("QUESTION:".length());
            if (questionLabel != null) {
                questionLabel.setText(currentQuestionText);
            }
        } else if (message.startsWith("CORRECT:")) {
            String name = message.substring("CORRECT:".length());
            playerScores.put(name, playerScores.getOrDefault(name, 0) + 1);
            updateGamePlayersLabel();
        }else if (message.startsWith("WINNERS:")) {
            if (!isInGame || scoreShown){
                return;
            }
            scoreShown = true;

            boolean isTimeUpNoWinner = false;
            String winnersRaw = message.substring("WINNERS:".length()).trim();

            if (winnersRaw.endsWith("TIME_UP:NO_WINNER")) {
                winnersRaw = winnersRaw.replace("TIME_UP:NO_WINNER", "").trim();
                isTimeUpNoWinner = true;
            }

            String[] lines = winnersRaw.split(", ");
            List<String> rankings = new ArrayList<>();
            String winner = "";
            int highestScore = -1;

            for (String line : lines) {
                if (!line.contains("(")) continue;
                String name = line.substring(0, line.indexOf(" ("));
                int score = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
                if (score > highestScore) {
                    highestScore = score;
                    winner = name;
                }
            }

            for (String line : lines) {
                if (!line.contains("(")) continue;
                String name = line.substring(0, line.indexOf(" ("));
                int score = Integer.parseInt(line.substring(line.indexOf("(") + 1, line.indexOf(")")));
                if (waitingListModel.contains(name)) {
                    String label = name + " (" + score + " scores)";
                    if (score == highestScore && !isTimeUpNoWinner) {
                        label += " (Winner)";
                    }
                    rankings.add(label);
                }
            }

            if (isTimeUpNoWinner) {
                JOptionPane.showMessageDialog(frame, "⏰ Time's up! No winner.", "Game Over", JOptionPane.INFORMATION_MESSAGE);
            }

            String finalWinner = isTimeUpNoWinner ? null : winner;
            SwingUtilities.invokeLater(() -> showScoreBoard(rankings, finalWinner));
        }else if (message.startsWith("SCORES:")) {
            String[] entries = message.substring("SCORES:".length()).split(", ");
            for (String entry : entries) {
                if (!entry.isEmpty() && entry.contains(":")) {
                    String[] parts = entry.split(":");
                    playerScores.put(parts[0], Integer.parseInt(parts[1]));
                }
            }
            updateGamePlayersLabel();
        } else if (message.equals("TRY_AGAIN")) {
            JOptionPane.showMessageDialog(frame, "Try again!", "Incorrect", JOptionPane.ERROR_MESSAGE);
        } else if (message.startsWith("REMOVE_PLAYER:")) {
            String removedName = message.substring("REMOVE_PLAYER:".length());
            playerScores.remove(removedName);
            for (int i = 0; i < waitingListModel.size(); i++) {
                if (waitingListModel.get(i).equals(removedName)) {
                    waitingListModel.remove(i);
                    break;
                }
            }
            updateGamePlayersLabel();
        }
    }

    private static void connectToServer() {
        try {
            Socket socket = new Socket("localhost", 5555); 
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(playerName);
            SwingUtilities.invokeLater(Client::showGameScreen);
            new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = in.readLine()) != null) {
                        final String msg = serverMessage;
                        SwingUtilities.invokeLater(() -> processServerMessage(msg));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void styleButton(JButton button) {
        button.setFont(new Font("Serif", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(204, 0, 0)); 
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(120, 30));
    }

    
    private static void showScoreBoard(List<String> rankings, String winnerName) {
        frame.getContentPane().removeAll();
        frame.setTitle("Score Board");
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(new Color(255, 253, 208)); 

        JPanel logoPanel = createLogoBanner();

        String scoreboardTitle = (winnerName == null) ? "🏁 Final Rankings (No Winner)" : "🏆 Final Rankings";
        JLabel title = new JLabel(scoreboardTitle, SwingConstants.CENTER);
        title.setFont(new Font("Serif", Font.BOLD, 18));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(new Color(255, 253, 208));
        topContainer.add(logoPanel);
        topContainer.add(title);

        frame.add(topContainer, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(255, 253, 208));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        for (int i = 0; i < rankings.size(); i++) {
            JLabel label = new JLabel((i + 1) + ". " + rankings.get(i));
            label.setFont(new Font("Serif", Font.PLAIN, 16));
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerPanel.add(label);
            centerPanel.add(Box.createVerticalStrut(5));
        }

        frame.add(centerPanel, BorderLayout.CENTER);
        JPanel bottomBorder = new JPanel();
        bottomBorder.setBackground(new Color(204, 0, 0));
        bottomBorder.setPreferredSize(new Dimension(frame.getWidth(), 10));
        frame.add(bottomBorder, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    
    private static JPanel createLogoBanner() {
        JPanel logoPanel = new JPanel();
        logoPanel.setBackground(new Color(204, 0, 0)); 

        try {
            ImageIcon logoIcon = new ImageIcon(Client.class.getResource("logomath.png"));
            Image originalImg = logoIcon.getImage();
            int width = 120, height = 120;

            BufferedImage resizedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = resizedImg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(originalImg, 0, 0, width, height, null);
            g2d.dispose();

            JLabel logoLabel = new JLabel(new ImageIcon(resizedImg));
            logoPanel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("Failed to load logo in banner.");
        }

        return logoPanel;
    }
    
    private static void playBackgroundMusic() {
        try {
            if (backgroundClip != null && backgroundClip.isRunning()) {
                return; 
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(Client.class.getResource("competition2.wav"));
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);
            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY); // loop forever
            backgroundClip.start();
        } catch (Exception e) {
            System.out.println("Failed to play background music: " + e.getMessage());
        }
    }

}