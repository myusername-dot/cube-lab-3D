package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Vector3;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.thread.TickChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EntityManager {
    private final AtomicInteger nextId = new AtomicInteger(0);
    private final ConcurrentHashMap<Chunk, ConcurrentHashMap<Entity, Object>> entitiesByChunks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Entity> entitiesById = new ConcurrentHashMap<>();
    private final Object justObject = new Object();

    private volatile boolean isTransaction = false;
    private long transactionId = -1;

    private GameScreen screen;
    private ChunkManager chunkMan;
    private RectManager rectMan;

    public void setScreen(final GameScreen screen) {
        this.screen = screen;
        rectMan = screen.game.getRectMan();
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public Chunk addEntityOnChunk(final Vector3 pos, final Entity ent) {
        entitiesById.put(ent.getId(), ent);
        Chunk chunk = chunkMan.get(pos.x, pos.y, pos.z);
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + pos + " is null.");
        }

        entitiesByChunks.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>()).put(ent, justObject);
        return chunk;
    }

    public void updateEntityChunk(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        logEntityMovement(ent);

        entitiesByChunks.get(oldChunk).remove(ent);
        entitiesByChunks.computeIfAbsent(newChunk, k -> new ConcurrentHashMap<>()).put(ent, justObject);
    }

    private void logEntityMovement(Entity ent) {
        Player player = screen.getPlayer();
        if (player != null && player.getId() == ent.getId()) {
            log.debug("Player is moving to the other chunk.");
        } else {
            log.debug("Entity id: " + ent.getId() + " is moving to the other chunk!");
        }
    }

    public List<Entity> getNearestEntities(Vector3 playersPos) {
        List<Chunk> nearestChunks = chunkMan.getNearestChunks(playersPos);
        if (nearestChunks == null || nearestChunks.isEmpty()) {
            throw new NullPointerException("nearestChunks == null || nearestChunks.isEmpty() at position " + playersPos + ".");
        }

        List<Entity> nearestEntities = new ArrayList<>();
        for (Chunk chunk : nearestChunks) {
            if (!entitiesByChunks.containsKey(chunk)) {
                //log.warn("Method getNearestEntities: !entitiesByChunks.containsKey(chunk).");
                continue;
            }
            nearestEntities.addAll(entitiesByChunks.get(chunk).keySet());
        }
        return nearestEntities;
    }

    public int assignId() {
        return nextId.getAndIncrement();
    }

    public Entity getEntityById(final int id) {
        return entitiesById.get(id);
    }

    public void removeEntity(Entity ent) {
        entitiesById.remove(ent.getId());
        entitiesByChunks.values().forEach(c -> c.remove(ent));
    }

    public void clear() {
        entitiesByChunks.clear();
        entitiesById.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, final Vector3 pos) {
        for (Chunk chunk : chunkMan.getNearestChunks(pos)) {
            if (!entitiesByChunks.containsKey(chunk)) {
                //log.warn("Method render3DAllEntities: !entitiesByChunks.containsKey(chunk).");
                continue;
            }
            for (final Entity ent : entitiesByChunks.get(chunk).keySet()) {
                if (ent.shouldRender3D()) {
                    ent.render3D(mdlBatch, env, delta);
                }
            }
        }
    }

    public synchronized void tickAllEntities(final float delta, final Vector3 pos) {
        long tickTime = System.currentTimeMillis();
        try {
            startTransaction();
            screen.game.getRectMan().joinTransaction(transactionId);

            List<Chunk> nearestChunks = chunkMan.getNearestChunks(pos);
            executeTickInParallel(nearestChunks, delta);

            screen.game.getRectMan().commitTransaction();
            commitTransaction();
            logTickDuration(tickTime);

        } catch (Exception e) {
            log.error("An error occurred during tickAllEntities()", e);
            screen.game.getRectMan().rollbackTransaction();
            rollbackTransaction();
            log.error("Rollback transaction not supported.");
        }
    }

    private void executeTickInParallel(List<Chunk> nearestChunks, float delta) throws InterruptedException, ExecutionException {
        ExecutorService executorService = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 4));
        List<Future<Boolean>> futures = new ArrayList<>();

        for (Chunk chunk : nearestChunks) {
            if (!entitiesByChunks.containsKey(chunk)) {
                log.warn("Method getNearestEntities: !entitiesByChunks.containsKey(chunk).");
                continue;
            }
            Set<Entity> entitiesByChunkClone = new HashSet<>(entitiesByChunks.get(chunk).keySet());
            futures.add(executorService.submit(new TickChunk(entitiesByChunkClone, delta)));
        }

        for (Future<Boolean> future : futures) {
            future.get();
        }
        executorService.shutdown();
    }

    public synchronized void startTransaction() {
        if (isTransaction) {
            throw new RuntimeException("Transaction has already started.");
        }
        transactionId = System.nanoTime();
        isTransaction = true;
    }

    public synchronized void commitTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
    }

    public synchronized void rollbackTransaction() {
        if (!isTransaction) {
            throw new RuntimeException("Transaction has already committed.");
        }
        isTransaction = false;
    }

    public boolean isTransaction() {
        return isTransaction;
    }

    public long getTransactionId() {
        return transactionId;
    }

    private void logTickDuration(long tickTime) {
        AtomicInteger entitiesSize = new AtomicInteger();
        entitiesByChunks.forEach((key, value) -> entitiesSize.addAndGet(value.size()));
        if (entitiesSize.get() != entitiesById.size()) {
            throw new RuntimeException("Entity count mismatch: " + entitiesSize.get() + " vs " + entitiesById.size());
        }
        int rectsCount = rectMan.rectsCountAndCheck();
        tickTime = System.currentTimeMillis() - tickTime;
        log.info("Tick complete. Entity count: " + entitiesSize.get() + ", rectangle count: " + rectsCount + ". Time spent seconds: " + tickTime / 1000d + ".");
    }
}
