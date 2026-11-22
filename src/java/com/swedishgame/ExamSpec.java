package com.swedishgame;

public class ExamSpec {
    public final String id;
    public final String title;
    public final String badgeLabel;
    public final String spriteKey;
    public final int tx;
    public final int ty;
    public final int startLevel;
    public final int endLevel;
    public final int questionCount;
    public final boolean isBig;

    public ExamSpec(
        String id,
        String title,
        String badgeLabel,
        String spriteKey,
        int tx,
        int ty,
        int startLevel,
        int endLevel,
        int questionCount,
        boolean isBig
    ) {
        this.id = id;
        this.title = title;
        this.badgeLabel = badgeLabel;
        this.spriteKey = spriteKey;
        this.tx = tx;
        this.ty = ty;
        this.startLevel = startLevel;
        this.endLevel = endLevel;
        this.questionCount = questionCount;
        this.isBig = isBig;
    }
}
