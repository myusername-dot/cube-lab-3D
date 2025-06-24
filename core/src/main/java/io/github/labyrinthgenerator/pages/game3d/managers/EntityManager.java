package io.github.labyrinthgenerator.pages.game3d.managers;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import io.github.labyrinthgenerator.pages.game3d.chunks.Chunk;
import io.github.labyrinthgenerator.pages.game3d.entities.Entity;
import io.github.labyrinthgenerator.pages.game3d.entities.player.Player;
import io.github.labyrinthgenerator.pages.game3d.screens.GameScreen;
import io.github.labyrinthgenerator.pages.game3d.thread.TickChunk;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class EntityManager {
    private final AtomicInteger nextId = new AtomicInteger(0);

    private final ConcurrentHashMap<Chunk, ConcurrentHashMap<Entity, Object>> entitiesByChunks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Entity> entitiesById = new ConcurrentHashMap<>();
    private final Object justObject = new Object();

    private volatile boolean isTransaction = false;
    private volatile long transactionId = -1;

    private GameScreen screen;
    private ChunkManager chunkMan;

    public void setScreen(final GameScreen screen) {
        this.screen = screen;
    }

    public void setChunkMan(final ChunkManager chunkMan) {
        this.chunkMan = chunkMan;
    }

    public Chunk addEntityOnChunk(float x, float z, final Entity ent) {
        Chunk chunk;
        entitiesById.put(ent.getId(), ent);
        chunk = chunkMan.get(x, z);
        if (chunk == null) {
            throw new NullPointerException("Chunk at position " + x + ", " + z + " is null.");
        }

        entitiesByChunks.computeIfAbsent(chunk, k -> new ConcurrentHashMap<>());
        entitiesByChunks.get(chunk).put(ent, justObject);
        return chunk;
    }

    public void updateEntityChunk(final Chunk oldChunk, final Chunk newChunk, final Entity ent) {
        Player player = getScreen().getPlayer();
        if (player != null && player.getId() == ent.getId()) {
            log.debug("Try to move the Player to the other chunk.");
        }

        entitiesByChunks.get(oldChunk).remove(ent);
        entitiesByChunks.computeIfAbsent(newChunk, k -> new ConcurrentHashMap<>());
        entitiesByChunks.get(newChunk).put(ent, justObject);

        if (player != null && player.getId() == ent.getId()) {
            log.debug("Player moved to the other chunk!");
        } else {
            log.debug("Entity id: " + ent.getId() + " moved to the other chunk!");
        }
    }

    public int assignId() {
        return nextId.getAndIncrement();
    }

    public Entity getEntityById(final int id) {
        return entitiesById.get(id);
    }

    public GameScreen getScreen() {
        return screen;
    }

    public void removeEntity(Entity ent) {
        entitiesById.remove(ent.getId());
        entitiesByChunks.values().forEach(c -> c.remove(ent));
    }

    public void clear() {
        entitiesByChunks.clear();
        entitiesById.clear();
    }

    public void render3DAllEntities(final ModelBatch mdlBatch, final Environment env, final float delta, float playerX, float playerZ) {
        for (Chunk chunk : chunkMan.getNearestChunks(playerX, playerZ)) {
            for (final Entity ent : entitiesByChunks.get(chunk).keySet()) {
                if (ent.shouldRender3D()) {
                    ent.render3D(mdlBatch, env, delta);
                }
            }
        }
    }

    public synchronized void tickAllEntities(final float delta, float playerX, float playerZ) {
        long tickTime = System.currentTimeMillis();
        try {
            startTickLog();


            // TRANSACTION START
            startTransaction();
            screen.game.getRectMan().joinTransaction(transactionId);


            // THREADS LOGIC START
            List<Chunk> nearestChunks = chunkMan.getNearestChunks(playerX, playerZ);

            List<Future<Boolean>> futures = new ArrayList<>(nearestChunks.size());
            int nThreads = Runtime.getRuntime().availableProcessors();
            if (nThreads > 4) nThreads = 4;
            ExecutorService executorService = Executors.newFixedThreadPool(nThreads);

            for (Chunk chunk : nearestChunks) {
                Set<Entity> entitiesByChunkClone = new HashSet<>(entitiesByChunks.get(chunk).keySet());
                TickChunk tickChunk = new TickChunk(entitiesByChunkClone, delta);
                futures.add(executorService.submit(tickChunk));
            }

            for (Future<Boolean> future : futures) {
                future.get();
            }
            executorService.shutdown();
            // THREADS LOGIC END


            screen.game.getRectMan().commitTransaction();
            commitTransaction();
            // TRANSACTION END


            endTickLogAndChecks(tickTime);

        } catch (Exception e) {
            log.error("An error occurred during tickAllEntities()", e);
            screen.game.getRectMan().rollbackTransaction();
            rollbackTransaction();
            log.error("Rollback transaction not supported.");
            // TRANSACTION END

            rollbackTickLog(tickTime);
        }
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

    private void startTickLog() {
        log.info("Start tick all entities." +
            " Entities count: " + entitiesById.size() +
            ", rectangles count: " + screen.game.getRectMan().rectsCountAndCheck() + ".");
    }

    private void endTickLogAndChecks(long tickTime) {
        AtomicInteger entitiesSize = new AtomicInteger();
        entitiesByChunks.forEach((key, value) -> entitiesSize.addAndGet(value.size()));
        if (entitiesSize.get() != entitiesById.size()) {
            if (entitiesSize.get() > entitiesById.size())
                throw new RuntimeException("entitiesSize.get() > entitiesById.size(): " + entitiesSize.get() + ", " + entitiesById.size());
            else
                throw new RuntimeException("entitiesSize.get() < entitiesById.size(): " + entitiesSize.get() + ", " + entitiesById.size());
        }
        tickTime = System.currentTimeMillis() - tickTime;
        log.info("End tick all entities." +
            " Entities count: " + entitiesSize.get() +
            ", rectangles count: " + screen.game.getRectMan().rectsCountAndCheck() +
            ". Time spent seconds: " + tickTime / 1000d + ".");
    }

    private void rollbackTickLog(long tickTime) {
        log.error(
            "End tick all entities, transaction rollback." +
                " Time spent seconds: " + tickTime / 1000d + "."
        );
    }
}
