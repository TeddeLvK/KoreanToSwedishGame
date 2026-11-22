package com.swedishgame;

import java.nio.file.Paths;
import java.util.List;
import javax.swing.SwingUtilities;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        Lang.loadPreference();
        List<Level> levels = CurriculumLoader.load(Paths.get("src/data/levels.txt"));
        Glossary glossary = new Glossary();
        glossary.load(Paths.get("src/data/glossary.txt"));
        SaveManager[] saveSlots = SaveManager.loadAllSlots();
        SwingUtilities.invokeLater(() -> {
            GameWindow win = new GameWindow(levels, saveSlots, glossary);
            win.setFullscreen(true);
        });
    }
}
