
package calculate;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedHashMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    static final int MaxPlayers = 4;
    private static Timer gameTimer = null;
    private static int gameTimeInSeconds = 120;
    private static ArrayList<ClientHandler> ConnectedPlayers = new ArrayList<>();
    public static ArrayList<ClientHandler> WaitingRoom = new ArrayList<>();
    public static boolean gameStarted = false;
    private static Timer gameStartTimer = null;
    private static final Object timerLock = new Object();
    static final Map<String, Boolean> finishedPlayers = new ConcurrentHashMap<>();
    static Server.Question currentQuestion = null;
    static Map<String, Integer> finishedOrder = new LinkedHashMap<>();

    private static final Question[] questions = {
        new Question("Find the value of X: 5x - 6 = 9", "3"),
        new Question("Find the value of X: 7, 6, 5, 4, X, 2", "3"),
        new Question("What is 7 + 6?", "13"),
        new Question("Solve: 10 - 4", "6"),
        new Question("What is 3 x 4?", "12"),
        new Question("If x + 2 = 5, what is x?", "3"),
        new Question("What is 18 ÷ 2?", "9"),
        new Question("What is the next number: 2, 4, 6, X", "8"),
        new Question("Solve: 12 ÷ 3", "4"),
        new Question("What is 5 + 7?", "12"),
        new Question("Find X: X - 3 = 7", "10"),
        new Question("What is 15 - 9?", "6"),
        new Question("What is 6 x 2?", "12"),
        new Question("What is 8 ÷ 4?", "2"),
        new Question("If 2x = 10, then x =", "5"),
        new Question("What is 3 + 3 + 3?", "9"),
        new Question("Find X: X + 5 = 12", "7"),
        new Question("What is 4 x 5?", "20"),
        new Question("What is 16 ÷ 4?", "4"),
        new Question("Solve: X = 11 - 6", "5")
    };
    
    static List<Question> gameQuestions = new ArrayList<>();
    static int currentQuestionIndex = 0;

    public static void main(String[] args) throws IOException {
        try {
            ServerSocket serverSocket = new ServerSocket(5555);
            System.out.println("Waiting for connection...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler client = new ClientHandler(clientSocket);

                ConnectedPlayers.add(client);
                new Thread(client).start();
                broadcastPlayersList();

                if (WaitingRoom.size() >= MaxPlayers) {
                    client.sendMessage("WAITING_ROOM_FULL");
                }
            }
        } catch (IOException e) {
            System.err.println("IO Exception");
        }
    }

    public static synchronized void broadcastPlayersList() {
        for (ClientHandler client : ConnectedPlayers) {
            StringBuilder PlayersList = new StringBuilder("Players connected: ");
            for (ClientHandler player : ConnectedPlayers) {
                if (WaitingRoom.contains(player)) {
                    PlayersList.append(player.getPlayerName()).append(" (In game), ");
                } else {
                    PlayersList.append(player.getPlayerName()).append(", ");
                }
            }
            client.sendMessage(PlayersList.toString());
        }
    }

    public static synchronized void broadcastWaitingRoom() {
        for (ClientHandler client : WaitingRoom) {
            StringBuilder waitingRoomStatus = new StringBuilder("Waiting Room: ");
            for (ClientHandler player : WaitingRoom) {
                waitingRoomStatus.append(player.getPlayerName()).append(", ");
            }
            client.sendMessage(waitingRoomStatus.toString());
        }
    }

    public static synchronized void addToWaitingRoom(ClientHandler c) {
        if (gameStarted) {
            c.sendMessage("WAITING_ROOM_CLOSED"); 
            return;
        }

        if (WaitingRoom.size() >= MaxPlayers) {
            c.sendMessage("WAITING_ROOM_FULL"); 
            return;
        }

        if (!WaitingRoom.contains(c)) {
            WaitingRoom.add(c);
            c.sendMessage("WAITING_ROOM");
            broadcastPlayersList();
            broadcastWaitingRoom();

            int roomSize = WaitingRoom.size();

            if (roomSize == 2) {
                // Start 30 seconds timer
                synchronized (timerLock) {
                    if (gameStartTimer == null) {
                        gameStartTimer = new Timer();
                        gameStartTimer.scheduleAtFixedRate(new TimerTask() {
                            int countdown = 30;

                            @Override
                            public void run() {
                                if (countdown >= 0) {
                                    broadcastToWaitingRoom("TIMER:" + countdown);
                                    countdown--;
                                } else {
                                    this.cancel();
                                    synchronized (Server.class) {
                                        if (!gameStarted && WaitingRoom.size() >= 2) {
                                            startGame();
                                        }
                                    }
                                }
                            }
                        }, 0, 1000);
                        System.out.println("Game start timer started (30s)");
                    }
                }
            } else if (roomSize == MaxPlayers) {
                synchronized (timerLock) {
                    if (gameStartTimer != null) {
                        gameStartTimer.cancel();
                        gameStartTimer = null;
                        System.out.println("Timer canceled due to full room");
                    }
                }

                for (ClientHandler client : ConnectedPlayers) {
                    client.sendMessage("WAITING_ROOM_FULL");
                }
               

                startGame();
            }
        }
    }

   public static synchronized void startGame() {
        gameStarted = true;
        finishedPlayers.clear();
        finishedOrder.clear(); 

        for (ClientHandler player : WaitingRoom) {
            finishedPlayers.put(player.getPlayerName(), false);
        }


        List<Question> shuffled = new ArrayList<>(Arrays.asList(questions));
        Collections.shuffle(shuffled);
        gameQuestions = shuffled.subList(0, 5);
        currentQuestionIndex = 0;
        currentQuestion = gameQuestions.get(currentQuestionIndex);


        for (ClientHandler player : WaitingRoom) {
            player.sendMessage("START_GAME_NOW");
        }

        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            int countdown = gameTimeInSeconds;

            @Override
            public void run() {
                if (countdown >= 0) {
                    broadcastToWaitingRoom("TIMER:" + countdown);
                    countdown--;
                } else {
                    this.cancel();
                    handleGameTimeout();
                }
            }
        }, 0, 1000);

    }

    public static synchronized void broadcastToWaitingRoom(String msg) {
        for (ClientHandler client : WaitingRoom) {
            client.sendMessage(msg);
        }
    }

    public static synchronized void removePlayer(ClientHandler client) {
        ConnectedPlayers.remove(client);
        WaitingRoom.remove(client);
        broadcastPlayersList();
        broadcastWaitingRoom();

        broadcastToAll("REMOVE_PLAYER:" + client.getPlayerName());
    }
    
    public static synchronized void broadcastToAll(String msg) {
        for (ClientHandler client : ConnectedPlayers) {
            client.sendMessage(msg);
        }
    }

    public static class Question {
        String q, a;
        public Question(String q, String a) {
            this.q = q;
            this.a = a;
        }
    }
    
    public static synchronized void broadcastScores() {
        StringBuilder sb = new StringBuilder("SCORES:");
        for (ClientHandler player : WaitingRoom) {
            int score = ClientHandler.getScore(player.getPlayerName());
            sb.append(player.getPlayerName()).append(":").append(score).append(", ");
        }
        broadcastToWaitingRoom(sb.toString());
    }
    
   public static synchronized void announceWinners() {
        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(finishedOrder.entrySet());
        sorted.sort((a, b) -> b.getValue() - a.getValue());

        StringBuilder result = new StringBuilder("WINNERS:");
        
        for (Map.Entry<String, Integer> entry : sorted) {
            result.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
        }

        broadcastToAll(result.toString());

        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }



        gameStarted = false;
         if (gameTimer != null) {    
            gameTimer.cancel();
            gameTimer = null;
        }
         
        WaitingRoom.clear();
        finishedPlayers.clear();
        finishedOrder.clear();
        gameQuestions.clear();
        currentQuestionIndex = 0;
        currentQuestion = null;
    }



    public static synchronized void handleGameTimeout() {
        if (!gameStarted) {
            return; 
        }
        System.out.println("Game time is up.");
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }

        gameStarted = false;
        gameQuestions.clear();
        currentQuestionIndex = 0;
        currentQuestion = null;

        StringBuilder sb = new StringBuilder("WINNERS:");
        boolean allZero = true;

        for (ClientHandler player : WaitingRoom) {
            int score = ClientHandler.getScore(player.getPlayerName());
            if (score > 0) allZero = false;
            sb.append(player.getPlayerName()).append(" (").append(score).append("), ");
        }

        
            sb.append("TIME_UP:NO_WINNER");
        

        broadcastToAll(sb.toString());


        WaitingRoom.clear();
        finishedPlayers.clear();
        finishedOrder.clear();
    }
    
    public static synchronized void manualStartGame() {
        if (!gameStarted && WaitingRoom.size() >= 2) {
            // Cancel the timer if it exists
            synchronized (timerLock) {
                if (gameStartTimer != null) {
                    gameStartTimer.cancel();
                    gameStartTimer = null;
                    System.out.println("Timer canceled due to manual start");
                }
            }
            startGame();
        }
    }
}

class ClientHandler implements Runnable {
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private String playerName;
    private int personalQuestionIndex = 0;
    private static Map<String, Integer> playerScores = new ConcurrentHashMap<>();

    public ClientHandler(Socket clientSocket) throws IOException {
        this.client = clientSocket;
        this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out = new PrintWriter(client.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            playerName = in.readLine();
            Server.broadcastPlayersList();

            String input;
            while ((input = in.readLine()) != null) {
                if (input.equalsIgnoreCase("play")) {
                    Server.addToWaitingRoom(this);
                }else if (input.equalsIgnoreCase("MANUAL_START")) {
                    Server.manualStartGame();
                }  else if (input.equalsIgnoreCase("start")) {
                    if (Server.WaitingRoom.size() >= Server.MaxPlayers) {
                        Server.startGame();
                    }
                } else if (input.equalsIgnoreCase("LEAVE")) {
                    break;
                } else if (input.equalsIgnoreCase("READY_FOR_QUESTION")) {
                    sendMessage("QUESTION:" + Server.gameQuestions.get(0).q);
                } else if (input.startsWith("ANSWER:")) {
                    String answer = input.substring(7).trim();

                    if (personalQuestionIndex < Server.gameQuestions.size()) {
                        Server.Question q = Server.gameQuestions.get(personalQuestionIndex);

                        if (q.a.equalsIgnoreCase(answer)) {
                            playerScores.put(playerName, playerScores.getOrDefault(playerName, 0) + 1);
                            sendMessage("CORRECT:" + playerName);
                            Server.broadcastScores();

                            personalQuestionIndex++;
                            if (personalQuestionIndex < Server.gameQuestions.size()) {
                                sendMessage("QUESTION:" + Server.gameQuestions.get(personalQuestionIndex).q);
                            } else {
                                sendMessage("GAME_OVER");

                                Map<String, Integer> allScores = new HashMap<>();
                                for (ClientHandler p : Server.WaitingRoom) {
                                    allScores.put(p.getPlayerName(), ClientHandler.getScore(p.getPlayerName()));
                                }

                                List<Map.Entry<String, Integer>> sorted = new ArrayList<>(allScores.entrySet());
                                sorted.sort((a, b) -> b.getValue() - a.getValue());

                                StringBuilder result = new StringBuilder("WINNERS:");
                                for (Map.Entry<String, Integer> entry : sorted) {
                                    result.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
                                }

                                Server.broadcastToAll(result.toString());
                            }
                        } else {
                                sendMessage("TRY_AGAIN");
                        }
                    }
                }else if (input.startsWith("GAME_FINISHED:")) {
                    String[] parts = input.split(":");
                    String name = parts[1];
                    Server.finishedPlayers.put(name, true);

                    long unfinished = Server.finishedPlayers.values().stream().filter(done -> !done).count();
                    if (unfinished == 0 || Server.WaitingRoom.size() <= 1) {
                        Server.announceWinners();
                    }
                }              
            }
        } catch (IOException e) {
            System.out.println("Client " + playerName + " disconnected.");
        } finally {
            Server.removePlayer(this);
            closeConnection();
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public static int getScore(String playerName) {
        return playerScores.getOrDefault(playerName, 0);
    }

    public void closeConnection() {
        try {
            in.close();
            out.close();
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }   
}