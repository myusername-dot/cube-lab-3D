package io.github.labyrinthgenerator.pages.game3d.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.rect.Chunk;
import io.github.labyrinthgenerator.pages.game3d.rect.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;

import java.util.*;

public class EntityManager {
    private int nextId = 0;

    private final Map<Chunk, Set<Entity>> entitiesByChunk = new HashMap<>();
    private final Map<Integer, Entity> entitiesById = new HashMap<>();

    private ChunkManager chunkMan;

    private GameScreen screen;

    public Chunk addEntityOnChunk(float x, float z, final Entity ent) {
        entitiesById.put(ent.getId(), ent);
        Chunk chunk = chunkMan.get(x, z);
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
        }
        entitiesByChunk.computeIfAbsent(chunk, k -> new HashSet<>());
        entitiesByChunk.get(chunk).add(ent);
        return chunk;
    }

    public void updateEntityChunk(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        entitiesByChunk.get(oldChunk).remove(ent);
        entitiesByChunk.computeIfAbsent(newChunk, k -> new HashSet<>());
        entitiesByChunk.get(newChunk).add(ent);
    }

    public int assignId() {
        return nextId++;
    }

    public Entity getEntityFromId(final int id) {
        return entitiesById.get(id);
    }

    public GameScreen getScreen() {
        return screen;
    }

    public void removeEntity(final int id) {
        Entity ent = entitiesById.get(id);
        entitiesById.remove(id);
        entitiesByChunk.values().forEach(c -> c.remove(ent));
    }

    public void removeAllEntities() {
        entitiesByChunk.values().forEach(Set::clear);
        entitiesByChunk.clear();
        entitiesById.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunk.get(chunk)) {
                if (ent.shouldRender3D()) {
                    ent.render3D(mdlBatch, env, delta);
                }
            }
        }
    }

    public void setScreen(final GameScreen screen) {
        this.screen = screen;
        this.chunkMan = screen.game.getChunkMan();
    }

    public void tickAllEntities(final float delta, float playerX, float playerZ) {
        // clone the array because entities can move between chunks
        Map<Chunk, Set<Entity>> entitiesByChunk = new HashMap<>(this.entitiesByChunk.size());
        this.entitiesByChunk.forEach((key, value) -> entitiesByChunk.put(key, new HashSet<>(value)));

        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (Entity ent : entitiesByChunk.get(chunk)) {
                if (ent.shouldTick()) {
                    ent.tick(delta);
                }
            }
        }
    }
}
