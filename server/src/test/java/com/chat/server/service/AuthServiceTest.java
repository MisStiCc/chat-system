package com.chat.server.service;

import com.chat.server.model.User;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    private final AuthService authService = new AuthService();

    @Test
    void testAuthenticateWithValidCredentials() {
        Optional<User> user = authService.authenticate("alice", "123");
        assertTrue(user.isPresent());
        assertEquals("Alice", user.get().getNickname());
    }

    @Test
    void testAuthenticateWithInvalidPassword() {
        Optional<User> user = authService.authenticate("alice", "wrong");
        assertFalse(user.isPresent());
    }

    @Test
    void testAuthenticateWithNonExistentUser() {
        Optional<User> user = authService.authenticate("unknown", "123");
        assertFalse(user.isPresent());
    }
}