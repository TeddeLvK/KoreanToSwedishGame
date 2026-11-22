package com.swedishgame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class CurriculumLoader {
    private CurriculumLoader() {}

    private static BufferedReader openReader(Path filePath, String resourcePath) throws IOException {
        if (Files.exists(filePath)) {
            return Files.newBufferedReader(filePath, StandardCharsets.UTF_8);
        }
        InputStream in = CurriculumLoader.class.getResourceAsStream(resourcePath);
        if (in == null) {
            throw new IOException("Missing resource: " + resourcePath);
        }
        return new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    public static List<Level> load(Path filePath) throws IOException {
        List<Level> levels = new ArrayList<>();
        Level curLevel = null;
        Entity curEntity = null;

        try (BufferedReader br = openReader(filePath, "/data/levels.txt")) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                String tag = parts[0];

                switch (tag) {
                    case "LEVEL":
                        curLevel = new Level(
                            Integer.parseInt(parts[1]),
                            parts[2],
                            parts[3],
                            Integer.parseInt(parts[4]),
                            Integer.parseInt(parts[5])
                        );
                        curEntity = null;
                        break;
                    case "SUMMARY":
                        if (curLevel != null) {
                            curLevel.summary = parts[1];
                        }
                        break;
                    case "TIP":
                        if (curLevel != null) {
                            curLevel.tips.add(parts[1]);
                        }
                        break;
                    case "VOCAB":
                        if (curLevel != null && parts.length > 1 && !parts[1].isEmpty()) {
                            for (String pair : parts[1].split(";")) {
                                int eq = pair.indexOf('=');
                                if (eq > 0) {
                                    curLevel.vocab.add(new String[]{
                                        pair.substring(0, eq).trim(),
                                        pair.substring(eq + 1).trim()
                                    });
                                }
                            }
                        }
                        break;
                    case "MAP":
                        if (curLevel != null) {
                            curLevel.mapRows.add(parts[1]);
                        }
                        break;
                    case "ENTITY":
                        if (curLevel != null) {
                            curEntity = new Entity(
                                parts[1],
                                parts[2],
                                parts[3],
                                Integer.parseInt(parts[4]),
                                Integer.parseInt(parts[5]),
                                parts[6]
                            );
                            curLevel.entities.add(curEntity);
                        }
                        break;
                    case "TASK":
                        if (curEntity != null) {
                            String type = parts[1];
                            String prompt = parts[2];
                            if ("build".equals(type)) {
                                List<String> tokens = splitList(parts[3]);
                                List<String> distractors = splitList(parts[4]);
                                String explanation = parts.length > 5 ? parts[5] : "";
                                curEntity.tasks.add(new Task(
                                    type,
                                    prompt,
                                    Collections.emptyList(),
                                    "",
                                    tokens,
                                    distractors,
                                    explanation
                                ));
                            } else {
                                List<String> options = splitList(parts[3]);
                                String answer = parts[4];
                                String explanation = parts.length > 5 ? parts[5] : "";
                                curEntity.tasks.add(new Task(
                                    type,
                                    prompt,
                                    options,
                                    answer,
                                    Collections.emptyList(),
                                    Collections.emptyList(),
                                    explanation
                                ));
                            }
                        }
                        break;
                    case "ENDLEVEL":
                        if (curLevel != null) {
                            levels.add(curLevel);
                            curLevel = null;
                            curEntity = null;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return levels;
    }

    private static List<String> splitList(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(raw.split(";")));
    }
}
