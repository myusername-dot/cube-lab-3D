package io.github.labyrinthgenerator.pages.game3d.utils;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.chunks.ChunkManager;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.thread.TickChunk;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntityManager {
    private int nextId = 0;

    private Map<Chunk, Set<Entity>> entitiesByChunks = new HashMap<>();
    private Map<Integer, Entity> entitiesById = new HashMap<>();

    private final Map<Chunk, Set<Entity>> entitiesByChunksClone = new HashMap<>();
    private final Map<Integer, Entity> entitiesByIdClone = new HashMap<>();

    // Read committed
    private boolean isTransaction = false;

    private ChunkManager chunkMan;

    private GameScreen screen;

    public Chunk addEntityOnChunk(float x, float z, final Entity ent) {
        synchronized (entitiesByChunksClone) {
            synchronized (entitiesByIdClone) {
                Map<Integer, Entity> entitiesById = getTransactionEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

                entitiesById.put(ent.getId(), ent);
                Chunk chunk = chunkMan.get(x, z);
                if (chunk == null) {
                    throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
                }

                entitiesByChunks.computeIfAbsent(chunk, k -> new HashSet<>());
                entitiesByChunks.get(chunk).add(ent);

                return chunk;
            }
        }
    }

    public void updateEntityChunk(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        synchronized (entitiesByChunksClone) {
            synchronized (entitiesByIdClone) {
                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

                entitiesByChunks.get(oldChunk).remove(ent);
                entitiesByChunks.computeIfAbsent(newChunk, k -> new HashSet<>());
                entitiesByChunks.get(newChunk).add(ent);
            }
        }
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
        synchronized (entitiesByChunksClone) {
            synchronized (entitiesByIdClone) {
                Map<Integer, Entity> entitiesById = getTransactionEntitiesById();
                Map<Chunk, Set<Entity>> entitiesByChunks = getTransactionEntitiesByChunks();

                Entity ent = entitiesById.get(id);
                entitiesById.remove(id);
                entitiesByChunks.values().forEach(c -> c.remove(ent));
            }
        }
    }

    public void removeAllEntities() {
        entitiesByChunks.values().forEach(Set::clear);
        entitiesByChunks.clear();
        entitiesById.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunks.get(chunk)) {
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

    public synchronized void tickAllEntities(final float delta, float playerX, float playerZ) throws InterruptedException {
        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);

        // ConcurrentModificationException
        // clone the arrays because entities can move between chunks
        //Map<Chunk, Set<Entity>> entitiesByChunksClone = new HashMap<>(this.entitiesByChunks.size());
        //this.entitiesByChunks.forEach((key, value) -> entitiesByChunksClone.put(key, new HashSet<>(value)));

        startTransaction();
        screen.game.getRectMan().startTransaction();

        CountDownLatch cdl = new CountDownLatch(nearestChunks.size());
        ExecutorService executorService = Executors.newFixedThreadPool(1);

        for (Chunk chunk : nearestChunks) {
            Set<Entity> entitiesByChunk = entitiesByChunks.get(chunk);
            TickChunk tickChunk = new TickChunk(entitiesByChunk, delta, cdl);
            executorService.submit(tickChunk);
        }

        cdl.await();
        executorService.shutdown();

        screen.game.getRectMan().commitTransaction();
        commitTransaction();
    }

    public synchronized void startTransaction() {
        if (isTransaction) {
            throw new RuntimeException("Transaction already started.");
        }
        entitiesByChunks.forEach((key, value) -> entitiesByChunksClone.put(key, new HashSet<>(value)));
        entitiesByIdClone.putAll(entitiesById);
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction already committed.");
        }
        entitiesByChunks = entitiesByChunksClone;
        entitiesById = entitiesByIdClone;
        isTransaction = false;
        entitiesByChunksClone.clear();
        entitiesByIdClone.clear();
    }

    private Map<Chunk, Set<Entity>> getTransactionEntitiesByChunks() {
        if (isTransaction) return entitiesByChunksClone;
        else return entitiesByChunks;
    }

    private Map<Integer, Entity> getTransactionEntitiesById() {
        if (isTransaction) return entitiesByIdClone;
        else return entitiesById;
    }
}
