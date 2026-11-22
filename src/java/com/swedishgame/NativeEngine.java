package com.swedishgame;

public final class NativeEngine {
    private static final int MAP_COLS = 18;
    private static final int MAP_ROWS = 12;
    private static final float TILE_SIZE = 48.0f;
    private static final int MAX_ENTITIES = 16;
    private static final float PLAYER_SPEED = 235.0f;
    private static final float HITBOX_RADIUS = 14.0f;
    private static final float INTERACT_DIST = 78.0f;

    private static boolean useNative;

    private static final char[][] tiles = new char[MAP_ROWS][MAP_COLS];
    private static final int[] entX = new int[MAX_ENTITIES];
    private static final int[] entY = new int[MAX_ENTITIES];
    private static int entityCount = 0;
    private static float px = 0.0f;
    private static float py = 0.0f;
    private static float animTime = 0.0f;
    private static int nearbyEntity = -1;

    static {
        try {
            System.loadLibrary("swedishgame");
            useNative = true;
        } catch (UnsatisfiedLinkError e) {
            useNative = false;
        }
    }

    private NativeEngine() {}

    private static native void jniInit();
    private static native void jniLoadMap(String flatTiles, float spawnX, float spawnY);
    private static native void jniClearEntities();
    private static native void jniAddEntity(int tx, int ty);
    private static native void jniSetPlayerPos(float x, float y);
    private static native void jniUpdate(float dt, boolean up, boolean down, boolean left, boolean right);
    private static native float jniGetPlayerX();
    private static native float jniGetPlayerY();
    private static native int jniGetNearbyEntity();
    private static native int jniPickEntity(float wx, float wy);
    private static native float jniGetAnimTime();

    public static void nativeInit() {
        if (useNative) {
            try {
                jniInit();
                return;
            } catch (UnsatisfiedLinkError e) {
                useNative = false;
            }
        }
        entityCount = 0;
        px = 0.0f;
        py = 0.0f;
        animTime = 0.0f;
        nearbyEntity = -1;
    }

    public static void nativeLoadMap(String flatTiles, float spawnX, float spawnY) {
        if (useNative) {
            jniLoadMap(flatTiles, spawnX, spawnY);
            return;
        }
        int idx = 0;
        for (int r = 0; r < MAP_ROWS; r++) {
            for (int c = 0; c < MAP_COLS; c++) {
                tiles[r][c] = idx < flatTiles.length() ? flatTiles.charAt(idx++) : '.';
            }
        }
        px = spawnX;
        py = spawnY;
        nearbyEntity = findNearby();
    }

    public static void nativeClearEntities() {
        if (useNative) {
            jniClearEntities();
            return;
        }
        entityCount = 0;
        nearbyEntity = -1;
    }

    public static void nativeAddEntity(int tx, int ty) {
        if (useNative) {
            jniAddEntity(tx, ty);
            return;
        }
        if (entityCount < MAX_ENTITIES) {
            entX[entityCount] = tx;
            entY[entityCount] = ty;
            entityCount++;
        }
    }

    public static void nativeSetPlayerPos(float x, float y) {
        if (useNative) {
            jniSetPlayerPos(x, y);
            return;
        }
        px = x;
        py = y;
        nearbyEntity = findNearby();
    }

    public static void nativeUpdate(float dt, boolean up, boolean down, boolean left, boolean right) {
        if (useNative) {
            jniUpdate(dt, up, down, left, right);
            return;
        }
        animTime += dt;
        float dx = 0.0f;
        float dy = 0.0f;
        if (up) dy -= 1.0f;
        if (down) dy += 1.0f;
        if (left) dx -= 1.0f;
        if (right) dx += 1.0f;
        if (dx != 0.0f || dy != 0.0f) {
            float mag = (float) Math.sqrt(dx * dx + dy * dy);
            dx = (dx / mag) * PLAYER_SPEED * dt;
            dy = (dy / mag) * PLAYER_SPEED * dt;
            if (!collides(px + dx, py)) {
                px += dx;
            }
            if (!collides(px, py + dy)) {
                py += dy;
            }
        }
        nearbyEntity = findNearby();
    }

    public static float nativeGetPlayerX() {
        return useNative ? jniGetPlayerX() : px;
    }

    public static float nativeGetPlayerY() {
        return useNative ? jniGetPlayerY() : py;
    }

    public static int nativeGetNearbyEntity() {
        return useNative ? jniGetNearbyEntity() : nearbyEntity;
    }

    public static int nativePickEntity(float wx, float wy) {
        if (useNative) {
            return jniPickEntity(wx, wy);
        }
        if (wx < 0.0f || wy < 0.0f) {
            return -1;
        }
        int tx = (int) Math.floor(wx / TILE_SIZE);
        int ty = (int) Math.floor(wy / TILE_SIZE);
        for (int i = 0; i < entityCount; i++) {
            if (entX[i] == tx && entY[i] == ty) {
                return i;
            }
        }
        return -1;
    }

    public static float nativeGetAnimTime() {
        return useNative ? jniGetAnimTime() : animTime;
    }

    private static boolean isSolidChar(char ch) {
        return ch == 'T' || ch == 'W' || ch == 'R' || ch == 'B'
            || ch == '~' || ch == 'C' || ch == 'G' || ch == 'N' || ch == 'K';
    }

    private static boolean isSolidTile(int tx, int ty) {
        if (tx < 0 || ty < 0 || tx >= MAP_COLS || ty >= MAP_ROWS) {
            return true;
        }
        if (isSolidChar(tiles[ty][tx])) {
            return true;
        }
        for (int i = 0; i < entityCount; i++) {
            if (entX[i] == tx && entY[i] == ty) {
                return true;
            }
        }
        return false;
    }

    private static boolean collides(float cx, float cy) {
        float r = HITBOX_RADIUS;
        float[] xs = {cx - r, cx + r, cx - r, cx + r};
        float[] ys = {cy - r, cy - r, cy + r, cy + r};
        for (int i = 0; i < 4; i++) {
            int tx = (int) Math.floor(xs[i] / TILE_SIZE);
            int ty = (int) Math.floor(ys[i] / TILE_SIZE);
            if (isSolidTile(tx, ty)) {
                return true;
            }
        }
        return false;
    }

    private static int findNearby() {
        int best = -1;
        float bestD = INTERACT_DIST;
        for (int i = 0; i < entityCount; i++) {
            float ex = entX[i] * TILE_SIZE + TILE_SIZE * 0.5f;
            float ey = entY[i] * TILE_SIZE + TILE_SIZE * 0.5f;
            float dx = px - ex;
            float dy = py - ey;
            float d = (float) Math.sqrt(dx * dx + dy * dy);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }
}
