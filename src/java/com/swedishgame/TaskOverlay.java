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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TaskOverlay {
    private static class WordHit {
        final Rectangle rect;
        final String tooltip;

        WordHit(Rectangle rect, String tooltip) {
            this.rect = rect;
            this.tooltip = tooltip;
        }
    }

    public final Entity entity;
    private final AssetLoader assets;
    private final Glossary glossary;
    private int index = 0;

    private List<String> shuffledOptions = new ArrayList<>();
    private final List<Rectangle> optionRects = new ArrayList<>();

    private final List<String> bankTokens = new ArrayList<>();
    private final List<String> selectedTokens = new ArrayList<>();
    private final List<Rectangle> bankRects = new ArrayList<>();
    private final List<Rectangle> selectedRects = new ArrayList<>();
    private final List<WordHit> promptHits = new ArrayList<>();

    private Boolean feedbackOk = null;
    private String feedbackMsg = "";

    private final Rectangle closeBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 102, 50, 36, 36);
    private final Rectangle clearBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 154, UIUtils.SCREEN_HEIGHT - 100, 142, 42);
    private final Rectangle checkBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 + 12, UIUtils.SCREEN_HEIGHT - 100, 142, 42);
    private final Rectangle nextBtn = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - 100, UIUtils.SCREEN_HEIGHT - 100, 200, 42);

    public TaskOverlay(Entity entity, AssetLoader assets, Glossary glossary) {
        this.entity = entity;
        this.assets = assets;
        this.glossary = glossary;
        setupTask();
    }

    private Task currentTask() {
        return entity.tasks.get(index);
    }

    private void setupTask() {
        feedbackOk = null;
        feedbackMsg = "";
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

    private String advanceOrFinish() {
        if (index + 1 < entity.tasks.size()) {
            index++;
            setupTask();
            return null;
        }
        return "completed";
    }

    public String onKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_ESCAPE) {
            return "close";
        }
        if (feedbackOk != null) {
            if (keyCode == KeyEvent.VK_ENTER || keyCode == KeyEvent.VK_SPACE) {
                if (feedbackOk) {
                    return advanceOrFinish();
                }
                feedbackOk = null;
            }
            return null;
        }
        Task task = currentTask();
        if (!"build".equals(task.type)) {
            int idx = -1;
            if (keyCode == KeyEvent.VK_1) idx = 0;
            else if (keyCode == KeyEvent.VK_2) idx = 1;
            else if (keyCode == KeyEvent.VK_3) idx = 2;
            else if (keyCode == KeyEvent.VK_4) idx = 3;
            if (idx >= 0 && idx < shuffledOptions.size()) {
                checkChoice(shuffledOptions.get(idx));
            }
        } else if (keyCode == KeyEvent.VK_ENTER && !selectedTokens.isEmpty()) {
            checkBuild();
        }
        return null;
    }

    public String onMouseClicked(Point p) {
        if (closeBtn.contains(p)) {
            return "close";
        }
        if (feedbackOk != null) {
            if (nextBtn.contains(p)) {
                if (feedbackOk) {
                    return advanceOrFinish();
                }
                feedbackOk = null;
            }
            return null;
        }
        Task task = currentTask();
        if ("build".equals(task.type)) {
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
            if (checkBtn.contains(p) && !selectedTokens.isEmpty()) {
                checkBuild();
            }
        } else {
            for (int i = 0; i < optionRects.size(); i++) {
                if (optionRects.get(i).contains(p)) {
                    checkChoice(shuffledOptions.get(i));
                    return null;
                }
            }
        }
        return null;
    }

    private void checkChoice(String chosen) {
        Task t = currentTask();
        if (chosen.equals(t.answer)) {
            feedbackOk = true;
            feedbackMsg = Lang.tr("Correct! ", "정답! ", "صحيح! ", "正确! ", "¡Correcto! ") + t.explanation;
        } else {
            feedbackOk = false;
            feedbackMsg = Lang.tr("Try again! ", "다시 해보세요! ", "حاول مجدداً! ", "再试一次! ", "¡Inténtalo de nuevo! ") + t.explanation;
        }
    }

    private void checkBuild() {
        Task t = currentTask();
        if (selectedTokens.equals(t.tokens)) {
            feedbackOk = true;
            feedbackMsg = Lang.tr("Correct! ", "정답! ", "صحيح! ", "正确! ", "¡Correcto! ") + t.explanation;
        } else {
            feedbackOk = false;
            feedbackMsg = Lang.tr("Not quite! Expected: ", "다시 확인해 보세요! 정답: ", "الصحيح: ", "正确答案: ", "Esperado: ") + String.join(" ", t.tokens);
        }
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

        Rectangle panel = new Rectangle(54, 38, UIUtils.SCREEN_WIDTH - 108, UIUtils.SCREEN_HEIGHT - 76);
        g.setColor(UIUtils.PANEL);
        g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);
        g.setColor(UIUtils.ACCENT);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panel.x, panel.y, panel.width, panel.height, 20, 20);

        Font fHead = assets.getFont(20, true);
        Font fSub = assets.getFont(13, false);
        Font fPrompt = assets.getFont(17, true);
        Font fOpt = assets.getFont(15, true);
        Font fBody = assets.getFont(14, false);

        BufferedImage spr = assets.getImage(entity.spriteKey);
        if (spr != null) {
            g.drawImage(spr, panel.x + 22, panel.y + 18, 48, 48, null);
        }

        g.setFont(fHead);
        g.setColor(UIUtils.GOLD);
        g.drawString(entity.name, panel.x + 82, panel.y + 40);

        g.setFont(fSub);
        g.setColor(UIUtils.MUTED);
        g.drawString(entity.greeting + "   •   Task " + (index + 1) + "/" + entity.tasks.size(), panel.x + 82, panel.y + 62);

        UIUtils.drawButton(g, closeBtn, "X", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, null);

        Task task = currentTask();
        int y = drawHoverablePrompt(g, task.prompt, fPrompt, panel.x + 24, panel.y + 112, panel.width - 48);
        y += 14;

        if (!"build".equals(task.type)) {
            optionRects.clear();
            int colW = (panel.width - 68) / 2;
            for (int i = 0; i < shuffledOptions.size(); i++) {
                int col = i % 2;
                int row = i / 2;
                Rectangle r = new Rectangle(panel.x + 24 + col * (colW + 20), y + row * 70, colW, 56);
                optionRects.add(r);
                UIUtils.drawButton(g, r, (i + 1) + ".  " + shuffledOptions.get(i), fOpt, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, feedbackOk == null ? mouse : null, UIUtils.TEXT, UIUtils.HUD_BORDER);
            }
        } else {
            Rectangle ansBox = new Rectangle(panel.x + 24, y, panel.width - 48, 72);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);
            g.setColor(UIUtils.ACCENT);
            g.drawRoundRect(ansBox.x, ansBox.y, ansBox.width, ansBox.height, 14, 14);

            selectedRects.clear();
            g.setFont(fOpt);
            FontMetrics fmOpt = g.getFontMetrics();
            int tx = ansBox.x + 14;
            int ty = ansBox.y + 15;
            if (selectedTokens.isEmpty()) {
                g.setFont(fBody);
                g.setColor(UIUtils.MUTED);
                g.drawString(Lang.tr("Click word tiles below in order...", "아래 단어 타일을 순서대로 클릭하세요...", "اضغط على الكلمات بالترتيب...", "按顺序点击下方词语...", "Haz clic en las palabras en orden..."), tx, ty + 24);
            } else {
                for (String tok : selectedTokens) {
                    int tw = Math.max(70, fmOpt.stringWidth(tok) + 26);
                    Rectangle r = new Rectangle(tx, ty, tw, 40);
                    selectedRects.add(r);
                    UIUtils.drawButton(g, r, tok, fOpt, UIUtils.ACCENT, UIUtils.ERROR, feedbackOk == null ? mouse : null, UIUtils.TEXT, null);
                    tx += tw + 10;
                }
            }

            int bankY = ansBox.y + ansBox.height + 28;
            g.setFont(fBody);
            g.setColor(UIUtils.MUTED);
            g.drawString("Word Bank:", panel.x + 24, bankY);
            bankY += 14;

            bankRects.clear();
            int bx = panel.x + 24;
            int by = bankY;
            for (String tok : bankTokens) {
                int tw = Math.max(76, fmOpt.stringWidth(tok) + 28);
                if (bx + tw > panel.x + panel.width - 24) {
                    bx = panel.x + 24;
                    by += 48;
                }
                Rectangle r = new Rectangle(bx, by, tw, 38);
                bankRects.add(r);
                UIUtils.drawButton(g, r, tok, fOpt, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, feedbackOk == null ? mouse : null, UIUtils.TEXT, UIUtils.HUD_BORDER);
                bx += tw + 10;
            }

            if (feedbackOk == null) {
                UIUtils.drawButton(g, clearBtn, "Clear", fOpt, UIUtils.PANEL_LIGHT, UIUtils.ERROR, mouse, UIUtils.TEXT, UIUtils.HUD_BORDER);
                UIUtils.drawButton(g, checkBtn, "Check [Enter]", fOpt, UIUtils.SUCCESS, UIUtils.SUCCESS_HOVER, mouse, UIUtils.TEXT, null);
            }
        }

        if (feedbackOk != null) {
            Rectangle fb = new Rectangle(panel.x + 24, panel.y + panel.height - 138, panel.width - 48, 56);
            g.setColor(UIUtils.PANEL_DARK);
            g.fillRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setColor(feedbackOk ? UIUtils.SUCCESS : UIUtils.ERROR);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(fb.x, fb.y, fb.width, fb.height, 12, 12);
            g.setFont(fBody);
            g.setColor(UIUtils.TEXT);
            int fy = fb.y + 22;
            for (String line : UIUtils.wrapText(feedbackMsg, g.getFontMetrics(), fb.width - 24)) {
                g.drawString(line, fb.x + 12, fy);
                fy += 18;
            }
            UIUtils.drawButton(g, nextBtn, feedbackOk ? Lang.tr("Continue [Enter]", "계속 [Enter]", "متابعة [Enter]", "继续 [Enter]", "Continuar [Enter]") : Lang.tr("Try Again", "다시 시도", "حاول مجدداً", "再试一次", "Reintentar"), fOpt, feedbackOk ? UIUtils.SUCCESS : UIUtils.ACCENT, feedbackOk ? UIUtils.SUCCESS_HOVER : UIUtils.ACCENT_HOVER, mouse, UIUtils.TEXT, null);
        }

        String hoverTip = findHoverTooltip(mouse, task);
        if (hoverTip != null) {
            drawTooltip(g, mouse, hoverTip);
        }
    }
}
