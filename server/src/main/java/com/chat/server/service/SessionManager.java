package com.chat.server.service;

import com.chat.server.model.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class SessionManager {
    private static final Logger logger = Logger.getLogger(SessionManager.class.getName());

    private final Map<String, User> activeUsers = new ConcurrentHashMap<>();

    public void addUser(User user) {
        activeUsers.put(user.getNickname(), user);
        logger.info("User joined: " + user.getNickname() + ". Active users: " + activeUsers.size());
    }

    public void removeUser(String nickname) {
        activeUsers.remove(nickname);
        logger.info("User left: " + nickname + ". Active users: " + activeUsers.size());
    }

    public User getUser(String nickname) {
        return activeUsers.get(nickname);
    }

    public boolean isUserOnline(String nickname) {
        return activeUsers.containsKey(nickname);
    }

    public Map<String, User> getAllUsers() {
        return activeUsers;
    }

    public boolean sendPrivateMessage(String fromNickname, String toNickname, String message) {
        User recipient = activeUsers.get(toNickname);
        if (recipient != null && recipient.getOut() != null) {
            recipient.getOut().println("PRIVATE|" + fromNickname + "|" + message);
            recipient.getOut().flush();
            logger.info("Private message from " + fromNickname + " to " + toNickname);
            return true;
        }
        return false;
    }

    public void broadcast(String fromNickname, String message) {
        String formattedMessage = "BROADCAST|" + fromNickname + "|" + message;
        for (User user : activeUsers.values()) {
            if (user.getOut() != null) {
                user.getOut().println(formattedMessage);
                user.getOut().flush();
            }
        }
        logger.info("Broadcast from " + fromNickname + ": " + message);
    }

    public void notifyUserJoined(String nickname) {
        broadcast("SERVER", "User " + nickname + " joined the chat");
    }

    public void notifyUserLeft(String nickname) {
        broadcast("SERVER", "User " + nickname + " left the chat");
    }
}