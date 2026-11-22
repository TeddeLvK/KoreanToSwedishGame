package com.swedishgame;

import java.util.ArrayList;
import java.util.List;

public class Level {
    public final int id;
    public final String title;
    public final String zoneName;
    public final int spawnX;
    public final int spawnY;
    public String summary = "";
    public final List<String> tips = new ArrayList<>();
    public final List<String[]> vocab = new ArrayList<>();
    public final List<String> mapRows = new ArrayList<>();
    public final List<Entity> entities = new ArrayList<>();

    public Level(int id, String title, String zoneName, int spawnX, int spawnY) {
        this.id = id;
        this.title = title;
        this.zoneName = zoneName;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public String getFlatMap() {
        StringBuilder sb = new StringBuilder();
        for (String row : mapRows) {
            sb.append(row);
        }
        return sb.toString();
    }
}
