package com.chat.client.ui;

import com.chat.client.network.ServerConnection;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField loginField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Chat Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Welcome to Chat", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(0, 168, 132));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        loginField = new JTextField(15);
        formPanel.add(loginField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField(15);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        formPanel.add(statusLabel, gbc);

        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 168, 132));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> attemptLogin());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(loginButton);

        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loginField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        String login = loginField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (login.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password");
            return;
        }

        loginButton.setEnabled(false);
        loginButton.setText("Connecting...");
        statusLabel.setForeground(Color.BLUE);
        statusLabel.setText("Connecting to server...");

        new Thread(() -> {
            try {
                ServerConnection connection = new ServerConnection();
                String authResult = connection.connectAndAuth(login, password);

                SwingUtilities.invokeLater(() -> {
                    if (authResult.startsWith("AUTH_OK")) {
                        String nickname = authResult.split("\\|")[1];
                        statusLabel.setForeground(new Color(0, 168, 132));
                        statusLabel.setText("Login successful!");

                        ChatFrame chatFrame = new ChatFrame(connection, nickname);
                        chatFrame.setVisible(true);

                        dispose();
                    } else {
                        loginButton.setEnabled(true);
                        loginButton.setText("Login");
                        statusLabel.setForeground(Color.RED);
                        statusLabel.setText("Invalid login or password");
                        connection.close();
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Cannot connect to server: " + e.getMessage());
                });
            }
        }).start();
    }
}