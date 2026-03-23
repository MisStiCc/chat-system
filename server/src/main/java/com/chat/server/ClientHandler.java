package com.chat.server;

import com.chat.server.model.User;
import com.chat.server.service.AuthService;
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
    private User authenticatedUser;
    private PrintWriter out;
    private BufferedReader in;
    private boolean authenticated;

    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
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
                        authenticatedUser = user;
                        authenticated = true;
                        authenticatedUser.setSocket(socket);
                        authenticatedUser.setOut(out);
                        sendMessage("AUTH_OK|" + user.getNickname());
                        logger.info("User authenticated: " + user.getNickname());
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
                    sendMessage("MSG_SENT|Message sent to " + recipient);
                } else {
                    sendMessage("ERROR|Invalid MSG format. Use: MSG|recipient|text");
                }
                break;

            case "BROADCAST":
                if (parts.length >= 2) {
                    String message = parts[1];
                    sendMessage("BROADCAST_SENT|Your message broadcasted");
                } else {
                    sendMessage("ERROR|Invalid BROADCAST format. Use: BROADCAST|text");
                }
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