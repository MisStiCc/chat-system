package com.chat.server.service;

import com.chat.server.model.User;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final Map<String, User> usersByLogin = new ConcurrentHashMap<>();

    public AuthService() {
        addUser(new User("alice", "123", "Alice"));
        addUser(new User("bob", "123", "Bob"));
        addUser(new User("charlie", "123", "Charlie"));
        addUser(new User("dmitry", "123", "Dmitry"));
        addUser(new User("elena", "123", "Elena"));
    }

    private void addUser(User user) {
        usersByLogin.put(user.getLogin(), user);
    }

    public Optional<User> authenticate(String login, String password) {
        User user = usersByLogin.get(login);
        if (user != null && user.getPassword().equals(password)) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(usersByLogin.get(login));
    }
}