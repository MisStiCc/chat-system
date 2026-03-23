package com.chat.server;

import com.chat.server.service.AuthService;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class ChatServer {
    private static final Logger logger = Logger.getLogger(ChatServer.class.getName());

    private final int port;
    private final AuthService authService;
    private final ExecutorService clientThreadPool;
    private volatile boolean running;

    public ChatServer(int port) {
        this.port = port;
        this.authService = new AuthService();
        this.clientThreadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        running = true;
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            logger.info("Server started on port " + port);
            logger.info("Test users: alice/123, bob/123, charlie/123, dmitry/123, elena/123");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                logger.info("New connection from: " + clientSocket.getRemoteSocketAddress());

                ClientHandler handler = new ClientHandler(clientSocket, authService);
                clientThreadPool.submit(handler);
            }
        } catch (IOException e) {
            if (running) {
                logger.severe("Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        clientThreadPool.shutdown();
        logger.info("Server stopped");
    }

    public static void main(String[] args) {
        int port = 8189;
        ChatServer server = new ChatServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }
}