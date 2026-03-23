package com.chat.client.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private MessageListener listener;

    public interface MessageListener {
        void onMessage(String message);
        void onDisconnect();
    }

    public ServerConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public String connectAndAuth(String login, String password) throws IOException {
        String welcome = in.readLine();
        System.out.println("Server: " + welcome);

        out.println("AUTH|" + login + "|" + password);

        String authResponse = in.readLine();
        System.out.println("Auth response: " + authResponse);

        return authResponse;
    }

    public void startListening(MessageListener listener) {
        this.listener = listener;

        Thread reader = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    if (this.listener != null) {
                        this.listener.onMessage(line);
                    }
                }
            } catch (IOException e) {
                if (this.listener != null) {
                    this.listener.onDisconnect();
                }
            }
        });
        reader.setDaemon(true);
        reader.start();
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
            out.flush();
        }
    }

    public void close() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}