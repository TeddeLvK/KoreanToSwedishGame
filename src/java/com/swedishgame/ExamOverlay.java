package com.swedishgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExamOverlay {
    private static class WordHit {
        final Rectangle rect;
        final String tooltip;

        WordHit(Rectangle rect, String tooltip) {
            this.rect = rect;
            this.tooltip = tooltip;
        }
    }

    private static class AnswerRecord {
        final int number;
        final Task task;
        final String userAnswer;
        final String correctAnswer;
        final boolean correct;

        AnswerRecord(int number, Task task, String userAnswer, String correctAnswer, boolean correct) {
            this.number = number;
            this.task = task;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.correct = correct;
        }
    }

    public final ExamSpec spec;
    private final AssetLoader assets;
    private final Glossary glossary;
    private final List<Task> tasks;

    private int index = 0;
    private boolean finished = false;
    private int score = 0;

    private List<String> shuffledOptions = new ArrayList<>();
    private final List<Rectangle> optionRects = new ArrayList<>();

    private final List<String> bankTokens = new ArrayList<>();
    private final List<String> selectedTokens = new ArrayList<>();
    private final List<Rectangle> bankRects = new ArrayList<>();
    private final List<Rectangle> selectedRects = new ArrayList<>();
    private final List<WordHit> promptHits = new ArrayList<>();

    private final List<AnswerRecord> records = new ArrayList<>();
    private int filterMode = 0;
    private int reviewPage = 0;
    private static final int REVIEW_PER_PAGE = 4;

    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 96, 46, 36, 36);
    private final Rectangle clearBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 154, UIUtils.SCREEN_HEIGHT - 96, 142, 42);
    private final Rectangle submitBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 + 12, UIUtils.SCREEN_HEIGHT - 96, 142, 42);

    private final Rectangle filterAllBtn = new Rectangle(72, 118, 92, 30);
    private final Rectangle filterWrongBtn = new Rectangle(172, 118, 112, 30);
    private final Rectangle filterRightBtn = new Rectangle(292, 118, 112, 30);

    private final Rectangle prevPageBtn = new Rectangle(72, UIUtils.SCREEN_HEIGHT - 84, 110, 38);
    private final Rectangle nextPageBtn = new Rectangle(192, UIUtils.SCREEN_HEIGHT - 84, 110, 38);
    private final Rectangle retakeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 344, UIUtils.SCREEN_HEIGHT - 84, 132, 38);
    private final Rectangle doneBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 202, UIUtils.SCREEN_HEIGHT - 84, 130, 38);

    public ExamOverlay(ExamSpec spec, List<Task> tasks, AssetLoader assets, Glossary glossary) {
        this.spec = spec;
        this.tasks = tasks;
        this.assets = assets;
        this.glossary = glossary;
        if (tasks.isEmpty()) {
            this.finished = true;
        } else {
            setupTask();
        }
    }

    public int getScore() {
        return score;
    }

    public int getTotal() {
        return tasks.size();
    }

    public boolean isPassed() {
        return !tasks.isEmpty() && score * 100 >= tasks.size() * 70;
    }

    private Task currentTask() {
        return tasks.get(index);
    }

    private void setupTask() {
        Task task = currentTask();
        if ("build".equals(task.type)) {
            bankTokens.clear();
            selectedTokens.clear();
            bankTokens.addAll(task.tokens);
            bankTokens.addAll(task.distractors);
            Collections.shuffle(bankTokens);
        } else {
            shuffledOptions = new ArrayList<>(task.options);
            Collections.shuffle(shuffledOptions);
        }
    }

    private void recordAndAdvance(String userAns, String correctAns, boolean ok) {
        if (ok) {
            score++;
        }
        records.add(new AnswerRecord(index + 1, currentTask(), userAns, correctAns, ok));
        index++;
        if (index >= tasks.size()) {
            finished = true;
            reviewPage = 0;
            filterMode = 0;
        } else {
            setupTask();
        }
    }

    private List<AnswerRecord> getFilteredRecords() {
        List<AnswerRecord> out = new ArrayList<>();
        for (AnswerRecord r : records) {
            if (filterMode == 0 || (filterMode == 1 && !r.correct) || (filterMode == 2 && r.correct)) {
                out.add(r);
            }
        }
        return out;
    }

    public String onKeyPressed(int keyCode) {
        if (finished) {
            if (keyCode == KeyEvent.VK_ESCAPE || keyCode == KeyEvent.VK_ENTER) {
                return "finished";
            }
            List<AnswerRecord> filtered = getFilteredRecords();
            int totalPages = Math.max(1, (filtered.size() + REVIEW_PER_PAGE - 1) / REVIEW_PER_PAGE);
            if (keyCode == KeyEvent.VK_LEFT && reviewPage > 0) {
                reviewPage--;
            } else if (keyCode == KeyEvent.VK_RIGHT && reviewPage < totalPages - 1) {
                reviewPage++;
            }
            return null;
        }
        if (keyCode == KeyEvent.VK_ESCAPE) {
            return "close";
        }
        Task t = currentTask();
        if (!"build".equals(t.type)) {
            int idx = -1;
            if (keyCode == KeyEvent.VK_1) idx = 0;
            else if (keyCode == KeyEvent.VK_2) idx = 1;
            else if (keyCode == KeyEvent.VK_3) idx = 2;
            else if (keyCode == KeyEvent.VK_4) idx = 3;
            if (idx >= 0 && idx < shuffledOptions.size()) {
                String chosen = shuffledOptions.get(idx);
                recordAndAdvance(chosen, t.answer, chosen.equals(t.answer));
            }
        } else if (keyCode == KeyEvent.VK_ENTER && !selectedTokens.isEmpty()) {
            String userStr = String.join(" ", selectedTokens);
            String corrStr = String.join(" ", t.tokens);
            recordAndAdvance(userStr, corrStr, selectedTokens.equals(t.tokens));
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (finished) {
            if (doneBtn.contains(p) || closeBtn.contains(p)) {
                return "finished";
            }
            if (retakeBtn.contains(p)) {
                return "retake";
            }
            if (filterAllBtn.contains(p)) {
                filterMode = 0;
                reviewPage = 0;
                return null;
            }
            if (filterWrongBtn.contains(p)) {
                filterMode = 1;
                reviewPage = 0;
                return null;
            }
            if (filterRightBtn.contains(p)) {
                filterMode = 2;
                reviewPage = 0;
                return null;
            }
            List<AnswerRecord> filtered = getFilteredRecords();
            int totalPages = Math.max(1, (filtered.size() + REVIEW_PER_PAGE - 1) / REVIEW_PER_PAGE);
            if (prevPageBtn.contains(p) && reviewPage > 0) {
                reviewPage--;
                return null;
            }
            if (nextPageBtn.contains(p) && reviewPage < totalPages - 1) {
                reviewPage++;
                return null;
            }
            return null;
        }

        if (closeBtn.contains(p)) {
            return "close";
        }
        Task t = currentTask();
        if ("build".equals(t.type)) {
            for (int i = 0; i < bankRects.size(); i++) {
                if (bankRects.get(i).contains(p)) {
                    selectedTokens.add(bankTokens.remove(i));
                    return null;
                }
            }
            for (int i = 0; i < selectedRects.size(); i++) {
                if (selectedRects.get(i).contains(p)) {
                    bankTokens.add(selectedTokens.remove(i));
                    return null;
                }
            }
            if (clearBtn.contains(p)) {
                bankTokens.addAll(selectedTokens);
                selectedTokens.clear();
                return null;
            }
            if (submitBtn.contains(p) && !selectedTokens.isEmpty()) {
                String userStr = String.join(" ", selectedTokens);
                String corrStr = String.join(" ", t.tokens);
                recordAndAdvance(userStr, corrStr, selectedTokens.equals(t.tokens));
                return null;
            }
        } else {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(p)) {
                    String chosen = shuffledOptions.get(i);
                    recordAndAdvance(chosen, t.answer, chosen.equals(t.answer));
                    return null;
                }
            }
        }
        return null;
    }

    private int drawHoverablePrompt(Graphics2D g, String prompt, Font font, int startX, int startY, int maxWidth) {
        promptHits.clear();
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int spaceW = fm.stringWidth(" ");
        int x = startX;
        int y = startY;
        int lineH = 28;

        Stroke prevStroke = g.getStroke();
        Stroke dotted = new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0f, new float[]{3f, 3f}, 0f);

        String[] words = prompt.split(" ");
        for (String w : words) {
            int ww = fm.stringWidth(w);
            if (x > startX && x + ww > startX + maxWidth) {
                x = startX;
                y += lineH;
            }
            String tip = glossary != null ? glossary.lookupWord(w) : null;
            g.setColor(tip != null ? new Color(166, 214, 255) : UIUtils.TEXT);
            g.drawString(w, x, y);
            if (tip != null) {
                g.setColor(UIUtils.GOLD);
                g.setStroke(dotted);
                g.drawLine(x, y + 3, x + ww, y + 3);
                g.setStroke(prevStroke);
                promptHits.add(new WordHit(new Rectangle(x - 2, y - fm.getAscent(), ww + 4, fm.getHeight() + 4), tip));
            }
            x += ww + spaceW;
        }
        return y + lineH;
    }

    private String findHoverTooltip(Point mouse, Task task) {
        if (mouse == null || glossary == null) {
            return null;
        }
        for (WordHit wh : promptHits) {
            if (wh.rect.contains(mouse)) {
                return wh.tooltip;
            }
        }
        if ("build".equals(task.type)) {
            for (int i = 0; i < bankRects.size(); i++) {
                if (bankRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(bankTokens.get(i));
                }
            }
            for (int i = 0; i < selectedRects.size(); i++) {
                if (selectedRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(selectedTokens.get(i));
                }
            }
        } else {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(mouse)) {
                    return glossary.lookupPhrase(shuffledOptions.get(i));
                }
            }
        }
        return null;
    }

    private void drawTooltip(Graphics2D g, Point mouse, String text) {
        Font fTip = assets.getFont(13, true);
        g.setFont(fTip);
        FontMetrics fm = g.getFontMetrics();
        int padX = 12;
        int tw = fm.stringWidth(text) + padX * 2;
        int th = fm.getHeight() + 12;
        int tx = mouse.x + 14;
        int ty = mouse.y - th - 10;
        if (tx + tw > UIUtils.SCREEN_WIDTH - 12) {
            tx = UIUtils.SCREEN_WIDTH - tw - 12;
        }
        if (ty < 12) {
            ty = mouse.y + 20;
        }
        g.setColor(new Color(16, 20, 32, 245));
        g.fillRoundRect(tx, ty, tw, th, 10, 10);
        g.setColor(UIUtils.GOLD);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRoundRect(tx, ty, tw, th, 10, 10);
        g.setColor(UIUtils.TEXT);
        g.drawString(text, tx + padX, ty + fm.getAscent() + 6);
    }

    public void draw(Graphics2D g, Point mouse) {
        g.setColor(UIUtils.DIM);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        if (finished) {
            drawAnswerSheet(g, mouse);
            return;
        }

        Rectangle panel = new Rectangle(48, 32, UIUtils.SCREEN_WIDTH - 96, UIUtils.SCREEN_HEIGHT - 64);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(spec.isBig ? new Color(188, 118, 252) : UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(20, true);
        Font fSub = assets.getFont(13, false);
        Font fPrompt = assets.getFont(17, true);
        Font fOpt = assets.getFont(15, true);
        Font fBody = assets.getFont(14, false);

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString(spec.title, panel.x + 24, panel.y + 38);

        g.setFont(fSub);
        g.setColor(UIUtils.MUTED);
        g.drawString(
            "Question " + (index + 1) + " of " + tasks.size() + "   •   Answer Sheet & Explanations shown at the end",
            panel.x + 24,
            panel.y + 62
        );

        UIUtils.drawButton(g, closeBtn, "X", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        Rectangle barBg = new Rectangle(panel.x + 24, panel.y + 74, panel.width - 48, 8);
        g.setColor(UIUtils.PANEL_DARK);
        g.fillRoundRect(barBg.x, barBg.y, barBg.width, barBg.height, 6, 6);
        int fillW = (int) Math.round(barBg.width * ((double) index / Math.max(1, tasks.size())));
        g.setColor(spec.isBig ? new Color(168, 96, 244) : UIUtils.ACCENT);
        g.fillRoundRect(barBg.x, barBg.y, Math.max(8, fillW), barBg.height, 6, 6);

        Task task = currentTask();
        int y = drawHoverablePrompt(g, task.prompt, fPrompt, panel.x + 24, panel.y + 118, panel.width - 48);
        y += 14;

        if (!"build".equals(task.type)) {
            optionRects.clear();
            int colW = (panel.width - 68) / 2;
            for (int i = 0; i < shuffledOptions.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                Rectangle r = new Rectangle(panel.x + 24 + col * (colW + 20), y + row * 70, colW, 56);
                optionRects.add(r);
                UIUtils.drawButton(
                    g, r, (i + 1) + ".  " + shuffledOptions.get(i), fOpt,
                    UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER
                );
            }
        } else {
            Rectangle ansBox = new Rectangle(panel.x + 24, y, panel.width - 48, 76);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);
            g.setColor(UIUtils.ACCENT);
            g.drawRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);

            selectedRects.clear();
            g.setFont(fOpt);
            FontMetrics fmOpt = g.getFontMetrics();
            int tx = ansBox.x + 14;
            int ty = ansBox.y + 17;
            if (selectedTokens.isEmpty()) {
                g.setFont(fBody);
                g.setColor(UIUtils.MUTED);
                g.drawString("Click word tiles below in order, then click Submit...", tx, ty + 24);
            } else {
                for (String tok : selectedTokens) {
                    int tw = Math.max(72, fmOpt.stringWidth(tok) + 28);
                    Rectangle r = new Rectangle(tx, ty, tw, 42);
                    selectedRects.add(r);
                    UIUtils.drawButton(g, r, tok, fOpt, UIUtils.ACCENT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);
                    tx += tw + 10;
                }
            }

            int bankY = ansBox.y + ansBox.height + 30;
            g.setFont(fBody);
            g.setColor(UIUtils.MUTED);
            g.drawString("Word Bank (click to add):", panel.x + 24, bankY);
            bankY += 14;

            bankRects.clear();
            int bx = panel.x + 24;
            int by = bankY;
            for (String tok : bankTokens) {
                int tw = Math.max(78, fmOpt.stringWidth(tok) + 30);
                if (bx + tw > panel.x + panel.width - 24) {
                    bx = panel.x + 24;
                    by += 50;
                }
                Rectangle r = new Rectangle(bx, by, tw, 40);
                bankRects.add(r);
                UIUtils.drawButton(g, r, tok, fOpt, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
                bx += tw + 12;
            }

            UIUtils.drawButton(g, clearBtn, "Clear", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
            UIUtils.drawButton(g, submitBtn, "Submit [Enter]", fOpt, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
        }

        String hoverTip = findHoverTooltip(mouse, task);
        if (hoverTip != null) {
            drawTooltip(g, mouse, hoverTip);
        }
    }

    private void drawAnswerSheet(Graphics2D g, Point mouse) {
        Rectangle panel = new Rectangle(48, 30, UIUtils.SCREEN_WIDTH - 96, UIUtils.SCREEN_HEIGHT - 60);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(isPassed() ? UIUtils.SUCCESS : UIUtils.GOLD);
        g.setStroke(new BasicStroke(2.5f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(20, true);
        Font fSub = assets.getFont(14, true);
        Font fCardTitle = assets.getFont(13, true);
        Font fCardBody = assets.getFont(12, false);
        Font fBtn = assets.getFont(13, true);

        int pct = tasks.isEmpty() ? 100 : (int) Math.round(score * 100.0 / tasks.size());
        String status = isPassed() ? "PASSED!" : "KEEP PRACTICING (70% to pass)";

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString("Answer Sheet  -  " + spec.title, panel.x + 24, panel.y + 38);

        g.setFont(fSub);
        g.setColor(isPassed() ? UIUtils.SUCCESS : UIUtils.ERROR);
        g.drawString("Score: " + score + " / " + tasks.size() + " (" + pct + "%)   •   " + status, panel.x + 24, panel.y + 66);

        UIUtils.drawButton(g, closeBtn, "X", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        int wrongCount = records.size() - score;
        UIUtils.drawButton(g, filterAllBtn, "All (" + records.size() + ")", fBtn, filterMode == 0 ? UIUtils.ACCENT : UIUtils.PANEL_LIGHT, UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, filterWrongBtn, "Wrong (" + wrongCount + ")", fBtn, filterMode == 1 ? UIUtils.ERROR : UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, filterRightBtn, "Right (" + score + ")", fBtn, filterMode == 2 ? UIUtils.SUCCESS : UIUtils.PANEL_LIGHT, UIUtils.SUCCESS, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);

        List<AnswerRecord> filtered = getFilteredRecords();
        int totalPages = Math.max(1, (filtered.size() + REVIEW_PER_PAGE - 1) / REVIEW_PER_PAGE);
        if (reviewPage >= totalPages) {
            reviewPage = totalPages - 1;
        }

        g.setFont(fCardBody);
        g.setColor(UIUtils.MUTED);
        g.drawString("Page " + (reviewPage + 1) + " / " + totalPages, panel.x + panel.width - 110, 138);

        int start = reviewPage * REVIEW_PER_PAGE;
        int end = Math.min(filtered.size(), start + REVIEW_PER_PAGE);
        int y = 160;
        int cardH = 88;

        if (filtered.isEmpty()) {
            g.setFont(fSub);
            g.setColor(UIUtils.MUTED);
            g.drawString("No questions in this filter!", panel.x + 28, y + 40);
        } else {
            for (int i = start; i < end; i++) {
                AnswerRecord rec = filtered.get(i);
                Rectangle card = new Rectangle(panel.x + 24, y, panel.width - 48, cardH);
                g.setColor(UIUtils.PANEL_DARK);
                g.fillRoundRect(card.x, card.y, card.width, card.height, 12, 12);
                g.setColor(rec.correct ? UIUtils.SUCCESS : UIUtils.ERROR);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(card.x, card.y, card.width, card.height, 12, 12);

                String mark = rec.correct ? "[OK]" : "[X]";
                g.setFont(fCardTitle);
                g.setColor(rec.correct ? UIUtils.SUCCESS : UIUtils.ERROR);
                String pText = mark + "  Q" + rec.number + ". " + rec.task.prompt;
                if (pText.length() > 78) {
                    pText = pText.substring(0, 77) + "...";
                }
                g.drawString(pText, card.x + 12, card.y + 22);

                g.setFont(fCardBody);
                g.setColor(UIUtils.TEXT);
                String ansLine = rec.correct
                    ? "Your Answer: " + rec.userAnswer + "   (Correct)"
                    : "Your Answer: " + rec.userAnswer + "   |   Correct: " + rec.correctAnswer;
                if (ansLine.length() > 92) {
                    ansLine = ansLine.substring(0, 91) + "...";
                }
                g.drawString(ansLine, card.x + 12, card.y + 44);

                g.setColor(UIUtils.MUTED);
                String expLine = "Why: " + rec.task.explanation;
                if (expLine.length() > 96) {
                    expLine = expLine.substring(0, 95) + "...";
                }
                g.drawString(expLine, card.x + 12, card.y + 66);

                y += cardH + 10;
            }
        }

        if (reviewPage > 0) {
            UIUtils.drawButton(g, prevPageBtn, "< Prev", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        if (reviewPage < totalPages - 1) {
            UIUtils.drawButton(g, nextPageBtn, "Next >", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        }
        UIUtils.drawButton(g, retakeBtn, "Retake Exam", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, doneBtn, "Back to Map", fBtn, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
    }
}
