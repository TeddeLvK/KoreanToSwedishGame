#ifndef KOREANGAME_ENGINE_H
#define KOREANGAME_ENGINE_H

#define MAP_COLS 18
#define MAP_ROWS 12
#define TILE_SIZE 48.0f
#define MAX_ENTITIES 16
#define PLAYER_SPEED 235.0f
#define HITBOX_RADIUS 14.0f
#define INTERACT_DIST 78.0f

typedef struct {
    int tx;
    int ty;
} EntityPos;

typedef struct {
    char tiles[MAP_ROWS][MAP_COLS];
    EntityPos entities[MAX_ENTITIES];
    int entity_count;
    float px;
    float py;
    float anim_time;
    int nearby_entity;
} EngineState;

void engine_init(EngineState *st);
void engine_load_map(EngineState *st, const char *flat_tiles, float spawn_x, float spawn_y);
void engine_clear_entities(EngineState *st);
void engine_add_entity(EngineState *st, int tx, int ty);
void engine_set_player_pos(EngineState *st, float px, float py);
void engine_update(EngineState *st, float dt, int up, int down, int left, int right);
int engine_pick_entity(const EngineState *st, float wx, float wy);

#endif
