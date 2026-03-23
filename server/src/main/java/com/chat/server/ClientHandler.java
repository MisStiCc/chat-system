package com.chat.server;

import com.chat.server.model.User;
import com.chat.server.service.AuthService;
import com.chat.server.service.SessionManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientHandler implements Runnable {
    private static final Logger logger = Logger.getLogger(ClientHandler.class.getName());

    private final Socket socket;
    private final AuthService authService;
    private final SessionManager sessionManager;
    private User authenticatedUser;
    private PrintWriter out;
    private BufferedReader in;
    private boolean authenticated;

    public ClientHandler(Socket socket, AuthService authService, SessionManager sessionManager) {
        this.socket = socket;
        this.authService = authService;
        this.sessionManager = sessionManager;
        this.authenticated = false;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            sendMessage("WELCOME|Connected to chat server. Please authenticate.");

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                logger.info("Received from client: " + inputLine);

                if (!authenticated) {
                    handleAuth(inputLine);
                } else {
                    handleCommand(inputLine);
                }
            }
        } catch (IOException e) {
            logger.warning("Client disconnected: " + e.getMessage());
        } finally {
            closeConnection();
        }
    }

    private void handleAuth(String input) {
        String[] parts = input.split("\\|");

        if (parts.length == 3 && "AUTH".equals(parts[0])) {
            String login = parts[1];
            String password = parts[2];

            authService.authenticate(login, password).ifPresentOrElse(
                    user -> {
                        if (sessionManager.isUserOnline(user.getNickname())) {
                            sendMessage("AUTH_FAIL|User already logged in from another session");
                            return;
                        }

                        authenticatedUser = user;
                        authenticated = true;
                        authenticatedUser.setSocket(socket);
                        authenticatedUser.setOut(out);

                        sessionManager.addUser(authenticatedUser);

                        sendMessage("AUTH_OK|" + user.getNickname());
                        logger.info("User authenticated: " + user.getNickname());

                        sessionManager.notifyUserJoined(user.getNickname());
                    },
                    () -> {
                        sendMessage("AUTH_FAIL|Invalid login or password");
                        logger.info("Authentication failed for: " + login);
                    }
            );
        } else {
            sendMessage("ERROR|Please authenticate first. Use: AUTH|login|password");
        }
    }

    private void handleCommand(String input) {
        String[] parts = input.split("\\|");
        String command = parts[0];

        switch (command) {
            case "MSG":
                if (parts.length >= 3) {
                    String recipient = parts[1];
                    String message = parts[2];

                    boolean sent = sessionManager.sendPrivateMessage(
                            authenticatedUser.getNickname(),
                            recipient,
                            message
                    );

                    if (sent) {
                        sendMessage("MSG_SENT|Message sent to " + recipient);
                    } else {
                        sendMessage("ERROR|User " + recipient + " is offline or doesn't exist");
                    }
                } else {
                    sendMessage("ERROR|Invalid MSG format. Use: MSG|recipient|text");
                }
                break;

            case "BROADCAST":
                if (parts.length >= 2) {
                    String message = parts[1];
                    sessionManager.broadcast(authenticatedUser.getNickname(), message);
                    sendMessage("BROADCAST_SENT|Your message has been broadcasted");
                } else {
                    sendMessage("ERROR|Invalid BROADCAST format. Use: BROADCAST|text");
                }
                break;

            case "USERS":
                StringBuilder users = new StringBuilder("ACTIVE_USERS|");
                for (String nickname : sessionManager.getAllUsers().keySet()) {
                    users.append(nickname).append(",");
                }
                sendMessage(users.toString());
                break;

            case "DISCONNECT":
                sendMessage("DISCONNECT_OK|Goodbye!");
                closeConnection();
                break;

            default:
                sendMessage("ERROR|Unknown command: " + command);
                break;
        }
    }

    private void sendMessage(String message) {
        if (out != null && !socket.isClosed()) {
            out.println(message);
            out.flush();
        }
    }

    private void closeConnection() {
        try {
            if (authenticatedUser != null) {
                sessionManager.notifyUserLeft(authenticatedUser.getNickname());
                sessionManager.removeUser(authenticatedUser.getNickname());
                logger.info("User disconnected: " + authenticatedUser.getNickname());
            }
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            logger.warning("Error closing connection: " + e.getMessage());
        }
    }
}