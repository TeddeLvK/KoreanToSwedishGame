#include "engine.h"
#include <math.h>
#include <string.h>

static int is_solid_char(char ch) {
    return ch == 'T' || ch == 'W' || ch == 'R' || ch == 'B' || ch == '~' || ch == 'C' || ch == 'G' || ch == 'N' || ch == 'K';
}

static int is_solid_tile(const EngineState *st, int tx, int ty) {
    if (tx < 0 || ty < 0 || tx >= MAP_COLS || ty >= MAP_ROWS) {
        return 1;
    }
    if (is_solid_char(st->tiles[ty][tx])) {
        return 1;
    }
    for (int i = 0; i < st->entity_count; i++) {
        if (st->entities[i].tx == tx && st->entities[i].ty == ty) {
            return 1;
        }
    }
    return 0;
}

static int collides(const EngineState *st, float cx, float cy) {
    float r = HITBOX_RADIUS;
    float xs[4] = {cx - r, cx + r, cx - r, cx + r};
    float ys[4] = {cy - r, cy - r, cy + r, cy + r};
    for (int i = 0; i < 4; i++) {
        int tx = (int)floorf(xs[i] / TILE_SIZE);
        int ty = (int)floorf(ys[i] / TILE_SIZE);
        if (is_solid_tile(st, tx, ty)) {
            return 1;
        }
    }
    return 0;
}

static int find_nearby(const EngineState *st) {
    int best = -1;
    float best_d = INTERACT_DIST;
    for (int i = 0; i < st->entity_count; i++) {
        float ex = (float)st->entities[i].tx * TILE_SIZE + TILE_SIZE * 0.5f;
        float ey = (float)st->entities[i].ty * TILE_SIZE + TILE_SIZE * 0.5f;
        float dx = st->px - ex;
        float dy = st->py - ey;
        float d = sqrtf(dx * dx + dy * dy);
        if (d < best_d) {
            best_d = d;
            best = i;
        }
    }
    return best;
}

void engine_init(EngineState *st) {
    memset(st, 0, sizeof(EngineState));
    st->nearby_entity = -1;
}

void engine_load_map(EngineState *st, const char *flat_tiles, float spawn_x, float spawn_y) {
    int idx = 0;
    for (int r = 0; r < MAP_ROWS; r++) {
        for (int c = 0; c < MAP_COLS; c++) {
            st->tiles[r][c] = flat_tiles[idx] ? flat_tiles[idx] : '.';
            if (flat_tiles[idx]) {
                idx++;
            }
        }
    }
    st->px = spawn_x;
    st->py = spawn_y;
    st->nearby_entity = find_nearby(st);
}

void engine_clear_entities(EngineState *st) {
    st->entity_count = 0;
    st->nearby_entity = -1;
}

void engine_add_entity(EngineState *st, int tx, int ty) {
    if (st->entity_count < MAX_ENTITIES) {
        st->entities[st->entity_count].tx = tx;
        st->entities[st->entity_count].ty = ty;
        st->entity_count++;
    }
}

void engine_set_player_pos(EngineState *st, float px, float py) {
    st->px = px;
    st->py = py;
    st->nearby_entity = find_nearby(st);
}

void engine_update(EngineState *st, float dt, int up, int down, int left, int right) {
    st->anim_time += dt;
    float dx = 0.0f;
    float dy = 0.0f;
    if (up) dy -= 1.0f;
    if (down) dy += 1.0f;
    if (left) dx -= 1.0f;
    if (right) dx += 1.0f;

    if (dx != 0.0f || dy != 0.0f) {
        float mag = sqrtf(dx * dx + dy * dy);
        dx = (dx / mag) * PLAYER_SPEED * dt;
        dy = (dy / mag) * PLAYER_SPEED * dt;
        if (!collides(st, st->px + dx, st->py)) {
            st->px += dx;
        }
        if (!collides(st, st->px, st->py + dy)) {
            st->py += dy;
        }
    }
    st->nearby_entity = find_nearby(st);
}

int engine_pick_entity(const EngineState *st, float wx, float wy) {
    if (wx < 0.0f || wy < 0.0f) {
        return -1;
    }
    int tx = (int)floorf(wx / TILE_SIZE);
    int ty = (int)floorf(wy / TILE_SIZE);
    for (int i = 0; i < st->entity_count; i++) {
        if (st->entities[i].tx == tx && st->entities[i].ty == ty) {
            return i;
        }
    }
    return -1;
}
