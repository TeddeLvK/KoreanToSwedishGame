package com.swedishgame;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

public class GamePanel extends JPanel {
    private final List<Level> levels;
    private final AssetLoader assets;
    private final SaveManager[] saveSlots;
    private int activeSlot = 0;
    private SaveManager save;
    private final Glossary glossary;
    private final Runnable onToggleFullscreen;

    private Level currentLevel;
    private final List<ExamSpec> activeExams = new ArrayList<>();
    private MainMenuOverlay mainMenuOverlay;
    private MiniGameOverlay miniGameOverlay;
    private IntroOverlay introOverlay;
    private LevelSelectOverlay levelSelectOverlay;
    private TaskOverlay taskOverlay;
    private ExamOverlay examOverlay;

    private boolean keyUp;
    private boolean keyDown;
    private boolean keyLeft;
    private boolean keyRight;

    private Point mousePos = new Point(-1, -1);
    private String toastText = "";
    private float toastTimer = 0.0f;
    private long lastFrameNanos;

    private double renderScale = 1.0;
    private double renderOffsetX = 0.0;
    private double renderOffsetY = 0.0;

    private final Rectangle menuBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 364, 14, 78, 36);
    private final Rectangle fullBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 278, 14, 72, 36);
    private final Rectangle guideBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 198, 14, 92, 36);
    private final Rectangle levelsBtn = new Rectangle(UIUtils.SCREEN_WIDTH - 98, 14, 86, 36);

    public GamePanel(List<Level> levels, SaveManager[] saveSlots, Glossary glossary, Runnable onToggleFullscreen) {
        this.levels = levels;
        this.saveSlots = saveSlots;
        this.activeSlot = 0;
        this.save = saveSlots[0];
        this.glossary = glossary;
        this.onToggleFullscreen = onToggleFullscreen;
        this.assets = new AssetLoader(Paths.get("Assets"));

        setPreferredSize(new Dimension(UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT));
        setBackground(UIUtils.BG);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        NativeEngine.nativeInit();
        loadLevel(save.currentLevel, save.hasSavedPos, false);
        mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, false);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                handleKeyPressed(e.getKeyCode(), e.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                handleKeyReleased(e.getKeyCode());
            }
        });

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mousePos = toVirtualPoint(e.getPoint());
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mousePos = toVirtualPoint(e.getPoint());
                handleMouseClicked(mousePos);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        lastFrameNanos = System.nanoTime();
        Timer timer = new Timer(16, e -> tick());
        timer.start();
    }

    private void updateRenderTransform() {
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        renderScale = Math.min((double) w / UIUtils.SCREEN_WIDTH, (double) h / UIUtils.SCREEN_HEIGHT);
        if (renderScale <= 0.0) {
            renderScale = 1.0;
        }
        renderOffsetX = (w - UIUtils.SCREEN_WIDTH * renderScale) * 0.5;
        renderOffsetY = (h - UIUtils.SCREEN_HEIGHT * renderScale) * 0.5;
    }

    private Point toVirtualPoint(Point screenPoint) {
        updateRenderTransform();
        int vx = (int) Math.floor((screenPoint.x - renderOffsetX) / renderScale);
        int vy = (int) Math.floor((screenPoint.y - renderOffsetY) / renderScale);
        return new Point(vx, vy);
    }

    private Level findLevel(int id) {
        for (Level lv : levels) {
            if (lv.id == id) {
                return lv;
            }
        }
        return levels.get(0);
    }

    private boolean isWalkableChar(char ch) {
        return ch != 'T' && ch != 'W' && ch != 'R' && ch != 'B'
            && ch != '~' && ch != 'C' && ch != 'G' && ch != 'N' && ch != 'K';
    }

    private List<int[]> findOpenExamTiles(int needed) {
        List<int[]> chosen = new ArrayList<>();
        int[][] preferred = {
            {9, 6}, {7, 6}, {11, 6}, {9, 5}, {7, 5}, {11, 5},
            {9, 7}, {6, 6}, {12, 6}, {8, 6}, {10, 6}, {8, 5}, {10, 5},
            {8, 7}, {10, 7}, {9, 4}, {7, 7}, {11, 7}, {6, 7}, {12, 7}
        };
        for (int[] cand : preferred) {
            if (chosen.size() >= needed) {
                break;
            }
            int tx = cand[0];
            int ty = cand[1];
            if (isTileFreeForExam(tx, ty, chosen)) {
                chosen.add(new int[]{tx, ty});
            }
        }
        for (int r = 2; r < UIUtils.MAP_ROWS - 2 && chosen.size() < needed; r++) {
            for (int c = 2; c < UIUtils.MAP_COLS - 2 && chosen.size() < needed; c++) {
                if (isTileFreeForExam(c, r, chosen)) {
                    chosen.add(new int[]{c, r});
                }
            }
        }
        return chosen;
    }

    private boolean isTileFreeForExam(int tx, int ty, List<int[]> already) {
        if (ty < 0 || ty >= currentLevel.mapRows.size()) {
            return false;
        }
        String row = currentLevel.mapRows.get(ty);
        if (tx < 0 || tx >= row.length()) {
            return false;
        }
        if (!isWalkableChar(row.charAt(tx))) {
            return false;
        }
        if (tx == currentLevel.spawnX && ty == currentLevel.spawnY) {
            return false;
        }
        for (Entity e : currentLevel.entities) {
            if (e.tx == tx && e.ty == ty) {
                return false;
            }
        }
        for (int[] p : already) {
            if (Math.abs(p[0] - tx) + Math.abs(p[1] - ty) < 2) {
                return false;
            }
        }
        return true;
    }

    private void rebuildActiveExams() {
        activeExams.clear();
        if (currentLevel == null) {
            return;
        }
        if (currentLevel.id == 80) {
            activeExams.add(new ExamSpec(
                "final_exam_80",
                "Grand Final Exam (All 79 Levels)",
                "FINAL 100",
                "objects/big_exam_icon",
                9,
                5,
                1,
                79,
                100,
                true
            ));
            return;
        }

        if (countLevelDone() < currentLevel.entities.size()) {
            return;
        }

        int lvId = currentLevel.id;
        boolean hasMilestone = (lvId % 10 == 0 && lvId <= 70);
        boolean hasCh1 = (lvId == 50);
        boolean hasCh2 = (lvId == 79);
        int count = 1 + (hasMilestone ? 1 : 0) + (hasCh1 ? 1 : 0) + (hasCh2 ? 1 : 0);
        List<int[]> tiles = findOpenExamTiles(count);
        int ti = 0;

        if (ti < tiles.size()) {
            int[] pos = tiles.get(ti++);
            activeExams.add(new ExamSpec(
                "exam_lv" + lvId,
                "Level " + lvId + " Revision Exam (10 Qs)",
                "EXAM",
                "objects/exam_icon",
                pos[0],
                pos[1],
                lvId,
                lvId,
                10,
                false
            ));
        }
        if (hasMilestone && ti < tiles.size()) {
            int[] pos = tiles.get(ti++);
            int fromLv = lvId - 9;
            activeExams.add(new ExamSpec(
                "big_exam_" + lvId,
                "Big Exam: Levels " + fromLv + "-" + lvId + " (25 Qs)",
                "BIG EXAM",
                "objects/big_exam_icon",
                pos[0],
                pos[1],
                fromLv,
                lvId,
                25,
                true
            ));
        }
        if (hasCh1 && ti < tiles.size()) {
            int[] pos = tiles.get(ti++);
            activeExams.add(new ExamSpec(
                "chapter1_test",
                "Chapter 1 Test: Levels 1-50 (40 Qs)",
                "CH.1 TEST",
                "objects/big_exam_icon",
                pos[0],
                pos[1],
                1,
                50,
                40,
                true
            ));
        }
        if (hasCh2 && ti < tiles.size()) {
            int[] pos = tiles.get(ti++);
            activeExams.add(new ExamSpec(
                "chapter2_test",
                "Chapter 2 Test: Levels 51-79 (40 Qs)",
                "CH.2 TEST",
                "objects/big_exam_icon",
                pos[0],
                pos[1],
                51,
                79,
                40,
                true
            ));
        }
    }

    private ExamSpec findNearbyExam() {
        float px = NativeEngine.nativeGetPlayerX();
        float py = NativeEngine.nativeGetPlayerY();
        ExamSpec best = null;
        double bestDist = 78.0;
        for (ExamSpec ex : activeExams) {
            double cx = ex.tx * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5;
            double cy = ex.ty * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5;
            double d = Math.hypot(px - cx, py - cy);
            if (d < bestDist) {
                bestDist = d;
                best = ex;
            }
        }
        return best;
    }

    private ExamSpec pickExamAt(int vx, int vy) {
        int wy = vy - UIUtils.HUD_HEIGHT;
        if (wy < 0) {
            return null;
        }
        int tx = vx / UIUtils.TILE_SIZE;
        int ty = wy / UIUtils.TILE_SIZE;
        for (ExamSpec ex : activeExams) {
            if (ex.tx == tx && ex.ty == ty) {
                return ex;
            }
        }
        return null;
    }

    private void startExam(ExamSpec spec) {
        clearMovementKeys();
        List<Task> qs = ExamGenerator.generate(levels, glossary, spec);
        examOverlay = new ExamOverlay(spec, qs, assets, glossary);
    }

    private void checkAchievements(String fallbackToast) {
        List<Achievement> unlocked = AchievementManager.evaluate(save, levels);
        if (!unlocked.isEmpty()) {
            Achievement last = unlocked.get(unlocked.size() - 1);
            showToast("Achievement Unlocked: " + last.title + " (" + last.swedishTitle + ")!", 4.5f);
        } else if (fallbackToast != null) {
            showToast(fallbackToast, 3.5f);
        }
    }

    private void onExamFinished(ExamOverlay overlay) {
        ExamSpec spec = overlay.spec;
        int gained = overlay.getScore() * 10;
        save.xp += gained;
        boolean perfectFinal = "final_exam_80".equals(spec.id)
            && overlay.getTotal() > 0
            && overlay.getScore() == overlay.getTotal();

        String msg;
        if (overlay.isPassed()) {
            boolean firstPass = save.passedExams.add(spec.id);
            if (firstPass) {
                save.xp += spec.isBig ? 150 : 50;
            }
            msg = spec.badgeLabel + " Passed! (" + overlay.getScore() + "/" + overlay.getTotal() + ")";
        } else {
            msg = spec.badgeLabel + " Score: " + overlay.getScore() + "/" + overlay.getTotal() + " - Try again anytime!";
        }

        if (perfectFinal) {
            Achievement mg = AchievementManager.unlockExplicit(AchievementManager.MEATBALL_GOD_ID, save);
            AchievementManager.evaluate(save, levels);
            if (mg != null) {
                showToast("100% PERFECT! Unlocked Badge: MEATBALL GOD!", 6.0f);
            } else {
                showToast("100% PERFECT on the Grand Final! (MEATBALL GOD)", 5.0f);
            }
        } else {
            checkAchievements(msg);
        }
        persist();
    }

    private void loadLevel(int id, boolean useSavedPos, boolean showIntroIfNew) {
        currentLevel = findLevel(id);
        save.currentLevel = currentLevel.id;
        float sx = currentLevel.spawnX * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5f;
        float sy = currentLevel.spawnY * UIUtils.TILE_SIZE + UIUtils.TILE_SIZE * 0.5f;
        NativeEngine.nativeClearEntities();
        for (Entity e : currentLevel.entities) {
            NativeEngine.nativeAddEntity(e.tx, e.ty);
        }
        NativeEngine.nativeLoadMap(currentLevel.getFlatMap(), sx, sy);
        if (useSavedPos && save.hasSavedPos) {
            NativeEngine.nativeSetPlayerPos(save.playerX, save.playerY);
        }
        rebuildActiveExams();
        if (showIntroIfNew && !save.seenIntros.contains(currentLevel.id)) {
            introOverlay = new IntroOverlay(currentLevel, assets, glossary);
            save.seenIntros.add(currentLevel.id);
        }
        persist();
    }

    public void persist() {
        save.playerX = NativeEngine.nativeGetPlayerX();
        save.playerY = NativeEngine.nativeGetPlayerY();
        save.hasSavedPos = true;
        save.save();
    }

    private void showToast(String msg, float seconds) {
        toastText = msg;
        toastTimer = seconds;
    }

    private boolean hasOverlay() {
        return mainMenuOverlay != null
            || miniGameOverlay != null
            || introOverlay != null
            || levelSelectOverlay != null
            || taskOverlay != null
            || examOverlay != null;
    }

    private void clearMovementKeys() {
        keyUp = false;
        keyDown = false;
        keyLeft = false;
        keyRight = false;
    }

    private void openMainMenu() {
        clearMovementKeys();
        persist();
        mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, true);
    }

    private void handleMenuAction(String action) {
        if (action == null) {
            return;
        }
        if ("start".equals(action)) {
            mainMenuOverlay = null;
            if (!save.seenIntros.contains(currentLevel.id)) {
                introOverlay = new IntroOverlay(currentLevel, assets, glossary);
                save.seenIntros.add(currentLevel.id);
                persist();
            }
        } else if (action.startsWith("minigame:")) {
            String mode = action.substring(9);
            mainMenuOverlay = null;
            miniGameOverlay = new MiniGameOverlay(mode, levels, glossary, assets);
        } else if (action.startsWith("load:")) {
            int idx = Integer.parseInt(action.substring(5));
            if (idx >= 0 && idx < saveSlots.length) {
                activeSlot = idx;
                save = saveSlots[activeSlot];
                mainMenuOverlay = null;
                loadLevel(save.currentLevel, save.hasSavedPos, true);
                showToast("Loaded Save Slot " + save.slotNumber + "!", 2.5f);
            }
        } else if (action.startsWith("reset:")) {
            int idx = Integer.parseInt(action.substring(6));
            if (idx >= 0 && idx < saveSlots.length) {
                saveSlots[idx].reset();
                if (idx == activeSlot) {
                    loadLevel(1, false, false);
                }
            }
        } else if ("quit".equals(action)) {
            persist();
            System.exit(0);
        }
    }

    private void onEntityCompleted(Entity ent) {
        boolean isNew = save.completedEntities.add(ent.id);
        if (isNew) {
            save.xp += 20 * ent.tasks.size();
            showToast("Completed " + ent.name + "! Progress saved.", 3.0f);
        } else {
            save.xp += 10;
            showToast("Practiced " + ent.name + "! (+10 XP)", 2.5f);
        }

        int done = countLevelDone();
        if (done == currentLevel.entities.size()) {
            rebuildActiveExams();
            int nextId = currentLevel.id + 1;
            if (nextId <= levels.size() && save.unlockedLevel < nextId) {
                save.unlockedLevel = nextId;
                if (currentLevel.id == 50) {
                    showToast("Chapter 1 Complete! Chapter 1 Test & Chapter 2 unlocked!", 5.0f);
                } else if (currentLevel.id == 79) {
                    showToast("Chapter 2 Complete! Chapter 2 Test & Level 80 Final Exam unlocked!", 5.0f);
                } else {
                    showToast("Level " + currentLevel.id + " Complete! EXAM icon spawned & Lv." + nextId + " unlocked!", 5.0f);
                }
            } else {
                showToast("Level " + currentLevel.id + " tasks done! Click the EXAM icon to revise!", 4.0f);
            }
        }
        List<Achievement> newly = AchievementManager.evaluate(save, levels);
        if (!newly.isEmpty()) {
            Achievement last = newly.get(newly.size() - 1);
            showToast("Achievement Unlocked: " + last.title + " (" + last.swedishTitle + ")!", 4.5f);
        }
        persist();
    }

    private String getPlayerSpriteKey() {
        boolean ch1Done = true;
        boolean ch2Done = true;
        for (Level lv : levels) {
            if (lv.id == 80) {
                continue;
            }
            for (Entity e : lv.entities) {
                if (!save.completedEntities.contains(e.id)) {
                    if (lv.id <= 50) {
                        ch1Done = false;
                    } else {
                        ch2Done = false;
                    }
                }
            }
        }
        if (ch2Done) {
            return "sprites/player_crown_big";
        }
        if (ch1Done) {
            return "sprites/player_crown_small";
        }
        return "sprites/player";
    }

    private int countLevelDone() {
        int done = 0;
        for (Entity e : currentLevel.entities) {
            if (save.completedEntities.contains(e.id)) {
                done++;
            }
        }
        return done;
    }

    private void handleKeyPressed(int code, char keyChar) {
        if (code == KeyEvent.VK_F11) {
            clearMovementKeys();
            onToggleFullscreen.run();
            return;
        }
        if (mainMenuOverlay != null) {
            handleMenuAction(mainMenuOverlay.onKeyPressed(code));
            return;
        }
        if (miniGameOverlay != null) {
            String res = miniGameOverlay.onKeyPressed(code, keyChar);
            if ("close".equals(res)) {
                int earned = miniGameOverlay.getScore() * 15;
                if (earned > 0) {
                    save.xp += earned;
                    persist();
                }
                miniGameOverlay = null;
                mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, false);
            }
            return;
        }
        if (introOverlay != null) {
            if (introOverlay.onKeyPressed(code)) {
                introOverlay = null;
            }
            return;
        }
        if (levelSelectOverlay != null) {
            int res = levelSelectOverlay.onKeyPressed(code);
            if (res == -1) {
                levelSelectOverlay = null;
            }
            return;
        }
        if (examOverlay != null) {
            String res = examOverlay.onKeyPressed(code);
            if ("close".equals(res)) {
                examOverlay = null;
            } else if ("finished".equals(res)) {
                ExamOverlay doneOv = examOverlay;
                examOverlay = null;
                onExamFinished(doneOv);
            }
            return;
        }
        if (taskOverlay != null) {
            String res = taskOverlay.onKeyPressed(code);
            if ("close".equals(res)) {
                taskOverlay = null;
            } else if ("completed".equals(res)) {
                Entity ent = taskOverlay.entity;
                taskOverlay = null;
                onEntityCompleted(ent);
            }
            return;
        }

        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = true;
        else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = true;
        else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = true;
        else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = true;
        else if (code == KeyEvent.VK_M || code == KeyEvent.VK_ESCAPE) {
            openMainMenu();
        } else if (code == KeyEvent.VK_F) {
            clearMovementKeys();
            onToggleFullscreen.run();
        } else if (code == KeyEvent.VK_TAB) {
            clearMovementKeys();
            introOverlay = new IntroOverlay(currentLevel, assets, glossary);
        } else if (code == KeyEvent.VK_L) {
            clearMovementKeys();
            levelSelectOverlay = new LevelSelectOverlay(
                levels, currentLevel.id, save.unlockedLevel, save.completedEntities, save.passedExams, assets
            );
        } else if (code == KeyEvent.VK_E || code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) {
            ExamSpec nearExam = findNearbyExam();
            if (nearExam != null) {
                startExam(nearExam);
                return;
            }
            int nearby = NativeEngine.nativeGetNearbyEntity();
            if (nearby >= 0 && nearby < currentLevel.entities.size()) {
                clearMovementKeys();
                taskOverlay = new TaskOverlay(currentLevel.entities.get(nearby), assets, glossary);
            }
        }
    }

    private void handleKeyReleased(int code) {
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) keyUp = false;
        else if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) keyDown = false;
        else if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) keyLeft = false;
        else if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) keyRight = false;
    }

    private void handleMouseClicked(Point p) {
        if (mainMenuOverlay != null) {
            handleMenuAction(mainMenuOverlay.onMouseClicked(p));
            return;
        }
        if (miniGameOverlay != null) {
            String res = miniGameOverlay.onMouseClicked(p);
            if ("close".equals(res)) {
                int earned = miniGameOverlay.getScore() * 15;
                if (earned > 0) {
                    save.xp += earned;
                    persist();
                }
                miniGameOverlay = null;
                mainMenuOverlay = new MainMenuOverlay(assets, saveSlots, activeSlot, false);
            }
            return;
        }
        if (introOverlay != null) {
            if (introOverlay.onMouseClicked(p)) {
                introOverlay = null;
            }
            return;
        }
        if (levelSelectOverlay != null) {
            int res = levelSelectOverlay.onMouseClicked(p);
            if (res == -1) {
                levelSelectOverlay = null;
            } else if (res > 0) {
                levelSelectOverlay = null;
                if (res != currentLevel.id) {
                    loadLevel(res, false, true);
                }
            }
            return;
        }
        if (examOverlay != null) {
            String res = examOverlay.onMouseClicked(p);
            if ("close".equals(res)) {
                examOverlay = null;
            } else if ("finished".equals(res)) {
                ExamOverlay doneOv = examOverlay;
                examOverlay = null;
                onExamFinished(doneOv);
            } else if ("retake".equals(res)) {
                ExamSpec sp = examOverlay.spec;
                onExamFinished(examOverlay);
                startExam(sp);
            }
            return;
        }
        if (taskOverlay != null) {
            String res = taskOverlay.onMouseClicked(p);
            if ("close".equals(res)) {
                taskOverlay = null;
            } else if ("completed".equals(res)) {
                Entity ent = taskOverlay.entity;
                taskOverlay = null;
                onEntityCompleted(ent);
            }
            return;
        }

        if (menuBtn.contains(p)) {
            openMainMenu();
            return;
        }
        if (fullBtn.contains(p)) {
            clearMovementKeys();
            onToggleFullscreen.run();
            return;
        }
        if (guideBtn.contains(p)) {
            clearMovementKeys();
            introOverlay = new IntroOverlay(currentLevel, assets, glossary);
            return;
        }
        if (levelsBtn.contains(p)) {
            clearMovementKeys();
            levelSelectOverlay = new LevelSelectOverlay(
                levels, currentLevel.id, save.unlockedLevel, save.completedEntities, save.passedExams, assets
            );
            return;
        }
        if (p.y >= UIUtils.HUD_HEIGHT && p.x >= 0 && p.x < UIUtils.SCREEN_WIDTH && p.y < UIUtils.SCREEN_HEIGHT) {
            ExamSpec clickedExam = pickExamAt(p.x, p.y);
            if (clickedExam != null) {
                startExam(clickedExam);
                return;
            }
            int picked = NativeEngine.nativePickEntity(p.x, p.y - UIUtils.HUD_HEIGHT);
            if (picked >= 0 && picked < currentLevel.entities.size()) {
                clearMovementKeys();
                taskOverlay = new TaskOverlay(currentLevel.entities.get(picked), assets, glossary);
            }
        }
    }

    private void tick() {
        long now = System.nanoTime();
        float dt = Math.min(0.05f, (now - lastFrameNanos) / 1_000_000_000.0f);
        lastFrameNanos = now;

        if (!hasOverlay()) {
            NativeEngine.nativeUpdate(dt, keyUp, keyDown, keyLeft, keyRight);
        }
        if (toastTimer > 0.0f) {
            toastTimer = Math.max(0.0f, toastTimer - dt);
        }
        repaint();
    }

    private String tileKey(char ch) {
        switch (ch) {
            case 'P': return "tiles/path";
            case 'F': return "tiles/floor";
            case 'W': return "tiles/wall";
            case 'R': return "tiles/roof_red";
            case 'B': return "tiles/roof_blue";
            case '~': return "tiles/water";
            case 'T': return "tiles/tree";
            case 'f': return "tiles/flower";
            case 'C': return "tiles/counter";
            case 'A': return "tiles/road";
            case 'X': return "tiles/crosswalk";
            case 'S': return "tiles/sidewalk";
            case 'L': return "tiles/plaza";
            case 'G': return "tiles/skyscraper";
            case 'N': return "tiles/neon_shop";
            case 'K': return "tiles/tower_roof";
            default: return "tiles/grass";
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setColor(UIUtils.BG);
        g.fillRect(0, 0, getWidth(), getHeight());

        updateRenderTransform();
        g.translate(renderOffsetX, renderOffsetY);
        g.scale(renderScale, renderScale);
        g.clipRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.SCREEN_HEIGHT);

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int s = UIUtils.TILE_SIZE;
        for (int r = 0; r < currentLevel.mapRows.size(); r++) {
            String row = currentLevel.mapRows.get(r);
            for (int c = 0; c < row.length(); c++) {
                char ch = row.charAt(c);
                int tx = c * s;
                int ty = UIUtils.HUD_HEIGHT + r * s;
                BufferedImage img = assets.getImage(tileKey(ch));
                if (img != null) {
                    g.drawImage(img, tx, ty, s, s, null);
                } else {
                    g.setColor(new Color(92, 162, 76));
                    g.fillRect(tx, ty, s, s);
                }
            }
        }

        float anim = NativeEngine.nativeGetAnimTime();
        int bob = (int) Math.round(Math.sin(anim * 5.0) * 3.0);
        int nearbyIdx = NativeEngine.nativeGetNearbyEntity();
        ExamSpec nearbyExam = findNearbyExam();
        Font fBadge = assets.getFont(11, true);
        Font fExamTag = assets.getFont(10, true);
        Font fPrompt = assets.getFont(12, true);

        for (ExamSpec ex : activeExams) {
            int exX = ex.tx * s;
            int exY = UIUtils.HUD_HEIGHT + ex.ty * s;
            BufferedImage eImg = assets.getImage(ex.spriteKey);
            if (eImg != null) {
                g.drawImage(eImg, exX, exY, s, s, null);
            }
            boolean passed = save.passedExams.contains(ex.id);
            String tag = passed ? ex.badgeLabel + " OK" : ex.badgeLabel;
            g.setFont(fExamTag);
            FontMetrics fmE = g.getFontMetrics();
            int tw = fmE.stringWidth(tag);
            int cx = exX + s / 2;
            int ty = exY - 14 + (passed ? 0 : bob);
            Rectangle pill = new Rectangle(cx - tw / 2 - 6, ty - 6, tw + 12, 16);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(passed ? UIUtils.SUCCESS : (ex.isBig ? new Color(156, 78, 232) : UIUtils.GOLD));
            g.fillRoundRect(pill.x, pill.y, pill.width, pill.height, 8, 8);
            g.setColor(new Color(18, 22, 32));
            g.drawRoundRect(pill.x, pill.y, pill.width, pill.height, 8, 8);
            g.setColor(ex.isBig && !passed ? UIUtils.TEXT : new Color(18, 22, 30));
            g.drawString(tag, pill.x + 6, pill.y + fmE.getAscent() + 1);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (nearbyExam == ex && !hasOverlay()) {
                String label = "[E] " + ex.title;
                g.setFont(fPrompt);
                FontMetrics fmP = g.getFontMetrics();
                int pw = fmP.stringWidth(label);
                int ph = fmP.getHeight();
                Rectangle box = new Rectangle(cx - pw / 2 - 8, exY + s + 2, pw + 16, ph + 6);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(UIUtils.PANEL);
                g.fillRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.GOLD);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.TEXT);
                g.drawString(label, box.x + 8, box.y + fmP.getAscent() + 3);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }

        for (int i = 0; i < currentLevel.entities.size(); i++) {
            Entity ent = currentLevel.entities.get(i);
            int ex = ent.tx * s;
            int ey = UIUtils.HUD_HEIGHT + ent.ty * s;
            BufferedImage spr = assets.getImage(ent.spriteKey);
            if (spr != null) {
                g.drawImage(spr, ex, ey, s, s, null);
            }

            boolean done = save.completedEntities.contains(ent.id);
            Color bCol = done ? UIUtils.SUCCESS : UIUtils.GOLD;
            String bTxt = done ? "OK" : "!";
            int cx = ex + s / 2;
            int cy = ey - 8 + (done ? 0 : bob);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 24, 34));
            g.fillOval(cx - 11, cy - 11, 22, 22);
            g.setColor(bCol);
            g.fillOval(cx - 9, cy - 9, 18, 18);
            g.setFont(fBadge);
            g.setColor(new Color(18, 22, 30));
            FontMetrics fmB = g.getFontMetrics();
            g.drawString(bTxt, cx - fmB.stringWidth(bTxt) / 2, cy + fmB.getAscent() / 2 - 1);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

            if (i == nearbyIdx && nearbyExam == null && !hasOverlay()) {
                String label = "[E] " + ent.name;
                g.setFont(fPrompt);
                FontMetrics fmP = g.getFontMetrics();
                int pw = fmP.stringWidth(label);
                int ph = fmP.getHeight();
                Rectangle box = new Rectangle(cx - pw / 2 - 8, ey + s + 2, pw + 16, ph + 6);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(UIUtils.PANEL);
                g.fillRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.ACCENT);
                g.setStroke(new BasicStroke(1.5f));
                g.drawRoundRect(box.x, box.y, box.width, box.height, 8, 8);
                g.setColor(UIUtils.TEXT);
                g.drawString(label, box.x + 8, box.y + fmP.getAscent() + 3);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            }
        }

        float px = NativeEngine.nativeGetPlayerX();
        float py = NativeEngine.nativeGetPlayerY();
        int pDrawX = Math.round(px - s * 0.5f);
        int pDrawY = Math.round(UIUtils.HUD_HEIGHT + py - s * 0.5f);
        BufferedImage pImg = assets.getImage(getPlayerSpriteKey());
        if (pImg != null) {
            g.drawImage(pImg, pDrawX, pDrawY, s, s, null);
        }
        if (save.unlockedAchievements.contains(AchievementManager.MEATBALL_GOD_ID)) {
            BufferedImage mgBadge = assets.getImage("objects/meatball_god_badge");
            if (mgBadge != null) {
                g.drawImage(mgBadge, pDrawX + s - 14, pDrawY - 6, 20, 20, null);
            }
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setFont(fExamTag);
            String mgTxt = "MEATBALL GOD";
            FontMetrics fmMg = g.getFontMetrics();
            int mgw = fmMg.stringWidth(mgTxt);
            int mgx = Math.round(px) - mgw / 2 - 5;
            int mgy = pDrawY + s + 1;
            g.setColor(new Color(22, 42, 74, 230));
            g.fillRoundRect(mgx, mgy, mgw + 10, 14, 6, 6);
            g.setColor(UIUtils.GOLD);
            g.drawRoundRect(mgx, mgy, mgw + 10, 14, 6, 6);
            g.drawString(mgTxt, mgx + 5, mgy + fmMg.getAscent() + 1);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawHud(g);

        if (toastTimer > 0.0f && !hasOverlay()) {
            Font fT = assets.getFont(14, true);
            g.setFont(fT);
            FontMetrics fmT = g.getFontMetrics();
            int tw = fmT.stringWidth(toastText);
            Rectangle box = new Rectangle(UIUtils.SCREEN_WIDTH / 2 - tw / 2 - 18, UIUtils.SCREEN_HEIGHT - 54, tw + 36, 38);
            g.setColor(UIUtils.PANEL);
            g.fillRoundRect(box.x, box.y, box.width, box.height, 12, 12);
            g.setColor(UIUtils.SUCCESS);
            g.setStroke(new BasicStroke(2f));
            g.drawRoundRect(box.x, box.y, box.width, box.height, 12, 12);
            g.setColor(UIUtils.TEXT);
            g.drawString(toastText, box.x + 18, box.y + 24);
        }

        if (mainMenuOverlay != null) {
            mainMenuOverlay.draw(g, mousePos);
        } else if (miniGameOverlay != null) {
            miniGameOverlay.draw(g, mousePos);
        } else if (introOverlay != null) {
            introOverlay.draw(g, mousePos);
        } else if (levelSelectOverlay != null) {
            levelSelectOverlay.draw(g, mousePos);
        } else if (examOverlay != null) {
            examOverlay.draw(g, mousePos);
        } else if (taskOverlay != null) {
            taskOverlay.draw(g, mousePos);
        }

        g.dispose();
    }

    private void drawHud(Graphics2D g) {
        g.setColor(UIUtils.HUD);
        g.fillRect(0, 0, UIUtils.SCREEN_WIDTH, UIUtils.HUD_HEIGHT);
        g.setColor(UIUtils.HUD_BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawLine(0, UIUtils.HUD_HEIGHT - 1, UIUtils.SCREEN_WIDTH, UIUtils.HUD_HEIGHT - 1);

        g.setFont(assets.getFont(17, true));
        g.setColor(UIUtils.TEXT);
        String chTag;
        if (currentLevel.id == 80) {
            chTag = "Lv.80 FINAL";
        } else if (currentLevel.id <= 50) {
            chTag = "Ch.1 Lv." + currentLevel.id + "/50";
        } else {
            chTag = "Ch.2 Lv." + (currentLevel.id - 50) + "/29";
        }
        g.drawString(chTag + "  " + currentLevel.zoneName, 16, 26);

        g.setFont(assets.getFont(13, false));
        g.setColor(UIUtils.MUTED);
        String taskInfo = currentLevel.id == 80
            ? "Grand Final Exam: 100 Qs"
            : "Tasks: " + countLevelDone() + "/" + currentLevel.entities.size();
        g.drawString(
            taskInfo + "   |   XP: " + save.xp + "   |   Exams: " + save.passedExams.size() + "   |   " + Lang.current().nativeName,
            16,
            49
        );

        Font fBtn = assets.getFont(12, true);
        UIUtils.drawButton(g, menuBtn, "Menu [M]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, fullBtn, "Full [F]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, guideBtn, "Guide [Tab]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
        UIUtils.drawButton(g, levelsBtn, "Levels [L]", fBtn, UIUtils.PANEL_LIGHT, UIUtils.ACCENT, mousePos, UIUtils.TEXT, UIUtils.HUD_BORDER);
    }
}
