package com.swedishgame;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;

public class GameWindow extends JFrame {
    private final GamePanel panel;
    private boolean fullscreen = false;

    public GameWindow(List<Level> levels, SaveManager[] saveSlots, Glossary glossary) {
        super("Swemon - Ordmästarnas uppgång");
        this.panel = new GamePanel(levels, saveSlots, glossary, this::toggleFullscreen);
        setContentPane(panel);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                panel.persist();
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    public void setFullscreen(boolean enable) {
        if (this.fullscreen == enable) {
            return;
        }
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        dispose();
        setUndecorated(enable);
        if (enable) {
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(this);
            } else {
                setExtendedState(JFrame.MAXIMIZED_BOTH);
                setVisible(true);
            }
        } else {
            if (gd.getFullScreenWindow() == this) {
                gd.setFullScreenWindow(null);
            }
            setExtendedState(JFrame.NORMAL);
            pack();
            setLocationRelativeTo(null);
            setVisible(true);
        }
        this.fullscreen = enable;
        panel.requestFocusInWindow();
    }

    public void toggleFullscreen() {
        setFullscreen(!fullscreen);
    }
}
