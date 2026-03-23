package com.chat.server.model;

import java.io.PrintWriter;
import java.net.Socket;

public class User {
    private final String login;
    private final String password;
    private final String nickname;
    private Socket socket;
    private PrintWriter out;

    public User(String login, String password, String nickname) {
        this.login = login;
        this.password = password;
        this.nickname = nickname;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setOut(PrintWriter out) {
        this.out = out;
    }

    @Override
    public String toString() {
        return nickname + " (" + login + ")";
    }
}