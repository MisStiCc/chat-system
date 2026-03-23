package com.chat.client.ui;

import com.chat.client.network.ServerConnection;
import javax.swing.*;
import java.awt.*;

public class ChatFrame extends JFrame {

    private final ServerConnection connection;
    private final String nickname;

    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;

    public ChatFrame(ServerConnection connection, String nickname) {
        this.connection = connection;
        this.nickname = nickname;
        initUI();
        setupNetworkListener();
    }

    private void initUI() {
        setTitle("Chat - " + nickname);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(30, 30, 30));
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatScroll.setBorder(BorderFactory.createTitledBorder("Messages"));

        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setBackground(new Color(40, 40, 40));
        userList.setForeground(Color.WHITE);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("Users"));
        userScroll.setPreferredSize(new Dimension(200, 0));

        messageField = new JTextField();
        messageField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 168, 132));
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                chatScroll,
                userScroll
        );
        splitPane.setDividerLocation(580);

        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        chatArea.append("Welcome to the chat, " + nickname + "!\n");
        chatArea.append("Type /help for commands\n\n");
    }

    private void setupNetworkListener() {
        connection.startListening(new ServerConnection.MessageListener() {
            @Override
            public void onMessage(String message) {
                SwingUtilities.invokeLater(() -> handleServerMessage(message));
            }

            @Override
            public void onDisconnect() {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(ChatFrame.this,
                            "Disconnected from server",
                            "Connection Lost",
                            JOptionPane.ERROR_MESSAGE);
                    dispose();
                    new LoginFrame().setVisible(true);
                });
            }
        });
    }

    private void handleServerMessage(String message) {
        String[] parts = message.split("\\|", 3);
        String type = parts[0];

        switch (type) {
            case "PRIVATE":
                if (parts.length >= 3) {
                    String sender = parts[1];
                    String text = parts[2];
                    chatArea.append("[PM] " + sender + ": " + text + "\n");
                }
                break;

            case "BROADCAST":
                if (parts.length >= 3) {
                    String sender = parts[1];
                    String text = parts[2];
                    if ("SERVER".equals(sender)) {
                        chatArea.append("*** " + text + " ***\n");
                    } else {
                        chatArea.append(sender + ": " + text + "\n");
                    }
                }
                break;

            case "ACTIVE_USERS":
                if (parts.length >= 2) {
                    updateUserList(parts[1]);
                }
                break;

            case "MSG_SENT":
                chatArea.append("[System] " + (parts.length > 1 ? parts[1] : "Message sent") + "\n");
                break;

            case "ERROR":
                chatArea.append("[Error] " + (parts.length > 1 ? parts[1] : "Unknown error") + "\n");
                break;

            default:
                chatArea.append("[Debug] " + message + "\n");
                break;
        }

        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void updateUserList(String usersStr) {
        userListModel.clear();
        String[] users = usersStr.split(",");
        for (String user : users) {
            if (!user.isEmpty()) {
                userListModel.addElement(user);
            }
        }
    }

    private void sendMessage() {
        String text = messageField.getText().trim();
        if (text.isEmpty()) return;

        if (text.startsWith("/")) {
            handleCommand(text);
        } else {
            connection.sendMessage("BROADCAST|" + text);
            chatArea.append("[You] " + text + "\n");
        }

        messageField.setText("");
        messageField.requestFocus();
    }

    private void handleCommand(String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "/msg":
                if (parts.length > 1) {
                    String[] msgParts = parts[1].split(" ", 2);
                    if (msgParts.length == 2) {
                        String recipient = msgParts[0];
                        String text = msgParts[1];
                        connection.sendMessage("MSG|" + recipient + "|" + text);
                        chatArea.append("[You -> " + recipient + "] " + text + "\n");
                    } else {
                        chatArea.append("[Usage] /msg <user> <message>\n");
                    }
                }
                break;

            case "/users":
                connection.sendMessage("USERS");
                break;

            case "/help":
                chatArea.append("\n--- Commands ---\n");
                chatArea.append("/msg <user> <message> - Send private message\n");
                chatArea.append("/users - Show online users\n");
                chatArea.append("/quit - Disconnect\n");
                chatArea.append("/help - Show this help\n");
                chatArea.append("Just type message - Broadcast to all\n\n");
                break;

            case "/quit":
                connection.sendMessage("DISCONNECT");
                connection.close();
                System.exit(0);
                break;

            default:
                chatArea.append("Unknown command. Type /help for commands\n");
                break;
        }
    }
}