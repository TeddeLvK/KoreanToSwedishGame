package com.swedishgame;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    public final String id;
    public final String name;
    public final String spriteKey;
    public final int tx;
    public final int ty;
    public final String greeting;
    public final List<Task> tasks;

    public Entity(String id, String name, String spriteKey, int tx, int ty, String greeting) {
        this.id = id;
        this.name = name;
        this.spriteKey = spriteKey;
        this.tx = tx;
        this.ty = ty;
        this.greeting = greeting;
        this.tasks = new ArrayList<>();
    }
}
