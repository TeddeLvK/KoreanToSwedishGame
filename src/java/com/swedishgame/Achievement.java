package com.swedishgame;

public class Achievement {
    public final String id;
    public final String title;
    public final String swedishTitle;
    public final String description;
    public final boolean specialBadge;

    public Achievement(String id, String title, String swedishTitle, String description, boolean specialBadge) {
        this.id = id;
        this.title = title;
        this.swedishTitle = swedishTitle;
        this.description = description;
        this.specialBadge = specialBadge;
    }
}
