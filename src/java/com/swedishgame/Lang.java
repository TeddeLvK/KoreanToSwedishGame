package com.swedishgame;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum Lang {
    EN(0, "en", "English", "English"),
    KO(1, "ko", "한국어", "Korean"),
    AR(2, "ar", "العربية", "Arabic"),
    ZH(3, "zh", "中文", "Chinese"),
    ES(4, "es", "Español", "Spanish");

    public final int index;
    public final String code;
    public final String nativeName;
    public final String englishName;

    private static Lang current = EN;

    Lang(int index, String code, String nativeName, String englishName) {
        this.index = index;
        this.code = code;
        this.nativeName = nativeName;
        this.englishName = englishName;
    }

    public static Lang current() {
        return current;
    }

    public static void set(Lang lang) {
        if (lang != null) {
            current = lang;
            savePreference();
        }
    }

    public static void loadPreference() {
        Path p = Paths.get(System.getProperty("user.home"), ".swemon", "settings.txt");
        if (!Files.exists(p)) {
            return;
        }
        try {
            for (String line : Files.readAllLines(p, StandardCharsets.UTF_8)) {
                if (line.startsWith("lang=")) {
                    String val = line.substring(5).trim();
                    for (Lang l : values()) {
                        if (l.code.equalsIgnoreCase(val)) {
                            current = l;
                            return;
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void savePreference() {
        Path dir = Paths.get(System.getProperty("user.home"), ".swemon");
        try {
            Files.createDirectories(dir);
            Files.writeString(dir.resolve("settings.txt"), "lang=" + current.code + "\n", StandardCharsets.UTF_8);
        } catch (IOException ignored) {
        }
    }

    public static String tr(String en, String ko, String ar, String zh, String es) {
        switch (current) {
            case KO: return ko;
            case AR: return ar;
            case ZH: return zh;
            case ES: return es;
            default: return en;
        }
    }
}
