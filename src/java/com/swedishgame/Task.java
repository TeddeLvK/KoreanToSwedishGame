package com.swedishgame;

import java.util.ArrayList;
import java.util.List;

public class Task {
    public final String type;
    public final String prompt;
    public final List<String> options;
    public final String answer;
    public final List<String> tokens;
    public final List<String> distractors;
    public final String explanation;

    public Task(
        String type,
        String prompt,
        List<String> options,
        String answer,
        List<String> tokens,
        List<String> distractors,
        String explanation
    ) {
        this.type = type;
        this.prompt = prompt;
        this.options = new ArrayList<>(options);
        this.answer = answer;
        this.tokens = new ArrayList<>(tokens);
        this.distractors = new ArrayList<>(distractors);
        this.explanation = explanation;
    }
}
