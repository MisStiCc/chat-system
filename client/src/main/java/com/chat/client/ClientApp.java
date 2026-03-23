package com.chat.client;

import com.chat.client.ui.LoginFrame;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;

public class ClientApp {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}