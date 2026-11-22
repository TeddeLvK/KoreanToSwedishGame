package com.swedishgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Glossary {
    private static final String[] SUFFIXES = {
        "arna", "erna", "orna", "ena", "na", "en", "et", "ar", "er", "or", "n", "t", "s"
    };

    private final Map<String, String[]> entries = new LinkedHashMap<>();
    private final Map<String, String> lowerToKey = new HashMap<>();

    public void load(Path path) throws IOException {
        BufferedReader br;
        if (Files.exists(path)) {
            br = Files.newBufferedReader(path, StandardCharsets.UTF_8);
        } else {
            InputStream in = Glossary.class.getResourceAsStream("/data/glossary.txt");
            if (in == null) {
                return;
            }
            br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        try (BufferedReader reader = br) {
            String line;
            while ((line = reader.readLine()) != null) {
                int eq = line.indexOf('=');
                if (eq > 0) {
                    String sv = line.substring(0, eq).trim();
                    String[] trans = line.substring(eq + 1).trim().split("\\|", -1);
                    if (trans.length >= 1) {
                        entries.put(sv, trans);
                        lowerToKey.put(sv.toLowerCase(java.util.Locale.ROOT), sv);
                    }
                }
            }
        }
    }

    public String translate(String swedishKey) {
        if (swedishKey == null) {
            return null;
        }
        String[] arr = entries.get(swedishKey);
        if (arr == null) {
            String k = lowerToKey.get(swedishKey.toLowerCase(java.util.Locale.ROOT));
            if (k != null) {
                arr = entries.get(k);
            }
        }
        if (arr == null || arr.length == 0) {
            return null;
        }
        int idx = Lang.current().index;
        return idx < arr.length && !arr[idx].isEmpty() ? arr[idx] : arr[0];
    }

    public Map<String, String> getAllEntries() {
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> e : entries.entrySet()) {
            String t = translate(e.getKey());
            if (t != null) {
                out.put(e.getKey(), t);
            }
        }
        return Collections.unmodifiableMap(out);
    }

    public String lookupWord(String rawWord) {
        if (rawWord == null || rawWord.isEmpty()) {
            return null;
        }
        String cleaned = stripPunctuation(rawWord);
        if (cleaned.length() < 2) {
            return null;
        }
        String low = cleaned.toLowerCase(java.util.Locale.ROOT);
        String exactKey = lowerToKey.get(low);
        if (exactKey != null) {
            String meaning = translate(exactKey);
            if (meaning != null) {
                return exactKey + "  -  " + meaning;
            }
        }
        for (String suf : SUFFIXES) {
            if (low.endsWith(suf) && low.length() > suf.length() + 2) {
                String stem = low.substring(0, low.length() - suf.length());
                String stemKey = lowerToKey.get(stem);
                if (stemKey != null) {
                    String meaning = translate(stemKey);
                    if (meaning != null) {
                        return stemKey + "  -  " + meaning;
                    }
                }
            }
        }
        return null;
    }

    public String lookupPhrase(String phrase) {
        if (phrase == null || phrase.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String[] words = phrase.split("\\s+");
        for (String w : words) {
            String hit = lookupWord(w);
            if (hit != null && sb.indexOf(hit) < 0) {
                if (sb.length() > 0) {
                    sb.append("   |   ");
                }
                sb.append(hit);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String stripPunctuation(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (Character.isLetter(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
}
